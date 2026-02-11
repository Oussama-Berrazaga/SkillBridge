package com.skillbridge.listing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.category.CategoryRepository;
import com.skillbridge.common.Status;
import com.skillbridge.exception.CategoryNotFoundException;

import jakarta.persistence.EntityNotFoundException;

import com.skillbridge.category.Category;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListingService {

  private final ListingRepository listingRepository;
  private final CategoryRepository categoryRepository;
  private final ListingMapper listingMapper;

  @Transactional
  public ListingResponse createListing(ListingRequest request) {
    // 1. Validate and Fetch Categories
    Set<Category> categories = validateAndGetCategories(request.categoryIds());

    // 2. Build the Entity
    Listing listing = Listing.builder()
        .title(request.title())
        .description(request.description())
        .customerId(request.customerId())
        .status(Status.DRAFT)
        .categories(categories)
        .build();

    // 3. Save and Return
    Listing saved = listingRepository.save(listing);

    return listingMapper.fromListing(saved);
  }

  private Set<Category> validateAndGetCategories(Set<Long> categoryIds) {
    if (categoryIds == null || categoryIds.isEmpty()) {
      throw new IllegalArgumentException("A listing must belong to at least one category.");
    }

    List<Category> foundCategories = categoryRepository.findAllById(categoryIds);

    // Check if we found as many categories as the user requested
    if (foundCategories.size() != categoryIds.size()) {
      // Find which IDs were missing for a better error message
      Set<Long> foundIds = foundCategories.stream().map(Category::getId).collect(Collectors.toSet());
      categoryIds.removeAll(foundIds);
      throw new CategoryNotFoundException("Categories not found for IDs: " + categoryIds);
    }

    return new HashSet<>(foundCategories);
  }

  public List<ListingResponse> getAllListings() {
    return listingRepository.findAll().stream()
        .map(listingMapper::fromListing)
        .toList();
  }
}
