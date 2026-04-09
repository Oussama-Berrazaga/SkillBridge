package com.skillbridge.listing;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.config.AuthUser;
import com.skillbridge.config.CurrentUser;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/listings")
public class ListingController {

  private final ListingService listingService;

  @PostMapping
  public ResponseEntity<ListingResponse> create(@RequestBody @Valid ListingRequest request,
      @CurrentUser AuthUser user) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(listingService.createListing(request, user.userId(), user.role()));
  }

  @GetMapping("/{listingId}")
  public ResponseEntity<ListingResponse> getListing(@PathVariable Long listingId) {
    return ResponseEntity.ok(listingService.getListing(listingId));
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

  @PostMapping("/activate/{listingId}")
  public ResponseEntity<ListingResponse> activateListing(@PathVariable Long listingId, @CurrentUser AuthUser user) {
    return ResponseEntity.ok(listingService.activateListing(listingId, user));
  }

  @GetMapping("/test")
  public ResponseEntity<?> someEndpoint(
      @RequestHeader("X-User-Id") Long userId,
      @RequestHeader("X-User-Role") String role) {

    // You know exactly who is calling and what their role is
    // No JWT parsing needed here

    return ResponseEntity.ok("User id: " + userId + " role: " + role);
  }
}
