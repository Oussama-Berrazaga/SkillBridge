package com.skillbridge.application;

import com.skillbridge.exception.ApplicationNotFoundException;
import com.skillbridge.exception.ListingNotFoundException;
import com.skillbridge.listing.Listing;
import com.skillbridge.listing.ListingRepository;
import com.skillbridge.listing.ListingResponse;
import com.skillbridge.listing.ListingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationService Unit Tests")
class ApplicationServiceUnitTest {

  @Mock
  private ApplicationRepository applicationRepository;
  @Mock
  private ListingRepository listingRepository;
  @Mock
  private ApplicationMapper applicationMapper;

  @InjectMocks
  private ApplicationService applicationService;

  private Listing activeListing;
  private Listing draftListing;
  private Application pendingApplication;
  private ApplicationRequest request;
  private ApplicationResponse response;

  @BeforeEach
  void setUp() {
    activeListing = Listing.builder()
        .id(1L)
        .title("Fix my sink")
        .description("Sink is broken and leaking water")
        .customerId(10L)
        .status(ListingStatus.ACTIVE)
        .build();

    draftListing = Listing.builder()
        .id(2L)
        .title("Fix my electrical panel")
        .description("Electrical panel needs replacing urgently")
        .customerId(10L)
        .status(ListingStatus.DRAFT)
        .build();

    pendingApplication = Application.builder()
        .id(1L)
        .technicianId(20L)
        .message("I can fix this today")
        .status(ApplicationStatus.PENDING)
        .listing(activeListing)
        .build();

    request = new ApplicationRequest(20L, "I can fix this today", 1L);

    ListingResponse listingResponse = new ListingResponse(
        1L, "Fix my sink", "Sink is broken and leaking water",
        "ACTIVE", 10L, Set.of("Plumbing"));

    response = new ApplicationResponse(
        1L, 20L, "I can fix this today", "PENDING", listingResponse);
  }

  // --- APPLY ---

  @Test
  @DisplayName("apply() creates application for ACTIVE listing")
  void apply_activeListing_createsApplication() {
    when(listingRepository.findById(1L)).thenReturn(Optional.of(activeListing));
    when(applicationRepository.existsByListingIdAndTechnicianId(1L, 20L)).thenReturn(false);
    when(applicationRepository.save(any(Application.class))).thenReturn(pendingApplication);
    when(applicationMapper.toApplicationResponse(pendingApplication)).thenReturn(response);

    ApplicationResponse result = applicationService.apply(request);

    assertThat(result.status()).isEqualTo("PENDING");
    assertThat(result.technicianId()).isEqualTo(20L);
    verify(applicationRepository).save(any(Application.class));
  }

  @Test
  @DisplayName("apply() throws ListingNotFoundException for unknown listing")
  void apply_unknownListing_throwsListingNotFound() {
    when(listingRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> applicationService.apply(request))
        .isInstanceOf(ListingNotFoundException.class);

    verify(applicationRepository, never()).save(any());
  }

  @Test
  @DisplayName("apply() throws IllegalStateException for non-ACTIVE listing")
  void apply_draftListing_throwsIllegalState() {
    when(listingRepository.findById(1L)).thenReturn(Optional.of(draftListing));

    ApplicationRequest draftRequest = new ApplicationRequest(20L, "I can fix this", 1L);
    assertThatThrownBy(() -> applicationService.apply(draftRequest))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("ACTIVE");

    verify(applicationRepository, never()).save(any());
  }

