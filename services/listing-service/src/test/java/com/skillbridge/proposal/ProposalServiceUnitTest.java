package com.skillbridge.proposal;

import com.skillbridge.application.Application;
import com.skillbridge.application.ApplicationRepository;
import com.skillbridge.application.ApplicationStatus;
import com.skillbridge.exception.AccessDeniedException;
import com.skillbridge.exception.ApplicationNotFoundException;
import com.skillbridge.exception.ProposalNotFoundException;
import com.skillbridge.kafka.ListingEventProducer;
import com.skillbridge.listing.Address;
import com.skillbridge.listing.Listing;
import com.skillbridge.listing.ListingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProposalService Unit Tests")
class ProposalServiceUnitTest {

  @Mock
  private ProposalRepository proposalRepository;
  @Mock
  private ApplicationRepository applicationRepository;
  @Mock
  private ProposalMapper proposalMapper;
  @Mock
  private ListingEventProducer eventProducer; // Kafka — always mocked

  @InjectMocks
  private ProposalService proposalService;

  private Listing listing;
  private Application acceptedApplication;
  private Proposal pendingProposal;
  private ProposalRequest request;
  private ProposalResponse response;

  @BeforeEach
  void setUp() {
    Address address = new Address();
    address.setStreet("123 Main St");
    address.setCity("Tunis");
    address.setState("TN");
    address.setZipCode("1000");

    listing = Listing.builder()
        .id(1L)
        .title("Fix my sink")
        .description("Sink is broken and leaking")
        .customerId(10L)
        .status(ListingStatus.ACTIVE)
        .address(address)
        .build();

    acceptedApplication = Application.builder()
        .id(1L)
        .technicianId(20L)
        .message("I can fix this")
        .status(ApplicationStatus.ACCEPTED)
        .listing(listing)
        .build();

    pendingProposal = Proposal.builder()
        .id(1L)
        .application(acceptedApplication)
        .visitFee(new BigDecimal("50.00"))
        .proposedTime(LocalDateTime.now().plusDays(1))
        .build();

    request = new ProposalRequest(
        1L,
        new BigDecimal("50.00"),
        LocalDateTime.now().plusDays(1));

    response = new ProposalResponse(
        1L, 1L,
        LocalDateTime.now().plusDays(1),
        new BigDecimal("50.00"));
  }

  // --- CREATE PROPOSAL ---

  @Test
  @DisplayName("createProposal() creates proposal for ACCEPTED application")
  void createProposal_acceptedApplication_success() {
    when(applicationRepository.findById(1L)).thenReturn(Optional.of(acceptedApplication));
    when(proposalRepository.existsByApplicationIdAndStatus(1L, ProposalStatus.PENDING)).thenReturn(false);
    when(proposalRepository.save(any(Proposal.class))).thenReturn(pendingProposal);
    when(proposalMapper.toProposalResponse(pendingProposal)).thenReturn(response);

    ProposalResponse result = proposalService.createProposal(request);

    assertThat(result.visitFee()).isEqualByComparingTo("50.00");
    assertThat(result.applicationId()).isEqualTo(1L);
    verify(proposalRepository).save(any(Proposal.class));
  }

  @Test
  @DisplayName("createProposal() creates proposal for VISIT_PROPOSED application (re-proposal after rejection)")
  void createProposal_visitProposedApplication_success() {
    Application visitProposedApp = Application.builder()
        .id(1L)
        .technicianId(20L)
        .message("I can fix this")
        .status(ApplicationStatus.VISIT_PROPOSED)
        .listing(listing)
        .build();

    when(applicationRepository.findById(1L)).thenReturn(Optional.of(visitProposedApp));
    when(proposalRepository.existsByApplicationIdAndStatus(1L, ProposalStatus.PENDING)).thenReturn(false);
    when(proposalRepository.save(any())).thenReturn(pendingProposal);
    when(proposalMapper.toProposalResponse(any())).thenReturn(response);

    ProposalResponse result = proposalService.createProposal(request);

    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("createProposal() throws ApplicationNotFoundException for unknown application")
  void createProposal_unknownApplication_throwsApplicationNotFound() {
    when(applicationRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> proposalService.createProposal(request))
        .isInstanceOf(ApplicationNotFoundException.class);

    verify(proposalRepository, never()).save(any());
  }

  @Test
  @DisplayName("createProposal() throws IllegalStateException for PENDING application")
  void createProposal_pendingApplication_throwsIllegalState() {
    Application pendingApp = Application.builder()
        .id(1L)
        .technicianId(20L)
        .message("I can fix this")
        .status(ApplicationStatus.PENDING)
        .listing(listing)
        .build();

    when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApp));

    assertThatThrownBy(() -> proposalService.createProposal(request))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("current application state");

