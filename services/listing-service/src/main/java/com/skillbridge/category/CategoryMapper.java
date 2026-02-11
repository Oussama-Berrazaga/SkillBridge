package com.skillbridge.category;

import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

  public CategoryResponse toCategoryResponse(Category category) {
    return new CategoryResponse(
        category.getId(),
        category.getName(),
        category.getDescription(),
        category.getIconCode(),
        category.getParent() != null ? category.getParent().getId() : null);
  }

  public Category toCategory(CategoryRequest request) {
    Category category = new Category();
    category.setName(request.name());
    category.setDescription(request.description());
    category.setIconCode(request.iconCode());
    // Parent will be set in the service layer after fetching the parent category
    return category;
  }

  public CategoryParentResponse toCategoryParentResponse(Category category) {
    return new CategoryParentResponse(
        category.getId(),
        category.getName(),
        category.getDescription(),
        category.getIconCode(),
        category.getSubCategories().stream().map(this::toCategoryResponse).toList());
  }
}