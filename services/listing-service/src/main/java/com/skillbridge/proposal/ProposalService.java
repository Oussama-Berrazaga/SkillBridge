package com.skillbridge.proposal;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.application.Application;
import com.skillbridge.application.ApplicationRepository;
import com.skillbridge.application.ApplicationStatus;
import com.skillbridge.exception.ApplicationNotFoundException;

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
}
