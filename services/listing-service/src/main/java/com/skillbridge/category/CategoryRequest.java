package com.skillbridge.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Category name is required") @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters") String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters") String description,

        @NotBlank(message = "Category icon code is required") String iconCode,

        Long parentId) {
}
