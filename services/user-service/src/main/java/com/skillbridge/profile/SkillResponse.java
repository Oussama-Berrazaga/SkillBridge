package com.skillbridge.profile;

public record SkillResponse(
        Long categoryId,
        String categoryName,
        Integer yearsExperience) {
}
