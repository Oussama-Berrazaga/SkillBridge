package com.skillbridge.auth;

import com.skillbridge.user.Role;

public record RegisterRequest(
    String email,
    String password,
    Role role) {
}