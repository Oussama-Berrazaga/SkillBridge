package com.skillbridge.proposal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.skillbridge.application.Application;
import com.skillbridge.application.ApplicationRepository;
import com.skillbridge.application.ApplicationStatus;
import com.skillbridge.exception.AccessDeniedException;
import com.skillbridge.exception.ApplicationNotFoundException;
import com.skillbridge.exception.ProposalNotFoundException;
import com.skillbridge.kafka.ListingEventProducer;
import com.skillbridge.kafka.ProposalPaidEvent;
import com.skillbridge.listing.ListingStatus;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProposalService {

  private final ProposalRepository proposalRepository;
  private final ApplicationRepository applicationRepository;
  private final ProposalMapper proposalMapper;
  private final ListingEventProducer eventProducer;

  @Transactional
  public ProposalResponse createProposal(ProposalRequest request) {
    log.info("Creating proposal for applicationId: {}", request.applicationId());
    Application application = applicationRepository.findById(request.applicationId())
        .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));

    // Guard: Only allow proposals if chat is open or a previous proposal was
    // rejected
    if (application.getStatus() != ApplicationStatus.ACCEPTED &&
        application.getStatus() != ApplicationStatus.VISIT_PROPOSED) {
      throw new IllegalStateException("Cannot propose a visit in current application state");
    }

    // Guard: Ensure no other PENDING proposal exists
    if (proposalRepository.existsByApplicationIdAndStatus(request.applicationId(), ProposalStatus.PENDING)) {
      throw new IllegalStateException("A pending proposal already exists.");
    }

    Proposal proposal = Proposal.builder()
        .application(application)
        .visitFee(request.visitFee())
        .proposedTime(request.proposedTime())
        // status defaults to PENDING via @Builder.Default
        .build();

    // Rich Domain: Update the parent's state
    application.transitionTo(ApplicationStatus.VISIT_PROPOSED);
    var savedProposal = proposalRepository.save(proposal);
    applicationRepository.save(application);
    return proposalMapper.toProposalResponse(savedProposal);
  }

  // this is called after the customer pays the visit fee, so we can be sure the
  // visit is locked in and the technician can't back out
  @Transactional
  public ProposalResponse acceptProposal(@NonNull Long proposalId, Long customerId) {
    Proposal proposal = proposalRepository.findById(proposalId)
        .orElseThrow(() -> new ProposalNotFoundException("Proposal not found"));

    // Guard: Only the owner of the listing can accept the proposal
    if (!proposal.getApplication().getListing().getCustomerId().equals(customerId)) {
      throw new AccessDeniedException("You are not authorized to accept this proposal");
    }

    // Rich Domain transition
    proposal.transitionTo(ProposalStatus.ACCEPTED);
    // TO DO: new flyway migration to add new application status "PROPOSAL_ACCEPTED"
    // and add proposal table foreign key to application
    // Parent update: The application is now "Locked" for payment
    proposal.getApplication().transitionTo(ApplicationStatus.PROPOSAL_ACCEPTED);

    return proposalMapper.toProposalResponse(proposalRepository.save(proposal));
  }

  @Transactional
  public ProposalResponse testProposalNotify(Long proposalId, Long customerId) {
    Proposal proposal = proposalRepository.findById(proposalId)
        .orElseThrow(() -> new ProposalNotFoundException("Proposal not found"));
    // 2. Prepare the Event
    ProposalPaidEvent event = new ProposalPaidEvent(
        proposal.getApplication().getListing().getTitle(),
        proposal.getId(),
        proposal.getApplication().getListing().getId(),
        proposal.getApplication().getListing().getCustomerId(),
        proposal.getApplication().getTechnicianId(),
        proposal.getVisitFee(),
        proposal.getProposedTime());

    // 3. Register Post-Commit Action
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        eventProducer.sendProposalPaidEvent(event);
      }
    });
    return proposalMapper.toProposalResponse(proposal);
  }

  @Transactional
  public void confirmPaymentAndNotify(Long proposalId, Long customerId) {
    // 1. Fetch and Update local state
    Proposal proposal = proposalRepository.findById(proposalId)
        .orElseThrow(() -> new ProposalNotFoundException("Proposal not found"));

    // Guard: Only the owner of the listing can accept the proposal
    if (!proposal.getApplication().getListing().getCustomerId().equals(customerId)) {
      throw new AccessDeniedException("You are not authorized to accept this proposal");
    }

    // Safety check: Don't process if already accepted
    if (proposal.getStatus() == ProposalStatus.ACCEPTED)
      return;

    proposal.transitionTo(ProposalStatus.ACCEPTED);
    proposal.getApplication().getListing().transitionTo(ListingStatus.ASSIGNED);

    // 2. Prepare the Event
    ProposalPaidEvent event = new ProposalPaidEvent(
        proposal.getApplication().getListing().getTitle(),
        proposal.getId(),
        proposal.getApplication().getListing().getId(),
        proposal.getApplication().getListing().getCustomerId(),
        proposal.getApplication().getTechnicianId(),
        proposal.getVisitFee(),
        proposal.getProposedTime());

    // 3. Register Post-Commit Action
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        eventProducer.sendProposalPaidEvent(event);
      }
    });
  }

  @Transactional
  public ProposalResponse rejectProposal(Long proposalId, Long customerId) {
    Proposal proposal = proposalRepository.findById(proposalId)
        .orElseThrow(() -> new ProposalNotFoundException("Proposal not found"));

    // Authorization check
    if (!proposal.getApplication().getListing().getCustomerId().equals(customerId)) {
      throw new AccessDeniedException("Unauthorized");
    }

    // 1. Transition the Proposal to REJECTED
    proposal.transitionTo(ProposalStatus.REJECTED);

    // 2. Transition the Application back to ACCEPTED
    // This "unlocks" the createProposal method for the technician to try again
    proposal.getApplication().transitionTo(ApplicationStatus.ACCEPTED);

    return proposalMapper.toProposalResponse(proposalRepository.save(proposal));
  }

  public ProposalResponse getLatestProposalByApplicationId(Long applicationId) {
    Proposal proposal = proposalRepository.findTopByApplicationIdOrderById(applicationId)
        .orElseThrow(() -> new ProposalNotFoundException("No proposals found for this application"));
    return proposalMapper.toProposalResponse(proposal);
  }
}
