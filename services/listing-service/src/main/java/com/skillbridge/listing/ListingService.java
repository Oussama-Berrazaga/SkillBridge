package com.skillbridge.listing;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.category.CategoryRepository;
import com.skillbridge.common.Status;
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
    // 1. Fetch all categories from the DB based on the IDs in the request
    Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.categoryIds()));

    // 2. Build the entity
    Listing listing = Listing.builder()
        .title(request.title())
        .description(request.description())
        .customerId(request.customerId())
        .status(Status.DRAFT) // New listings start as drafts
        .categories(categories)
        .build();

    Listing saved = listingRepository.save(listing);
    return listingMapper.fromListing(saved);
  }
}
