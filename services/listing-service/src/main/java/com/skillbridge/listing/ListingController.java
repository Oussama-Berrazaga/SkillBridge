package com.skillbridge.listing;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @GetMapping("/search")
  public ResponseEntity<Page<ListingResponse>> search(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) ListingStatus status,
      @RequestParam(required = false) Long categoryId,
      @PageableDefault(size = 10) Pageable pageable) {
    return ResponseEntity.ok(listingService.searchListings(title, status, categoryId, pageable));
  }
}
