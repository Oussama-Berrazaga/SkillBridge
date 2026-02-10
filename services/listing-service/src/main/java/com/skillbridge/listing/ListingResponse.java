package com.skillbridge.listing;

import java.util.Set;

public record ListingResponse(
    Long id,
    String title,
    String description,
    String status,
    Long customerId,
    Set<String> categoryNames // Friendly names for the UI
) {
}
