package com.skillbridge.user;

public record UserResponse(
                Long id,
                String email,
                Role role,
                ProfileResponse profile) {

}
