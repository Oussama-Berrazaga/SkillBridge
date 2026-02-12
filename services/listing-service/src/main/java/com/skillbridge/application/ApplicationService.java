package com.skillbridge.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.common.ApplicationStatus;
import com.skillbridge.common.Status;
import com.skillbridge.exception.ApplicationNotFoundException;
import com.skillbridge.exception.ListingNotFoundException;
import com.skillbridge.listing.Listing;
import com.skillbridge.listing.ListingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationService {

  private final ApplicationRepository applicationRepository;
  private final ListingRepository listingRepository;
  private final ApplicationMapper applicationMapper;

  @Transactional
  public ApplicationResponse apply(ApplicationRequest request) {
    // 1. Find the listing
    Listing listing = listingRepository.findById(request.listingId())
        .orElseThrow(() -> new ListingNotFoundException("Listing not found"));

    // 2. Rule: Only apply to ACTIVE listings
    if (listing.getStatus() != Status.ACTIVE) {
      throw new IllegalStateException("You can only apply to listings that are currently ACTIVE");
    }

    // 3. Rule: Check for duplicate applications
    boolean alreadyApplied = applicationRepository
        .existsByListingIdAndTechnicianId(request.listingId(), request.technicianId());

    if (alreadyApplied) {
      throw new IllegalArgumentException("You have already applied for this listing");
    }

    // 4. Create and Save
    Application application = Application.builder()
        .listing(listing)
        .technicianId(request.technicianId())
        .message(request.message())
        .status(ApplicationStatus.PENDING)
        .build();

    return applicationMapper.toApplicationResponse(applicationRepository.save(application));
  }

  public ApplicationResponse getApplicationById(Long id) {
    Application application = applicationRepository.findById(id)
        .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
    return applicationMapper.toApplicationResponse(application);
  }

  public List<ApplicationResponse> getAllApplications() {
    List<Application> applications = applicationRepository.findAll();
    return applications.stream()
        .map(applicationMapper::toApplicationResponse)
        .toList();
  }
}
