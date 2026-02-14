package com.skillbridge.proposal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProposalResponse(
    Long id,
    Long applicationId,
    LocalDateTime proposedTime,
    BigDecimal visitFee) {
}
