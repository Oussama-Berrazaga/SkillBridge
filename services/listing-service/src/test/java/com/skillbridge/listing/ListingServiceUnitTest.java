package com.skillbridge.listing;

import com.skillbridge.category.Category;
import com.skillbridge.category.CategoryRepository;
import com.skillbridge.config.AuthUser;
import com.skillbridge.config.UserRole;
import com.skillbridge.exception.AccessDeniedException;
import com.skillbridge.exception.CategoryNotFoundException;
import com.skillbridge.exception.ListingNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListingService Unit Tests")
class ListingServiceUnitTest {

  @Mock
  private ListingRepository listingRepository;
  @Mock
  private CategoryRepository categoryRepository;
  @Mock
  private ListingMapper listingMapper;

  @InjectMocks
  private ListingService listingService;

  private Category category;
  private Listing listing;
  private ListingRequest request;
  private ListingResponse response;

  @BeforeEach
  void setUp() {
    category = new Category();
    category.setId(1L);
    category.setName("Plumbing");

    listing = Listing.builder()
        .id(1L)
        .title("Fix my kitchen sink")
        .description("The kitchen sink is leaking and needs urgent repair")
        .customerId(10L)
        .status(ListingStatus.DRAFT)
        .categories(Set.of(category))
        .build();

    request = new ListingRequest(
        "Fix my kitchen sink",
        "The kitchen sink is leaking and needs urgent repair",
        Set.of(1L));

    response = new ListingResponse(1L, "Fix my kitchen sink",
        "The kitchen sink is leaking and needs urgent repair",
        "DRAFT", 10L, Set.of("Plumbing"));
  }

  // --- CREATE LISTING ---

  @Test
  @DisplayName("createListing() creates listing successfully for CLIENT role")
  void createListing_clientRole_success() {
    when(categoryRepository.findAllById(Set.of(1L))).thenReturn(List.of(category));
    when(listingRepository.save(any(Listing.class))).thenReturn(listing);
    when(listingMapper.toListingResponse(listing)).thenReturn(response);

    ListingResponse result = listingService.createListing(request, 10L, UserRole.CLIENT);

    assertThat(result.title()).isEqualTo("Fix my kitchen sink");
    assertThat(result.status()).isEqualTo("DRAFT");
    assertThat(result.customerId()).isEqualTo(10L);
    verify(listingRepository).save(any(Listing.class));
  }

  @Test
  @DisplayName("createListing() creates listing successfully for ADMIN role")
  void createListing_adminRole_success() {
    when(categoryRepository.findAllById(Set.of(1L))).thenReturn(List.of(category));
    when(listingRepository.save(any(Listing.class))).thenReturn(listing);
    when(listingMapper.toListingResponse(listing)).thenReturn(response);

    ListingResponse result = listingService.createListing(request, 10L, UserRole.ADMIN);

    assertThat(result).isNotNull();
    verify(listingRepository).save(any(Listing.class));
  }

  @Test
  @DisplayName("createListing() throws AccessDeniedException for TECHNICIAN role")
  void createListing_technicianRole_throwsAccessDenied() {
    assertThatThrownBy(() -> listingService.createListing(request, 10L, UserRole.TECHNICIAN))
        .isInstanceOf(AccessDeniedException.class);

    verify(listingRepository, never()).save(any());
  }

  @Test
  @DisplayName("createListing() throws AccessDeniedException for SUPPORT role")
  void createListing_supportRole_throwsAccessDenied() {
    assertThatThrownBy(() -> listingService.createListing(request, 10L, UserRole.SUPPORT))
        .isInstanceOf(AccessDeniedException.class);

    verify(listingRepository, never()).save(any());
  }

  @Test
  @DisplayName("createListing() throws CategoryNotFoundException when some categories don't exist")
  void createListing_invalidCategoryId_throwsCategoryNotFound() {
    // Request has IDs 1 and 2, but only 1 is found
    ListingRequest requestWithTwoCats = new ListingRequest(
        "Fix my kitchen sink",
        "The kitchen sink is leaking and needs urgent repair",
        Set.of(1L, 2L));
    when(categoryRepository.findAllById(any())).thenReturn(List.of(category)); // only returns 1

    assertThatThrownBy(() -> listingService.createListing(requestWithTwoCats, 10L, UserRole.CLIENT))
        .isInstanceOf(CategoryNotFoundException.class);

    verify(listingRepository, never()).save(any());
  }

