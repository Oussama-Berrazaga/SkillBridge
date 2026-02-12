package com.skillbridge.application;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

  private final ApplicationService applicationService;

  @GetMapping
  public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
    return ResponseEntity.ok(applicationService.getAllApplications());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable @NotNull Long id) {
    return ResponseEntity.ok(applicationService.getApplicationById(id));
  }

  @PostMapping("/apply")
  public ResponseEntity<ApplicationResponse> apply(@RequestBody @Valid ApplicationRequest request) {
    return ResponseEntity.ok(applicationService.apply(request));
  }
}
