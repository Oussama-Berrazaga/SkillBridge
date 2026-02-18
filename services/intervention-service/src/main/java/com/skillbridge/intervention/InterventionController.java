package com.skillbridge.intervention;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/interventions")
public class InterventionController {

  private final InterventionService interventionService;

  @GetMapping("/my-interventions")
  public ResponseEntity<List<InterventionResponse>> getMyInterventions(Long technicianId) {
    return ResponseEntity.ok(interventionService.getInterventionsByTechnicianId(technicianId));
  }
}