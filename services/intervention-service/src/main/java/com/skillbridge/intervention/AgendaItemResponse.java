package com.skillbridge.intervention;

import java.time.LocalDateTime;

public record AgendaItemResponse(
    Long interventionId,
    InterventionStatus status,
    LocalDateTime scheduledTime,
    String listingTitle,
    ClientDTO client,
    String address) {
  public record ClientDTO(
      Long id,
      String fullName,
      String phoneNumber) {
  }
}