  @Test
  @DisplayName("createListing() throws IllegalArgumentException when categoryIds is empty")
  void createListing_emptyCategories_throwsIllegalArgument() {
    ListingRequest emptyCategories = new ListingRequest(
        "Fix my kitchen sink",
        "The kitchen sink is leaking and needs urgent repair",
        Set.of());

    assertThatThrownBy(() -> listingService.createListing(emptyCategories, 10L, UserRole.CLIENT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("at least one category");
  }

  @Test
  @DisplayName("createListing() saves listing with DRAFT status by default")
  void createListing_savedWithDraftStatus() {
    when(categoryRepository.findAllById(any())).thenReturn(List.of(category));
    when(listingRepository.save(any(Listing.class))).thenReturn(listing);
    when(listingMapper.toListingResponse(listing)).thenReturn(response);

    listingService.createListing(request, 10L, UserRole.CLIENT);

    verify(listingRepository).save(argThat(l -> l.getStatus() == ListingStatus.DRAFT));
  }

  // --- ACTIVATE LISTING ---

  @Test
  @DisplayName("activateListing() transitions listing from DRAFT to ACTIVE")
  void activateListing_draftListing_becomesActive() {
    ListingResponse activeResponse = new ListingResponse(1L, "Fix my kitchen sink",
        "The kitchen sink is leaking and needs urgent repair",
        "ACTIVE", 10L, Set.of("Plumbing"));

    when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));
    when(listingRepository.save(any())).thenReturn(listing);
    when(listingMapper.toListingResponse(any())).thenReturn(activeResponse);

    ListingResponse result = listingService.activateListing(1L, new AuthUser(10L, UserRole.CLIENT));

    assertThat(result.status()).isEqualTo("ACTIVE");
  }

  @Test
  @DisplayName("activateListing() throws ListingNotFoundException for unknown ID")
  void activateListing_unknownId_throwsListingNotFound() {
    when(listingRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> listingService.activateListing(99L, new AuthUser(10L, UserRole.CLIENT)))
        .isInstanceOf(ListingNotFoundException.class);
  }

  @Test
  @DisplayName("activateListing() throws IllegalStateException when listing is already ARCHIVED")
  void activateListing_archivedListing_throwsIllegalState() {
    Listing archived = Listing.builder()
        .id(2L)
        .title("Old listing")
        .description("This listing is archived")
        .status(ListingStatus.ARCHIVED)
        .build();
    // manually set to ARCHIVED since builder default is DRAFT
    archived.transitionTo(ListingStatus.ARCHIVED); // DRAFT → ARCHIVED is valid

    when(listingRepository.findById(2L)).thenReturn(Optional.of(archived));

    assertThatThrownBy(() -> listingService.activateListing(2L, new AuthUser(10L, UserRole.CLIENT)))
        .isInstanceOf(IllegalStateException.class);
  }

  // --- GET ALL LISTINGS ---

  @Test
  @DisplayName("getAllListings() returns mapped list of all listings")
  void getAllListings_returnsAllListings() {
    when(listingRepository.findAll()).thenReturn(List.of(listing));
    when(listingMapper.toListingResponse(listing)).thenReturn(response);

    List<ListingResponse> results = listingService.getAllListings();

    assertThat(results).hasSize(1);
    assertThat(results.get(0).title()).isEqualTo("Fix my kitchen sink");
  }

  @Test
  @DisplayName("getAllListings() returns empty list when no listings exist")
  void getAllListings_noListings_returnsEmptyList() {
    when(listingRepository.findAll()).thenReturn(List.of());

    List<ListingResponse> results = listingService.getAllListings();

    assertThat(results).isEmpty();
  }

  // --- SEARCH LISTINGS ---

  @Test
  @DisplayName("searchListings() returns paginated results")
  void searchListings_withFilters_returnsPaginatedResults() {
    Page<Listing> page = new PageImpl<>(List.of(listing));
    when(listingRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
    when(listingMapper.toListingResponse(listing)).thenReturn(response);

    Page<ListingResponse> result = listingService.searchListings(
        "sink", ListingStatus.DRAFT, 1L, PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).title()).isEqualTo("Fix my kitchen sink");
  }

  @Test
  @DisplayName("searchListings() works with no filters")
  void searchListings_noFilters_returnsAllResults() {
    Page<Listing> page = new PageImpl<>(List.of(listing));
    when(listingRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
    when(listingMapper.toListingResponse(listing)).thenReturn(response);

    Page<ListingResponse> result = listingService.searchListings(
        null, null, null, PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(1);
  }
}