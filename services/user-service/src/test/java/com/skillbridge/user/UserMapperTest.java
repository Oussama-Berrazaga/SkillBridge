package com.skillbridge.user;

import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.skillbridge.profile.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

  private final UserMapper userMapper = new UserMapper();

  @Test
  void shouldMapUserRequestToUserEntityWithSkills() {
    // Given
    SkillRequest skillReq = new SkillRequest(1L, 5);
    ProfileRequest profileReq = new ProfileRequest(
        "John", "Doe", "12345678",
        LocalDate.of(1990, 1, 1), "Bio", List.of(skillReq));
    UserRequest request = new UserRequest("test@test.com", "password", Role.TECHNICIAN, profileReq);

    // When
    User user = userMapper.toUser(request);

    // Then
    assertNotNull(user.getProfile());
    assertEquals("John", user.getProfile().getFirstName());
    assertEquals(1, user.getProfile().getSkills().size());
    assertEquals(user, user.getProfile().getUser()); // Verifies bidirectional link
    assertEquals(user.getProfile(), user.getProfile().getSkills().get(0).getProfile()); // Verifies skill link
  }
}
