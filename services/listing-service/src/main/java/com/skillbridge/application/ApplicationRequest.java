package com.skillbridge.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApplicationRequest(
    @NotNull(message = "Technician ID cannot be null") Long technicianId,
    @NotBlank(message = "Message cannot be blank") String message,
    @NotNull(message = "Listing ID cannot be null") Long listingId) {
}
