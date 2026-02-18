package com.skillbridge.intervention;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

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

  public InterventionResponse createIntervention(InterventionRequest request) {
    Intervention intervention = Intervention.builder()
        .title(request.title())
        .proposalId(request.proposalId())
        .listingId(request.listingId())
        .technicianId(request.technicianId())
        .clientId(request.clientId())
        .finalPrice(request.finalPrice())
        .scheduledTime(request.scheduledTime())
        .status(InterventionStatus.PLANNED)
        .build();
    Intervention saved = interventionRepository.save(intervention);
    return interventionMapper.toInterventionResponse(saved);
  }
}