    verify(proposalRepository, never()).save(any());
  }

  @Test
  @DisplayName("createProposal() throws IllegalStateException when a PENDING proposal already exists")
  void createProposal_pendingProposalExists_throwsIllegalState() {
    when(applicationRepository.findById(1L)).thenReturn(Optional.of(acceptedApplication));
    when(proposalRepository.existsByApplicationIdAndStatus(1L, ProposalStatus.PENDING)).thenReturn(true);

    assertThatThrownBy(() -> proposalService.createProposal(request))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("pending proposal already exists");

    verify(proposalRepository, never()).save(any());
  }

  @Test
  @DisplayName("createProposal() transitions application to VISIT_PROPOSED")
  void createProposal_transitionsApplicationToVisitProposed() {
    when(applicationRepository.findById(1L)).thenReturn(Optional.of(acceptedApplication));
    when(proposalRepository.existsByApplicationIdAndStatus(any(), any())).thenReturn(false);
    when(proposalRepository.save(any())).thenReturn(pendingProposal);
    when(proposalMapper.toProposalResponse(any())).thenReturn(response);

    proposalService.createProposal(request);

    assertThat(acceptedApplication.getStatus()).isEqualTo(ApplicationStatus.VISIT_PROPOSED);
    verify(applicationRepository).save(acceptedApplication);
  }

  // --- ACCEPT PROPOSAL ---

  @Test
  @DisplayName("acceptProposal() transitions proposal to ACCEPTED for correct customer")
  void acceptProposal_correctCustomer_success() {
    when(proposalRepository.findById(1L)).thenReturn(Optional.of(pendingProposal));
    when(proposalRepository.save(any())).thenReturn(pendingProposal);
    when(proposalMapper.toProposalResponse(any())).thenReturn(response);

    ProposalResponse result = proposalService.acceptProposal(1L, 10L); // customerId = 10L matches listing

    assertThat(result).isNotNull();
    assertThat(pendingProposal.getStatus()).isEqualTo(ProposalStatus.ACCEPTED);
  }

  @Test
  @DisplayName("acceptProposal() throws ProposalNotFoundException for unknown proposal")
  void acceptProposal_unknownProposal_throwsProposalNotFound() {
    when(proposalRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> proposalService.acceptProposal(99L, 10L))
        .isInstanceOf(ProposalNotFoundException.class);
  }

  @Test
  @DisplayName("acceptProposal() throws AccessDeniedException for wrong customer")
  void acceptProposal_wrongCustomer_throwsAccessDenied() {
    when(proposalRepository.findById(1L)).thenReturn(Optional.of(pendingProposal));

    assertThatThrownBy(() -> proposalService.acceptProposal(1L, 99L)) // wrong customerId
        .isInstanceOf(AccessDeniedException.class);

    verify(proposalRepository, never()).save(any());
  }

  // --- REJECT PROPOSAL ---

  @Test
  @DisplayName("rejectProposal() transitions proposal to REJECTED and application back to ACCEPTED")
  void rejectProposal_correctCustomer_success() {
    when(proposalRepository.findById(1L)).thenReturn(Optional.of(pendingProposal));
    when(proposalRepository.save(any())).thenReturn(pendingProposal);
    when(proposalMapper.toProposalResponse(any())).thenReturn(response);

    proposalService.rejectProposal(1L, 10L);

    assertThat(pendingProposal.getStatus()).isEqualTo(ProposalStatus.REJECTED);
    assertThat(acceptedApplication.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED); // unlocked
  }

  @Test
  @DisplayName("rejectProposal() throws AccessDeniedException for wrong customer")
  void rejectProposal_wrongCustomer_throwsAccessDenied() {
    when(proposalRepository.findById(1L)).thenReturn(Optional.of(pendingProposal));

    assertThatThrownBy(() -> proposalService.rejectProposal(1L, 99L))
        .isInstanceOf(AccessDeniedException.class);

    verify(proposalRepository, never()).save(any());
  }

  @Test
  @DisplayName("rejectProposal() throws ProposalNotFoundException for unknown proposal")
  void rejectProposal_unknownProposal_throwsNotFound() {
    when(proposalRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> proposalService.rejectProposal(99L, 10L))
        .isInstanceOf(ProposalNotFoundException.class);
  }

  // --- GET LATEST PROPOSAL ---

  @Test
  @DisplayName("getLatestProposalByApplicationId() returns most recent proposal")
  void getLatestProposalByApplicationId_returnsProposal() {
    when(proposalRepository.findTopByApplicationIdOrderById(1L))
        .thenReturn(Optional.of(pendingProposal));
    when(proposalMapper.toProposalResponse(pendingProposal)).thenReturn(response);

    ProposalResponse result = proposalService.getLatestProposalByApplicationId(1L);

    assertThat(result.id()).isEqualTo(1L);
  }

  @Test
  @DisplayName("getLatestProposalByApplicationId() throws ProposalNotFoundException when none exist")
  void getLatestProposalByApplicationId_noProposals_throwsNotFound() {
    when(proposalRepository.findTopByApplicationIdOrderById(99L))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> proposalService.getLatestProposalByApplicationId(99L))
        .isInstanceOf(ProposalNotFoundException.class);
  }
}