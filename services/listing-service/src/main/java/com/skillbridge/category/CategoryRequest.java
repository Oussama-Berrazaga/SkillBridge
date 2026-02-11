package com.skillbridge.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank(message = "Category name is required") String name,
        String description,
        @NotBlank(message = "Category icon code is required") String iconCode,
        Long parentId) {

}
