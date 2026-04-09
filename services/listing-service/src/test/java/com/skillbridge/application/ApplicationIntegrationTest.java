package com.skillbridge.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.category.Category;
import com.skillbridge.category.CategoryRepository;
import com.skillbridge.listing.AcceptApplicationRequest;
import com.skillbridge.listing.Listing;
import com.skillbridge.listing.ListingRepository;
import com.skillbridge.listing.ListingStatus;
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
@DisplayName("Application Integration Tests")
class ApplicationIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private ApplicationRepository applicationRepository;
  @Autowired
  private ListingRepository listingRepository;
  @Autowired
  private CategoryRepository categoryRepository;

  private Listing activeListing;
  private Listing draftListing;

  @BeforeEach
  void setUp() {
    applicationRepository.deleteAll();
    listingRepository.deleteAll();
    categoryRepository.deleteAll();

    Category category = categoryRepository.save(
        Category.builder().name("Plumbing").build());

    activeListing = listingRepository.save(Listing.builder()
        .title("Fix kitchen sink")
        .description("Sink is leaking badly and needs fixing now")
        .customerId(10L)
        .status(ListingStatus.ACTIVE)
        .categories(Set.of(category))
        .build());

    draftListing = listingRepository.save(Listing.builder()
        .title("Fix shower")
        .description("Shower head is broken and needs replacing soon")
        .customerId(10L)
        .status(ListingStatus.DRAFT)
        .categories(Set.of(category))
        .build());
  }

  // --- APPLY ---

  @Test
  @DisplayName("POST /api/v1/applications/apply creates PENDING application for ACTIVE listing")
  void apply_activeListing_createsPendingApplication() throws Exception {
    ApplicationRequest request = new ApplicationRequest(
        20L, "I can fix this today", activeListing.getId());

    mockMvc.perform(post("/api/v1/applications/apply")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", "20")
        .header("X-User-Role", "TECHNICIAN")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.technicianId").value(20))
        .andExpect(jsonPath("$.listing.id").value(activeListing.getId()));

    assertThat(applicationRepository.findAll()).hasSize(1);
  }

  @Test
  @DisplayName("POST /api/v1/applications/apply returns 400 for DRAFT listing")
  void apply_draftListing_returns400() throws Exception {
    ApplicationRequest request = new ApplicationRequest(
        20L, "I can fix this today", draftListing.getId());

    mockMvc.perform(post("/api/v1/applications/apply")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", "20")
        .header("X-User-Role", "TECHNICIAN")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    assertThat(applicationRepository.findAll()).isEmpty();
  }

  @Test
  @DisplayName("POST /api/v1/applications/apply returns 409 on duplicate application")
  void apply_duplicate_returns409() throws Exception {
    ApplicationRequest request = new ApplicationRequest(
        20L, "I can fix this today", activeListing.getId());

    // First application
    mockMvc.perform(post("/api/v1/applications/apply")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", "20")
        .header("X-User-Role", "TECHNICIAN")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Duplicate application
    mockMvc.perform(post("/api/v1/applications/apply")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", "20")
        .header("X-User-Role", "TECHNICIAN")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());

    assertThat(applicationRepository.findAll()).hasSize(1);
  }

  @Test
  @DisplayName("POST /api/v1/applications/apply returns 404 for unknown listing")
  void apply_unknownListing_returns404() throws Exception {
    ApplicationRequest request = new ApplicationRequest(
        20L, "I can fix this today", 999L);

    mockMvc.perform(post("/api/v1/applications/apply")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", "20")
        .header("X-User-Role", "TECHNICIAN")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/v1/applications/apply returns 400 for blank message")
  void apply_blankMessage_returns400() throws Exception {
    ApplicationRequest request = new ApplicationRequest(
        20L, "", activeListing.getId());

    mockMvc.perform(post("/api/v1/applications/apply")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", "20")
        .header("X-User-Role", "TECHNICIAN")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  // --- ACCEPT APPLICATION ---

  @Test
  @DisplayName("POST /api/v1/applications/accept transitions application to ACCEPTED")
  void acceptApplication_pendingApplication_becomesAccepted() throws Exception {
    Application application = applicationRepository.save(Application.builder()
        .technicianId(20L)
        .message("I can fix this")
        .status(ApplicationStatus.PENDING)
        .listing(activeListing)
        .build());

    AcceptApplicationRequest request = new AcceptApplicationRequest(
        activeListing.getId(), application.getId());

    mockMvc.perform(post("/api/v1/applications/accept")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    Application updated = applicationRepository.findById(application.getId()).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
  }

  @Test
  @DisplayName("POST /api/v1/applications/accept returns 404 for unknown application")
  void acceptApplication_unknownApplication_returns404() throws Exception {
    AcceptApplicationRequest request = new AcceptApplicationRequest(
        activeListing.getId(), 999L);

    mockMvc.perform(post("/api/v1/applications/accept")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT")
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  // --- GET ---

  @Test
  @DisplayName("GET /api/v1/applications returns all applications")
  void getAllApplications_returnsAll() throws Exception {
    applicationRepository.save(Application.builder()
        .technicianId(20L)
        .message("I can fix this")
        .status(ApplicationStatus.PENDING)
        .listing(activeListing)
        .build());

    mockMvc.perform(get("/api/v1/applications")
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @DisplayName("GET /api/v1/applications/{id} returns application by ID")
  void getApplicationById_existingId_returnsApplication() throws Exception {
    Application application = applicationRepository.save(Application.builder()
        .technicianId(20L)
        .message("I can fix this")
        .status(ApplicationStatus.PENDING)
        .listing(activeListing)
        .build());

    mockMvc.perform(get("/api/v1/applications/" + application.getId())
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(application.getId()))
        .andExpect(jsonPath("$.technicianId").value(20));
  }

  @Test
  @DisplayName("GET /api/v1/applications/{id} returns 404 for unknown ID")
  void getApplicationById_unknownId_returns404() throws Exception {
    mockMvc.perform(get("/api/v1/applications/999")
        .header("X-User-Id", "10")
        .header("X-User-Role", "CLIENT"))
        .andExpect(status().isNotFound());
  }
}