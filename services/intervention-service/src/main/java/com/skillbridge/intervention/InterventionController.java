package com.skillbridge.intervention;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/interventions")
public class InterventionController {

  private final InterventionService interventionService;

  @GetMapping("/my-interventions")
  public ResponseEntity<List<InterventionResponse>> getMyInterventions(Long technicianId) {
    return ResponseEntity.ok(interventionService.getInterventionsByTechnicianId(technicianId));
  }

  @GetMapping("/technician/{techId}")
  public ResponseEntity<List<InterventionResponse>> getTechnicianAgenda(@PathVariable Long techId) {
    log.info("Fetching agenda for technician: {}", techId);
    return ResponseEntity.ok(interventionService.findInterventionByTechId(techId));
  }
}