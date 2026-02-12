package com.skillbridge.category;

import java.util.List;

public record CategoryParentResponse(Long id,
    String name,
    String description,
    String iconCode,
    List<CategoryResponse> subCategories) {
}
