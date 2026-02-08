package com.skillbridge.domain.user;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.domain.profile.ProfileRequest;
import com.skillbridge.domain.profile.SkillRequest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // This triggers the application-test.yml
@Transactional
class UserServiceIT {

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Test
  void shouldCreateUserWithProfileAndSkillsInDatabase() {
    // Given
    ProfileRequest profileReq = new ProfileRequest(
        "Jane", "Smith", "98765432",
        LocalDate.of(1992, 5, 20), "Senior Tech",
        List.of(new SkillRequest(10L, 3)));
    UserRequest request = new UserRequest("jane@smith.com", "pass123", Role.TECHNICIAN, profileReq);

    // When
    UserResponse response = userService.createUser(request);

    // Then
    assertNotNull(response.id());
    assertTrue(userRepository.existsByEmail("jane@smith.com"));

    // Deep verification of the cascade save
    User savedUser = userRepository.findByEmail("jane@smith.com").get();
    assertNotNull(savedUser.getProfile());
    assertEquals(1, savedUser.getProfile().getSkills().size());
    assertEquals(10L, savedUser.getProfile().getSkills().get(0).getCategoryId());
    // Verify bidirectional relationship
    assertEquals(savedUser.getId(), savedUser.getProfile().getUser().getId());
  }
}
