package com.skillbridge.proposal;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

  boolean existsByApplicationIdAndStatus(Long applicationId, ProposalStatus status);

  Optional<Proposal> findTopByApplicationIdOrderById(Long applicationId);
}
