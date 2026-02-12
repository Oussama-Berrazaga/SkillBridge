package com.skillbridge.listing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.category.CategoryRepository;
import com.skillbridge.common.Status;
import com.skillbridge.exception.CategoryNotFoundException;
import com.skillbridge.exception.UserNotFoundException;
import com.skillbridge.external.UserServiceClient;

import com.skillbridge.category.Category;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListingService {

  private final ListingRepository listingRepository;
  private final CategoryRepository categoryRepository;
  private final ListingMapper listingMapper;
  private final UserServiceClient userServiceClient;

  @Transactional
  public ListingResponse createListing(ListingRequest request) {

    // 1. External Validation: Check if customer exists in user-service
    boolean userExists = userServiceClient.checkUserExists(request.customerId());
    if (!userExists) {
      throw new UserNotFoundException("Customer with ID " + request.customerId() + " not found in User Service");
    }

    // 2. Validate and Fetch Categories
    Set<Category> categories = validateAndGetCategories(request.categoryIds());

    // 3. Build the Entity
    Listing listing = Listing.builder()
        .title(request.title())
        .description(request.description())
        .customerId(request.customerId())
        .status(Status.DRAFT)
        .categories(categories)
        .build();

    // 4. Save and Return
    Listing saved = listingRepository.save(listing);

    return listingMapper.toListingResponse(saved);
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
        .map(listingMapper::toListingResponse)
        .toList();
  }

  public Page<ListingResponse> searchListings(String title, Status status, Long categoryId, Pageable pageable) {
    // Start with an 'unrestricted' spec (basically 1=1 in SQL)
    Specification<Listing> spec = Specification.unrestricted();

    // Chain the filters only if the parameters are provided
    if (title != null && !title.isBlank()) {
      spec = spec.and(ListingSpecifications.hasTitleLike(title));
    }

    if (status != null) {
      spec = spec.and(ListingSpecifications.hasStatus(status));
    }

    if (categoryId != null) {
      spec = spec.and(ListingSpecifications.belongsToCategory(categoryId));
    }

    return listingRepository.findAll(spec, pageable)
        .map(listingMapper::toListingResponse);
  }

}
