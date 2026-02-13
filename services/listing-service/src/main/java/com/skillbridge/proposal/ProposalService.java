package com.skillbridge.proposal;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.application.Application;
import com.skillbridge.application.ApplicationRepository;
import com.skillbridge.application.ApplicationStatus;
import com.skillbridge.exception.AccessDeniedException;
import com.skillbridge.exception.ApplicationNotFoundException;
import com.skillbridge.exception.ProposalNotFoundException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProposalService {

  private final ProposalRepository proposalRepository;
  private final ApplicationRepository applicationRepository;

  @Transactional
  public void createProposal(Long applicationId, Double fee, LocalDateTime date) {
    Application application = applicationRepository.findById(applicationId)
        .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));

    // Guard: Only allow proposals if chat is open or a previous proposal was
    // rejected
    if (application.getStatus() != ApplicationStatus.ACCEPTED &&
        application.getStatus() != ApplicationStatus.VISIT_PROPOSED) {
      throw new IllegalStateException("Cannot propose a visit in current application state");
    }

    // Guard: Ensure no other PENDING proposal exists
    if (proposalRepository.existsByApplicationIdAndStatus(applicationId, ProposalStatus.PENDING)) {
      throw new IllegalStateException("A pending proposal already exists.");
    }

    Proposal proposal = Proposal.builder()
        .application(application)
        .visitFee(fee)
        .proposedTime(date)
        // status defaults to PENDING via @Builder.Default
        .build();

    // Rich Domain: Update the parent's state
    application.transitionTo(ApplicationStatus.VISIT_PROPOSED);

    proposalRepository.save(proposal);
    // applicationRepository.save(application); // Often implicit via @Transactional
  }

  @Transactional
  public void acceptProposal(Long proposalId, Long customerId) {
    Proposal proposal = proposalRepository.findById(proposalId)
        .orElseThrow(() -> new ProposalNotFoundException("Proposal not found"));

    // Guard: Only the owner of the listing can accept the proposal
    if (!proposal.getApplication().getListing().getCustomerId().equals(customerId)) {
      throw new AccessDeniedException("You are not authorized to accept this proposal");
    }

    // Rich Domain transition
    proposal.transitionTo(ProposalStatus.ACCEPTED);

    // Parent update: The application is now "Locked" for payment
    proposal.getApplication().transitionTo(ApplicationStatus.PROPOSAL_ACCEPTED);

    proposalRepository.save(proposal);
  }

  @Transactional
  public void rejectProposal(Long proposalId, Long customerId) {
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

    proposalRepository.save(proposal);
  }
}
