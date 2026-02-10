package com.skillbridge.domain.profile;

import java.time.LocalDate;
import java.util.List;

public record ProfileResponse(
    String firstName,
    String lastName,
    String phoneNumber,
    LocalDate birthDate,
    String bio,
    Double averageRating,
    List<SkillResponse> skills) {
}