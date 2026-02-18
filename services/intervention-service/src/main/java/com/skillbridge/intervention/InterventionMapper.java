package com.skillbridge.intervention;

import org.springframework.stereotype.Component;

@Component
public class InterventionMapper {

  public InterventionResponse toInterventionResponse(Intervention intervention) {
    return new InterventionResponse(
        intervention.getId(),
        intervention.getTitle(),
        intervention.getProposalId(),
        intervention.getListingId(),
        intervention.getTechnicianId(),
        intervention.getClientId(),
        intervention.getFinalPrice(),
        intervention.getScheduledTime(),
        intervention.getStatus());
  }
}
