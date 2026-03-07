package com.skillbridge.auth;

public record AuthResponse(
    String token,
    String role,
    Long userId) {
}