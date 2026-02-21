package com.skillbridge.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.skillbridge.intervention.InterventionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterventionEventListener {

  private final InterventionService interventionService;

  @KafkaListener(topics = "proposal-paid-topic", groupId = "intervention-group")
  public void onProposalPaid(ProposalPaidEvent event) {
    log.info("📩 Kafka Event Received: Creating intervention for Proposal ID {}", event.proposalId());
    try {
      interventionService.createIntervention(event);
    } catch (Exception e) {
      log.error("❌ Failed to process intervention for proposal {}: {}", event.proposalId(), e.getMessage());
      // Senior tip: In production, you'd send this to a 'dead-letter-topic'
    }
  }
}
