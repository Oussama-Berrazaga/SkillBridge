package com.skillbridge.domain.profile;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record ProfileRequest(
    @NotBlank(message = "First name is required") String firstName,
    @NotBlank(message = "Last name is required") String lastName,
    @NotBlank(message = "Phone number is required") String phoneNumber,
    @NotBlank(message = "Birth date is required") LocalDate birthDate,
    String bio,
    List<SkillRequest> skills // Technicians fill this; Clients leave it empty/null
) {
}
