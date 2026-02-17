package com.skillbridge.proposal;

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
  private final ProposalMapper proposalMapper;

  @Transactional
  public ProposalResponse createProposal(ProposalRequest request) {
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
  public ProposalResponse acceptProposal(Long proposalId, Long customerId) {
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

    return proposalMapper.toProposalResponse(proposalRepository.save(proposal));
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
}
