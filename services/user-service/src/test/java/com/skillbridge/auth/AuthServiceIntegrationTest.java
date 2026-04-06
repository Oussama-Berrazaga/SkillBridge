package com.skillbridge.auth;

import com.skillbridge.exception.UserAlreadyExistsException;
import com.skillbridge.user.Role;
import com.skillbridge.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("AuthService Integration Tests")
class AuthServiceIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired
  private AuthService authService;
  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void cleanDb() {
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("register() persists user to DB and returns valid JWT")
  void register_persistsUserAndReturnsJwt() {
    RegisterRequest request = new RegisterRequest("client@test.com", "Test@123", Role.CLIENT);

    AuthResponse response = authService.register(request);

    assertThat(response.token()).isNotBlank();
    assertThat(response.role()).isEqualTo("CLIENT");
    assertThat(userRepository.findByEmail("client@test.com")).isPresent();
  }

  @Test
  @DisplayName("register() stores password as BCrypt hash, not plain text")
  void register_passwordIsHashed() {
    RegisterRequest request = new RegisterRequest("client@test.com", "Test@123", Role.CLIENT);

    authService.register(request);

    String storedPassword = userRepository.findByEmail("client@test.com")
        .get().getPassword();

    assertThat(storedPassword).startsWith("$2a$");
    assertThat(storedPassword).isNotEqualTo("Test@123");
  }

  @Test
  @DisplayName("login() returns valid JWT after registering user")
  void login_afterRegister_returnsJwt() {
    authService.register(new RegisterRequest("tech@test.com", "Test@123", Role.TECHNICIAN));

    AuthResponse response = authService.login(new AuthRequest("tech@test.com", "Test@123"));

    assertThat(response.token()).isNotBlank();
    assertThat(response.role()).isEqualTo("TECHNICIAN");
  }

  @Test
  @DisplayName("login() throws BadCredentialsException for wrong password")
  void login_wrongPassword_throwsBadCredentials() {
    authService.register(new RegisterRequest("client@test.com", "Test@123", Role.CLIENT));

    assertThatThrownBy(() -> authService.login(new AuthRequest("client@test.com", "WrongPass")))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  @DisplayName("login() throws for non-existent user")
  void login_nonExistentUser_throwsException() {
    assertThatThrownBy(() -> authService.login(new AuthRequest("nobody@test.com", "Test@123")))
        .isInstanceOf(Exception.class);
  }

  @Test
  @DisplayName("register() throws UserAlreadyExistsException on duplicate email")
  void register_duplicateEmail_throwsConflict() {
    RegisterRequest request = new RegisterRequest("client@test.com", "Test@123", Role.CLIENT);

    authService.register(request);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(UserAlreadyExistsException.class);
  }
}