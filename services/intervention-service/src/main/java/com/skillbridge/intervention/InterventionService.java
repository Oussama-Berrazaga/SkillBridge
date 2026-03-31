package com.skillbridge.intervention;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.kafka.clients.ClientResponse;
import org.springframework.stereotype.Service;

import com.skillbridge.intervention.AgendaItemResponse.ClientDTO;
import com.skillbridge.kafka.ProposalPaidEvent;
import com.skillbridge.user.UserResponse;
import com.skillbridge.user.UserServiceClient;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class InterventionService {

  private final InterventionRepository interventionRepository;
  private final InterventionMapper interventionMapper;
  private final UserServiceClient userServiceClient;

  public List<InterventionResponse> getInterventionsByTechnicianId(Long technicianId) {
    List<Intervention> interventions = interventionRepository.findByTechnicianId(technicianId);
    return interventions.stream()
        .map(interventionMapper::toInterventionResponse)
        .collect(Collectors.toList());
  }

  public InterventionResponse getInterventionById(Long id) {
    Intervention intervention = interventionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Intervention not found with id: " + id));
    return interventionMapper.toInterventionResponse(intervention);
  }

  public InterventionResponse updateInterventionStatus(Long id, InterventionStatus newStatus, Long userId,
      String role) {

    Intervention intervention = interventionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Intervention not found with id: " + id));
    // check if the user is authorized to update the intervention
    boolean isAuthorized = false;
    if (role.equals("TECHNICIAN") && intervention.getTechnicianId().equals(userId)) {
      isAuthorized = true;
    } else if (role.equals("CLIENT") && intervention.getClientId().equals(userId)) {
      isAuthorized = true;
    } else if (role.equals("ADMIN") || role.equals("SUPPORT")) {
      isAuthorized = true;
    }

    if (!isAuthorized) {
      throw new RuntimeException("Unauthorized to update this intervention");
    }

    intervention.transitionTo(newStatus);
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

  public List<AgendaItemResponse> findInterventionByTechId(Long techId) {
    List<Intervention> interventions = interventionRepository.findAllByTechnicianIdOrderByScheduledTimeAsc(techId);

    List<AgendaItemResponse> agenda = interventions.stream().map(intervention -> {
      UserResponse client = userServiceClient.getUserById(intervention.getClientId());
      ClientDTO clientDTO = new AgendaItemResponse.ClientDTO(
          client.id(), client.profile().firstName() + " " + client.profile().lastName(),
          client.profile().phoneNumber());

      AgendaItemResponse agendaItemResponse = new AgendaItemResponse(
          intervention.getId(),
          intervention.getStatus(),
          intervention.getScheduledTime(),
          intervention.getTitle(),
          clientDTO,
          intervention.getLocation());
      return agendaItemResponse;
    }).toList();

    return agenda;
  }

  public InterventionResponse startJob(Long id, Long userId, String role) {
    return updateInterventionStatus(id, InterventionStatus.IN_PROGRESS, userId, role);
  }

  public InterventionResponse completeJob(Long id, Long userId, String role) {
    return updateInterventionStatus(id, InterventionStatus.COMPLETED, userId, role);
  }

}
