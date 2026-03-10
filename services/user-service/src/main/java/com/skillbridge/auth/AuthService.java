package com.skillbridge.auth;

import com.skillbridge.exception.UserNotFoundException;
import com.skillbridge.user.User;
import com.skillbridge.user.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthResponse login(AuthRequest request) {
    // This validates credentials and throws if invalid
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.email(),
            request.password()));

    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new UserNotFoundException("User not found"));

    String token = jwtService.generateToken(user);
    return new AuthResponse(token, user.getRole().name(), user.getId());
  }

  public AuthResponse register(RegisterRequest request) {
    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(request.getRole())
        .build();

    userRepository.save(user);
    String token = jwtService.generateToken(user);
    return new AuthResponse(token, user.getRole().name(), user.getId());
  }
}