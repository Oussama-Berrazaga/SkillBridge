package com.skillbridge.listing;

import jakarta.validation.constraints.NotNull;

public record AcceptApplicationRequest(
    @NotNull(message = "Listing ID is required") Long listingId,
    @NotNull(message = "Application ID is required") Long applicationId) {

}
