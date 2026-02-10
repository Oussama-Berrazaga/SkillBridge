package com.skillbridge.domain.user;

import com.skillbridge.domain.profile.ProfileResponse;

public record UserResponse(
        Long id,
        String email,
        Role role,
        ProfileResponse profile) {

}
