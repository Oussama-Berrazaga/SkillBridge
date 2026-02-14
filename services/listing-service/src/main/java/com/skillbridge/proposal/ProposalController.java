package com.skillbridge.proposal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/proposals")
public class ProposalController {
  private final ProposalService proposalService;

  public ResponseEntity<ProposalResponse> createProposal(ProposalRequest request) {
    return new ResponseEntity<>(proposalService.createProposal(request), HttpStatus.CREATED);
  }

  public ResponseEntity<ProposalResponse> acceptProposal(Long proposalId, Long customerId) {
    return ResponseEntity.ok(proposalService.acceptProposal(proposalId, customerId));
  }

  public ResponseEntity<ProposalResponse> rejectProposal(Long proposalId, Long customerId) {
    return ResponseEntity.ok(proposalService.rejectProposal(proposalId, customerId));
  }
}
