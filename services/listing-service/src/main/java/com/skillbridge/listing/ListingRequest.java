package com.skillbridge.listing;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ListingRequest(
                @NotBlank(message = "Title is required") @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters") String title,

                @NotBlank(message = "Description is required") @Size(min = 20, message = "Please provide a more detailed description (at least 20 chars)") String description,

                @NotNull(message = "Customer ID is required") Long customerId,

                @NotEmpty(message = "At least one category must be selected") Set<Long> categoryIds) {
}