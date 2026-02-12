package com.skillbridge.application;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.skillbridge.listing.Listing;
import com.skillbridge.listing.ListingMapper;

@RequiredArgsConstructor
@Component
public class ApplicationMapper {

  private final ListingMapper listingMapper;

  public ApplicationResponse toApplicationResponse(Application application) {
    return new ApplicationResponse(
        application.getId(),
        application.getTechnicianId(),
        application.getMessage(),
        application.getStatus().name(),
        listingMapper.toListingResponse(application.getListing()));
  }

  public Application toApplication(ApplicationRequest request) {
    return Application.builder()
        .technicianId(request.technicianId())
        .message(request.message())
        .listing(Listing.builder().id(request.listingId()).build())
        .build();
  }
}
