package com.skillbridge.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProposalPaidEvent(
        String title,
        Long proposalId,
        Long listingId,
        Long clientId,
        Long technicianId,
        BigDecimal amount,
        LocalDateTime scheduledTime) {
}