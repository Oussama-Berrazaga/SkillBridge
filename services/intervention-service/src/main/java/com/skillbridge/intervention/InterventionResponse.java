package com.skillbridge.intervention;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InterventionResponse(
    Long id,
    String title,
    Long proposalId,
    Long listingId,
    Long technicianId,
    Long clientId,
    BigDecimal finalPrice,
    LocalDateTime scheduledTime,
    InterventionStatus status) {

}
