package com.skillbridge.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
    return ResponseEntity.ok(authService.register(request));
  }

  @GetMapping("/get-user-info")
  public ResponseEntity<String> someEndpoint(
      @RequestHeader("X-User-Id") Long userId,
      @RequestHeader("X-User-Role") String role) {

    // You know exactly who is calling and what their role is
    // No JWT parsing needed here

    return ResponseEntity.ok("User id: " + userId + " role: " + role);
  }
}
