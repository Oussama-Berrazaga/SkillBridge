package com.skillbridge.listing;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/listings")
public class ListingController {

  private final ListingService listingService;

  @PostMapping
  public ResponseEntity<ListingResponse> create(@RequestBody @Valid ListingRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(listingService.createListing(request));
  }

  @GetMapping
  public ResponseEntity<List<ListingResponse>> getAll() {
    return ResponseEntity.ok(listingService.getAllListings());
  }
}
