package com.skillbridge.domain.user;

import com.skillbridge.domain.profile.ProfileRequest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

        @NotBlank(message = "Password is required") @Size(min = 8) String password,

        @NotNull(message = "Role is required") Role role,

        @NotNull(message = "Profile data is required") ProfileRequest profile) {
}