  @Test
  @DisplayName("apply() throws IllegalArgumentException on duplicate application")
  void apply_duplicateApplication_throwsIllegalArgument() {
    when(listingRepository.findById(1L)).thenReturn(Optional.of(activeListing));
    when(applicationRepository.existsByListingIdAndTechnicianId(1L, 20L)).thenReturn(true);

    assertThatThrownBy(() -> applicationService.apply(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already applied");

    verify(applicationRepository, never()).save(any());
  }

  @Test
  @DisplayName("apply() saves application with PENDING status")
  void apply_savedWithPendingStatus() {
    when(listingRepository.findById(1L)).thenReturn(Optional.of(activeListing));
    when(applicationRepository.existsByListingIdAndTechnicianId(any(), any())).thenReturn(false);
    when(applicationRepository.save(any())).thenReturn(pendingApplication);
    when(applicationMapper.toApplicationResponse(any())).thenReturn(response);

    applicationService.apply(request);

    verify(applicationRepository).save(argThat(a -> a.getStatus() == ApplicationStatus.PENDING));
  }

  // --- ACCEPT APPLICATION ---

  @Test
  @DisplayName("acceptApplication() transitions application from PENDING to ACCEPTED")
  void acceptApplication_pendingApplication_becomesAccepted() {
    when(listingRepository.findById(1L)).thenReturn(Optional.of(activeListing));
    when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));

    applicationService.acceptApplication(1L, 1L);

    assertThat(pendingApplication.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
    verify(applicationRepository).save(pendingApplication);
  }

  @Test
  @DisplayName("acceptApplication() throws ListingNotFoundException for unknown listing")
  void acceptApplication_unknownListing_throwsListingNotFound() {
    when(listingRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> applicationService.acceptApplication(99L, 1L))
        .isInstanceOf(ListingNotFoundException.class);
  }

  @Test
  @DisplayName("acceptApplication() throws ApplicationNotFoundException for unknown application")
  void acceptApplication_unknownApplication_throwsApplicationNotFound() {
    when(listingRepository.findById(1L)).thenReturn(Optional.of(activeListing));
    when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> applicationService.acceptApplication(1L, 99L))
        .isInstanceOf(ApplicationNotFoundException.class);
  }

  @Test
  @DisplayName("acceptApplication() throws IllegalStateException for non-ACTIVE listing")
  void acceptApplication_nonActiveListing_throwsIllegalState() {
    when(listingRepository.findById(2L)).thenReturn(Optional.of(draftListing));
    when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));

    assertThatThrownBy(() -> applicationService.acceptApplication(2L, 1L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("ACTIVE");
  }

  @Test
  @DisplayName("acceptApplication() throws IllegalArgumentException when application doesn't belong to listing")
  void acceptApplication_applicationNotBelongingToListing_throwsIllegalArgument() {
    Listing otherListing = Listing.builder()
        .id(99L)
        .title("Different listing")
        .description("This is a completely different listing")
        .status(ListingStatus.ACTIVE)
        .build();

    Application wrongApplication = Application.builder()
        .id(5L)
        .technicianId(20L)
        .message("Wrong listing")
        .status(ApplicationStatus.PENDING)
        .listing(otherListing) // belongs to listing 99, not listing 1
        .build();

    when(listingRepository.findById(1L)).thenReturn(Optional.of(activeListing));
    when(applicationRepository.findById(5L)).thenReturn(Optional.of(wrongApplication));

    assertThatThrownBy(() -> applicationService.acceptApplication(1L, 5L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("does not belong");
  }

  // --- GET APPLICATION ---

  @Test
  @DisplayName("getApplicationById() returns application when found")
  void getApplicationById_existingId_returnsApplication() {
    when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));
    when(applicationMapper.toApplicationResponse(pendingApplication)).thenReturn(response);

    ApplicationResponse result = applicationService.getApplicationById(1L);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.technicianId()).isEqualTo(20L);
  }

  @Test
  @DisplayName("getApplicationById() throws ApplicationNotFoundException for unknown ID")
  void getApplicationById_unknownId_throwsApplicationNotFound() {
    when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> applicationService.getApplicationById(99L))
        .isInstanceOf(ApplicationNotFoundException.class);
  }

  @Test
  @DisplayName("getAllApplications() returns list of all applications")
  void getAllApplications_returnsAll() {
    when(applicationRepository.findAll()).thenReturn(List.of(pendingApplication));
    when(applicationMapper.toApplicationResponse(pendingApplication)).thenReturn(response);

    List<ApplicationResponse> results = applicationService.getAllApplications();

    assertThat(results).hasSize(1);
  }
}