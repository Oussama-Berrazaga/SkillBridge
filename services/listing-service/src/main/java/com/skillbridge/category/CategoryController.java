package com.skillbridge.category;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

  private final CategoryService service;

  @GetMapping
  public ResponseEntity<List<CategoryResponse>> getAllCategories() {
    return ResponseEntity.ok(service.getAllCategories());
  }

  @GetMapping("/{categoryId}")
  public ResponseEntity<CategoryResponse> getCategoryById(@PathParam("categoryId") Long categoryId) {
    return ResponseEntity.ok(service.getCategoryById(categoryId));
  }

  @PostMapping
  public ResponseEntity<CategoryResponse> createCategory(CategoryRequest request) {
    return ResponseEntity.ok(service.createCategory(request));
  }

  @PutMapping("/{categoryId}")
  public ResponseEntity<CategoryResponse> updateCategory(@PathParam("categoryId") Long categoryId,
      CategoryRequest request) {
    return ResponseEntity.ok(service.updateCategory(categoryId, request));
  }

  @DeleteMapping("/{categoryId}")
  public ResponseEntity<Void> deleteCategory(@PathParam("categoryId") Long categoryId) {
    service.deleteCategory(categoryId);
    return ResponseEntity.noContent().build();
  }
}
