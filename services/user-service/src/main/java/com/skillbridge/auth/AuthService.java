package com.skillbridge.auth;

import com.skillbridge.user.User;
import com.skillbridge.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthResponse login(AuthRequest request) {
    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new RuntimeException("Invalid credentials");
    }

    String token = jwtService.generateToken(user);
    return new AuthResponse(token, user.getRole().name(), user.getId());
  }
}