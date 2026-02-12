package com.skillbridge.application;

import com.skillbridge.listing.ListingResponse;

public record ApplicationResponse(
    Long id,
    Long technicianId,
    String message,
    String status,
    ListingResponse listing) {

}
