package com.skillbridge.intervention;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
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

  @GetMapping
  public ResponseEntity<List<InterventionResponse>> getMyInterventions(@RequestHeader("X-User-Id") Long userId,
      @RequestHeader("X-User-Role") String role) {

    if (role.equals("TECHNICIAN")) {
      return ResponseEntity.ok(interventionService.getInterventionsByTechnicianId(userId));
    } else if (role.equals("CLIENT")) {
      return ResponseEntity.ok(interventionService.getInterventionsByClientId(userId));
    } else if (role.equals("ADMIN") || role.equals("SUPPORT")) {
      return ResponseEntity.ok(interventionService.getAllInterventions());
    } else {
      return ResponseEntity.status(403).build();
    }

  }

  @GetMapping("/technician/{techId}")
  public ResponseEntity<List<AgendaItemResponse>> getTechnicianAgenda(@PathVariable Long techId) {
    log.info("Fetching agenda for technician: {}", techId);
    return ResponseEntity.ok(interventionService.findInterventionByTechId(techId));
  }

  @PatchMapping("/{id}/start")
  public ResponseEntity<InterventionResponse> startIntervention(@PathVariable Long id,
      @RequestHeader("X-User-Id") Long userId,
      @RequestHeader("X-User-Role") String role) {
    return ResponseEntity.ok(interventionService.startJob(id, userId, role));
  }

  @PatchMapping("/{id}/complete")
  public ResponseEntity<InterventionResponse> completeIntervention(@PathVariable Long id,
      @RequestHeader("X-User-Id") Long userId,
      @RequestHeader("X-User-Role") String role) {
    return ResponseEntity.ok(interventionService.completeJob(id, userId, role));
  }

}