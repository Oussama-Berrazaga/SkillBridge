package com.skillbridge.user;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
                @NotNull(message = "Customer first name is required") String firstName,
                @NotNull(message = "Customer last name is required") String lastName,
                @NotNull(message = "Customer email is required") @Email(message = "Customer email is not a valid email address") String email,
                LocalDate dateOfBirth,
                @NotNull(message = "Customer phone number is required") String phoneNumber,
                @NotNull(message = "Role is required") Role role) {

}
