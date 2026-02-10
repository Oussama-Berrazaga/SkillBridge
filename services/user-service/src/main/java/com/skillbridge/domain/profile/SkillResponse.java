package com.skillbridge.domain.profile;

public record SkillResponse(
    Long categoryId,
    String categoryName,
    Integer yearsExperience) {
}
