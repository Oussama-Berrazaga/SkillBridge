package com.skillbridge.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ListingEventProducer {

  private final KafkaTemplate<String, ProposalPaidEvent> kafkaTemplate;

  // Define your topic name as a constant to avoid typos
  private static final String TOPIC = "proposal-paid-topic";

  public void sendProposalPaidEvent(ProposalPaidEvent event) {
    log.info("Publishing Payment Success Event for Proposal: {}", event.proposalId());

    // Using proposalId as the key ensures all messages for this proposal
    // go to the same Kafka partition (order preservation)
    kafkaTemplate.send(TOPIC, event.proposalId().toString(), event);
  }
}