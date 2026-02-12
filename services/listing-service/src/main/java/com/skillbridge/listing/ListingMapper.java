package com.skillbridge.listing;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.skillbridge.category.Category;

@Component
public class ListingMapper {

  public ListingResponse toListingResponse(Listing listing) {
    return new ListingResponse(
        listing.getId(),
        listing.getTitle(),
        listing.getDescription(),
        listing.getStatus().name(),
        listing.getCustomerId(),
        // 3. Map to Response (using Java 21 Streams)
        listing.getCategories().stream().map(Category::getName).collect(Collectors.toSet()));
  }
}
