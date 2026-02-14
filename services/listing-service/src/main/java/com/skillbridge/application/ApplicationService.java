package com.skillbridge.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.exception.ApplicationNotFoundException;
import com.skillbridge.exception.ListingNotFoundException;
import com.skillbridge.listing.Listing;
import com.skillbridge.listing.ListingRepository;
import com.skillbridge.listing.ListingStatus;

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
    if (listing.getStatus() != ListingStatus.ACTIVE) {
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

  @Transactional
  public void acceptApplication(Long listingId, Long applicationId) {
    // 1. Fetch both entities
    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ListingNotFoundException("Listing not found"));

    Application application = applicationRepository.findById(applicationId)
        .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));

    // 2. Validate listing is ACTIVE
    if (listing.getStatus() != ListingStatus.ACTIVE) {
      throw new IllegalStateException("Only ACTIVE listings can accept applications");
    }
    // 3. Validate application belongs to this listing
    if (!application.getListing().getId().equals(listingId)) {
      throw new IllegalArgumentException("Application does not belong to this listing");
    }

    // 4. Execute transition
    application.transitionTo(ApplicationStatus.ACCEPTED);

    // 5. Persistence & Side Effects
    applicationRepository.save(application);

    // TODO: Implement the following side effects:
    // Notify the technician about acceptance (could be an event or direct call)
    // Create a chatroom for the customer and technician to communicate
    // Add application message to the chatroom history (as a first message)

  }
}
