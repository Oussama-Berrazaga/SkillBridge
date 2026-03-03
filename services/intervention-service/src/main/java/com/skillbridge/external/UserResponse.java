package com.skillbridge.external;

import com.skillbridge.external.ProfileResponse;

public record UserResponse(
    Long id,
    String email,
    Role role,
    ProfileResponse profile) {

}
