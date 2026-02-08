package com.skillbridge.user;

import java.time.LocalDate;

public record UserResponse(
                Long id,
                String firstName,
                String lastName,
                String email,
                LocalDate dateOfBirth,
                String phoneNumber,
                Role role) {

}
