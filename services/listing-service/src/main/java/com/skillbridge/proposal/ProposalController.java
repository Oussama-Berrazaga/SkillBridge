package com.skillbridge.proposal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/proposals")
public class ProposalController {
  private final ProposalService proposalService;

  @GetMapping("/latest/{applicationId}")
  public ResponseEntity<ProposalResponse> getLatestProposalByApplicationId(@PathVariable Long applicationId) {
    return ResponseEntity.ok(proposalService.getLatestProposalByApplicationId(applicationId));
  }

  @PostMapping("/create")
  public ResponseEntity<ProposalResponse> createProposal(@RequestBody @Valid ProposalRequest request) {
    return new ResponseEntity<>(proposalService.createProposal(request), HttpStatus.CREATED);
  }

  @PostMapping("/accept")
  public ResponseEntity<ProposalResponse> acceptProposal(@RequestParam Long proposalId, @RequestParam Long customerId) {
    return ResponseEntity.ok(proposalService.testProposalNotify(proposalId,
        customerId));
  }

  @PostMapping("/reject")
  public ResponseEntity<ProposalResponse> rejectProposal(@RequestParam Long proposalId, @RequestParam Long customerId) {
    return ResponseEntity.ok(proposalService.rejectProposal(proposalId, customerId));
  }
}