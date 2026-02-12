package com.skillbridge.user;

import com.skillbridge.profile.ProfileResponse;

public record UserResponse(
                Long id,
                String email,
                Role role,
                ProfileResponse profile) {

}
