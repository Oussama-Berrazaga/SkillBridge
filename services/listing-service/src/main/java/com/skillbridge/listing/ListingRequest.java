package com.skillbridge.listing;

import java.util.Set;

public record ListingRequest(
    String title,
    String description,
    Long customerId,
    Set<Long> categoryIds // We'll use these to look up real Categories in the Service
) {
}