package com.skillbridge.listing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.category.CategoryRepository;
import com.skillbridge.config.AuthUser;
import com.skillbridge.config.UserRole;
import com.skillbridge.exception.AccessDeniedException;
import com.skillbridge.exception.CategoryNotFoundException;
import com.skillbridge.exception.ListingNotFoundException;

import com.skillbridge.category.Category;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListingService {

  private final ListingRepository listingRepository;
  private final CategoryRepository categoryRepository;
  private final ListingMapper listingMapper;

  @Transactional
  public ListingResponse createListing(ListingRequest request, Long customerId, UserRole role) {

    // 1. External Validation: Check if the caller has the right role
    if (role != UserRole.CLIENT && role != UserRole.ADMIN) {
      throw new AccessDeniedException("Only users with CLIENT or ADMIN role can create listings");
    }

    // 2. Validate and Fetch Categories
    Set<Category> categories = validateAndGetCategories(request.categoryIds());

    // 3. Build the Entity
    Listing listing = Listing.builder()
        .title(request.title())
        .description(request.description())
        .customerId(customerId)
        .status(ListingStatus.DRAFT)
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

  public ListingResponse getListing(Long listingId) {
    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ListingNotFoundException("Listing not found with ID: " + listingId));
    return listingMapper.toListingResponse(listing);
  }

  public Page<ListingResponse> searchListings(String title, ListingStatus status, Long categoryId, Pageable pageable) {
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

  @Transactional
  public ListingResponse activateListing(@NonNull Long listingId, AuthUser user) {
    // 1. External Validation: Check if the caller has the right role
    if (user.role() != UserRole.ADMIN && user.role() != UserRole.CLIENT) {
      throw new AccessDeniedException("Only users with ADMIN or CLIENT role can activate listings");
    }

    // 2. Fetch the listing
    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ListingNotFoundException("Listing not found with ID: " + listingId));

    // 3. If the user is a CLIENT, ensure they own the listing
    if (user.role() == UserRole.CLIENT) {
      if (!listing.getCustomerId().equals(user.userId())) {
        throw new AccessDeniedException("You can only activate your own listings");
      }
    }

    // 4. Transition the status to ACTIVE
    listing.transitionTo(ListingStatus.ACTIVE);
    return listingMapper.toListingResponse(listingRepository.save(listing));
  }
}
