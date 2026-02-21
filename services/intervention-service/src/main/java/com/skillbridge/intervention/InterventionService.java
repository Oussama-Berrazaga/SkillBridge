package com.skillbridge.intervention;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.skillbridge.kafka.ProposalPaidEvent;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class InterventionService {

  private final InterventionRepository interventionRepository;
  private final InterventionMapper interventionMapper;

  public List<InterventionResponse> getInterventionsByTechnicianId(Long technicianId) {
    List<Intervention> interventions = interventionRepository.findByTechnicianId(technicianId);
    return interventions.stream()
        .map(interventionMapper::toInterventionResponse)
        .collect(Collectors.toList());
  }

  public InterventionResponse getInterventionById(Long id) {
    Intervention intervention = interventionRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Intervention not found with id: " + id));
    return interventionMapper.toInterventionResponse(intervention);
  }

  public InterventionResponse updateInterventionStatus(Long id, InterventionStatus newStatus) {
    Intervention intervention = interventionRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Intervention not found with id: " + id));
    intervention.setStatus(newStatus);
    Intervention updated = interventionRepository.save(intervention);
    return interventionMapper.toInterventionResponse(updated);
  }

  public void deleteIntervention(Long id) {
    if (!interventionRepository.existsById(id)) {
      throw new RuntimeException("Intervention not found with id: " + id);
    }
    interventionRepository.deleteById(id);
  }

  // public InterventionResponse createIntervention(ProposalPaidEvent event) {
  // Intervention intervention = Intervention.builder()
  // .title(event.title())
  // .proposalId(event.proposalId())
  // .listingId(event.listingId())
  // .technicianId(event.technicianId())
  // .clientId(event.clientId())
  // .finalPrice(event.amount())
  // .scheduledTime(event.scheduledTime())
  // .status(InterventionStatus.PLANNED)
  // .build();
  // Intervention saved = interventionRepository.save(intervention);
  // return interventionMapper.toInterventionResponse(saved);
  // }

  @Transactional
  public void createIntervention(ProposalPaidEvent event) {
    // 1. Idempotency Check
    if (interventionRepository.existsByProposalId(event.proposalId())) {
      log.warn("⚠️ Intervention already exists for proposal {}. Skipping.", event.proposalId());
      return;
    }

    // 2. Map Event to Entity
    Intervention intervention = Intervention.builder()
        .title(event.title())
        .location(event.address())
        .proposalId(event.proposalId())
        .listingId(event.listingId())
        .technicianId(event.technicianId())
        .clientId(event.clientId())
        .finalPrice(event.amount())
        .scheduledTime(event.scheduledTime())
        .status(InterventionStatus.PLANNED)
        .createdAt(LocalDateTime.now())
        .build();

    // 3. Save
    interventionRepository.save(intervention);
    log.info("✅ Intervention successfully created for Technician ID {}", event.technicianId());
  }
}
