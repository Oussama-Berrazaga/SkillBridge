package com.skillbridge.auth;

import com.skillbridge.exception.UserAlreadyExistsException;
import com.skillbridge.exception.UserNotFoundException;
import com.skillbridge.user.Role;
import com.skillbridge.user.User;
import com.skillbridge.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceUnitTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private JwtService jwtService;
  @Mock
  private AuthenticationManager authenticationManager;

  @InjectMocks
  private AuthService authService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .id(1L)
        .email("client@test.com")
        .password("hashed_password")
        .role(Role.CLIENT)
        .build();
  }

  // --- LOGIN ---

  @Test
  @DisplayName("login() returns AuthResponse with token when credentials are valid")
  void login_validCredentials_returnsAuthResponse() {
    AuthRequest request = new AuthRequest("client@test.com", "Test@123");

    when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(testUser));
    when(jwtService.generateToken(testUser)).thenReturn("mocked.jwt.token");

    AuthResponse response = authService.login(request);

    assertThat(response.token()).isEqualTo("mocked.jwt.token");
    assertThat(response.role()).isEqualTo("CLIENT");
    assertThat(response.userId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("login() throws when AuthenticationManager rejects credentials")
  void login_invalidPassword_throwsBadCredentials() {
    AuthRequest request = new AuthRequest("client@test.com", "wrongpassword");

    doThrow(new BadCredentialsException("Bad credentials"))
        .when(authenticationManager)
        .authenticate(any(UsernamePasswordAuthenticationToken.class));

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BadCredentialsException.class);

    verify(userRepository, never()).findByEmail(any());
  }

  @Test
  @DisplayName("login() throws UserNotFoundException when user not found after auth")
  void login_userNotFound_throwsUserNotFoundException() {
    AuthRequest request = new AuthRequest("ghost@test.com", "Test@123");

    when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  @DisplayName("login() calls authenticationManager with correct credentials")
  void login_callsAuthenticationManagerWithCorrectCredentials() {
    AuthRequest request = new AuthRequest("client@test.com", "Test@123");

    when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(testUser));
    when(jwtService.generateToken(testUser)).thenReturn("token");

    authService.login(request);

    verify(authenticationManager).authenticate(
        new UsernamePasswordAuthenticationToken("client@test.com", "Test@123"));
  }

  // --- REGISTER ---

  @Test
  @DisplayName("register() saves user with encoded password and returns AuthResponse")
  void register_validRequest_savesUserAndReturnsToken() {
    RegisterRequest request = new RegisterRequest("newuser@test.com", "Test@123", Role.TECHNICIAN);

    when(passwordEncoder.encode("Test@123")).thenReturn("encoded_password");
    when(jwtService.generateToken(any(User.class))).thenReturn("mocked.jwt.token");

    AuthResponse response = authService.register(request);

    assertThat(response.token()).isEqualTo("mocked.jwt.token");
    assertThat(response.role()).isEqualTo("TECHNICIAN");

    verify(userRepository).save(argThat(user -> user.getEmail().equals("newuser@test.com") &&
        user.getPassword().equals("encoded_password") &&
        user.getRole() == Role.TECHNICIAN));
  }

  @Test
  @DisplayName("register() never stores plain text password")
  void register_passwordIsAlwaysEncoded() {
    RegisterRequest request = new RegisterRequest("newuser@test.com", "Test@123", Role.CLIENT);

    when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
    when(jwtService.generateToken(any())).thenReturn("token");

    authService.register(request);

    verify(userRepository).save(argThat(user -> !user.getPassword().equals("Test@123")));
  }

  @Test
  @DisplayName("register() calls passwordEncoder with the raw password")
  void register_callsPasswordEncoderWithRawPassword() {
    RegisterRequest request = new RegisterRequest("newuser@test.com", "Test@123", Role.CLIENT);

    when(passwordEncoder.encode("Test@123")).thenReturn("encoded");
    when(jwtService.generateToken(any())).thenReturn("token");

    authService.register(request);

    verify(passwordEncoder).encode("Test@123");
  }

  @Test
  @DisplayName("register() throws UserAlreadyExistsException when email is taken")
  void register_duplicateEmail_throwsUserAlreadyExistsException() {
    RegisterRequest request = new RegisterRequest("existing@test.com", "Test@123", Role.CLIENT);

    when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(UserAlreadyExistsException.class);

    verify(userRepository, never()).save(any());
  }
}