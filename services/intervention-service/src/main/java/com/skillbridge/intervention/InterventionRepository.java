package com.skillbridge.intervention;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterventionRepository extends JpaRepository<Intervention, Long> {

  public List<Intervention> findByTechnicianId(Long technicianId);

  boolean existsByProposalId(Long proposalId);
}
