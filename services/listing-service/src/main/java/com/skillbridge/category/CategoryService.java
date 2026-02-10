package com.skillbridge.category;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CategoryService {

  private final CategoryRepository repository;
  private final CategoryMapper mapper;

  public CategoryResponse createCategory(CategoryRequest category) {
    Category categoryToSave = mapper.toCategory(category);
    return mapper.toCategoryResponse(repository.save(categoryToSave));
  }

  public CategoryResponse getCategoryById(Long id) {
    Category category = repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    return mapper.toCategoryResponse(category);
  }

  public List<CategoryResponse> getAllCategories() {
    return repository.findAll().stream().map(mapper::toCategoryResponse).toList();
  }

  public CategoryResponse updateCategory(Long id, CategoryRequest updatedCategory) {
    Category existingCategory = repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    existingCategory.setName(updatedCategory.name());
    existingCategory.setDescription(updatedCategory.description());
    existingCategory.setIconCode(updatedCategory.iconCode());
    existingCategory.setParent(updatedCategory.parentId() != null
        ? repository.findById(updatedCategory.parentId())
            .orElseThrow(
                () -> new EntityNotFoundException("Parent category not found with id: " + updatedCategory.parentId()))
        : null);
    // Parent will be set in the service layer after fetching the parent category
    return mapper.toCategoryResponse(repository.save(existingCategory));
  }

  public void deleteCategory(Long id) {
    repository.deleteById(id);
  }

}
