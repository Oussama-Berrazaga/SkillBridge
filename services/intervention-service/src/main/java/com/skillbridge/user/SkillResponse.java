package com.skillbridge.user;

public record SkillResponse(
    Long categoryId,
    String categoryName,
    Integer yearsExperience) {
}
