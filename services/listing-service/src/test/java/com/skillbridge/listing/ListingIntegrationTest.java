package com.skillbridge.listing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.category.Category;
import com.skillbridge.category.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Listing Integration Tests")
class ListingIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private ListingRepository listingRepository;
  @Autowired
  private CategoryRepository categoryRepository;

  private Category plumbingCategory;

  @BeforeEach
  void setUp() {
    listingRepository.deleteAll();
    categoryRepository.deleteAll();

    plumbingCategory = categoryRepository.save(
        Category.builder().name("Plumbing").build());
  }

  // --- CREATE LISTING ---

  @Test
  @DisplayName("POST /api/v1/listings creates listing for CLIENT role")
  void createListing_clientRole_returns201() throws Exception {
    ListingRequest request = new ListingRequest(
        "Fix my kitchen sink",
        "The kitchen sink is leaking and needs urgent repair",
        Set.of(plumbingCategory.getId()));

    mockMvc.perform(post("/api/v1/listings")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Fix my kitchen sink"))
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.customerId").value(10))
        .andExpect(jsonPath("$.categoryNames", hasItem("Plumbing")));

    assertThat(listingRepository.findAll()).hasSize(1);
  }

  @Test
  @DisplayName("POST /api/v1/listings returns 403 for TECHNICIAN role")
  void createListing_technicianRole_returns403() throws Exception {
    ListingRequest request = new ListingRequest(
        "Fix my kitchen sink",
        "The kitchen sink is leaking and needs urgent repair",
        Set.of(plumbingCategory.getId()));

    mockMvc.perform(post("/api/v1/listings")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", "20")
        .header("X-User-Role", "TECHNICIAN")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());

    assertThat(listingRepository.findAll()).isEmpty();
  }

  @Test
  @DisplayName("POST /api/v1/listings returns 400 for missing title")
  void createListing_missingTitle_returns400() throws Exception {
    ListingRequest request = new ListingRequest(
        "",
        "The kitchen sink is leaking and needs urgent repair",
        Set.of(plumbingCategory.getId()));

    mockMvc.perform(post("/api/v1/listings")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/v1/listings returns 400 for short description")
  void createListing_shortDescription_returns400() throws Exception {
    ListingRequest request = new ListingRequest(
        "Fix my sink",
        "Too short",
        Set.of(plumbingCategory.getId()));

    mockMvc.perform(post("/api/v1/listings")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/v1/listings returns 404 when category does not exist")
  void createListing_invalidCategory_returns404() throws Exception {
    ListingRequest request = new ListingRequest(
        "Fix my kitchen sink",
        "The kitchen sink is leaking and needs urgent repair",
        Set.of(999L) // non-existent category
    );

    mockMvc.perform(post("/api/v1/listings")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  // --- GET ALL LISTINGS ---

  @Test
  @DisplayName("GET /api/v1/listings returns all listings")
  void getAllListings_returnsAll() throws Exception {
    // Seed two listings directly via service
    Listing l1 = listingRepository.save(Listing.builder()
        .title("Fix sink")
        .description("Sink is leaking badly and needs fixing")
        .customerId(10L)
        .status(ListingStatus.DRAFT)
        .categories(Set.of(plumbingCategory))
        .build());

    Listing l2 = listingRepository.save(Listing.builder()
        .title("Fix shower")
        .description("Shower head is broken and needs replacing")
        .customerId(11L)
        .status(ListingStatus.ACTIVE)
        .categories(Set.of(plumbingCategory))
        .build());

    mockMvc.perform(get("/api/v1/listings")
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[*].title", containsInAnyOrder("Fix sink", "Fix shower")));
  }

  @Test
  @DisplayName("GET /api/v1/listings returns empty list when no listings exist")
  void getAllListings_empty_returnsEmptyList() throws Exception {
    mockMvc.perform(get("/api/v1/listings")
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  // --- SEARCH LISTINGS ---

  @Test
  @DisplayName("GET /api/v1/listings/search filters by title")
  void searchListings_byTitle_returnsMatching() throws Exception {
    listingRepository.save(Listing.builder()
        .title("Fix kitchen sink")
        .description("Sink is leaking badly and needs fixing")
        .customerId(10L)
        .status(ListingStatus.ACTIVE)
        .categories(Set.of(plumbingCategory))
        .build());

    listingRepository.save(Listing.builder()
        .title("Fix bathroom shower")
        .description("Shower head is broken and needs replacing")
        .customerId(10L)
        .status(ListingStatus.ACTIVE)
        .categories(Set.of(plumbingCategory))
        .build());

    mockMvc.perform(get("/api/v1/listings/search")
        .param("title", "kitchen")
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].title").value("Fix kitchen sink"));
  }

  @Test
  @DisplayName("GET /api/v1/listings/search filters by status")
  void searchListings_byStatus_returnsMatching() throws Exception {
    listingRepository.save(Listing.builder()
        .title("Fix sink")
        .description("Sink is leaking badly and needs fixing")
        .customerId(10L)
        .status(ListingStatus.DRAFT)
        .categories(Set.of(plumbingCategory))
        .build());

    listingRepository.save(Listing.builder()
        .title("Fix shower")
        .description("Shower head is broken and needs replacing")
        .customerId(10L)
        .status(ListingStatus.ACTIVE)
        .categories(Set.of(plumbingCategory))
        .build());

    mockMvc.perform(get("/api/v1/listings/search")
        .param("status", "ACTIVE")
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
  }

  // --- ACTIVATE LISTING ---

  @Test
  @DisplayName("POST /api/v1/listings/activate/{id} transitions DRAFT to ACTIVE")
  void activateListing_draftListing_becomesActive() throws Exception {
    Listing listing = listingRepository.save(Listing.builder()
        .title("Fix sink")
        .description("Sink is leaking badly and needs fixing")
        .customerId(10L)
        .status(ListingStatus.DRAFT)
        .categories(Set.of(plumbingCategory))
        .build());

    mockMvc.perform(post("/api/v1/listings/activate/" + listing.getId())
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACTIVE"));

    Listing updated = listingRepository.findById(listing.getId()).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(ListingStatus.ACTIVE);
  }

  @Test
  @DisplayName("POST /api/v1/listings/activate/{id} returns 404 for unknown listing")
  void activateListing_unknownId_returns404() throws Exception {
    mockMvc.perform(post("/api/v1/listings/activate/999")
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/v1/listings/activate/{id} returns 403 when missing auth headers")
  void activateListing_missingHeaders_returns403() throws Exception {
    mockMvc.perform(post("/api/v1/listings/activate/1"))
        .andExpect(status().isForbidden());
  }
}