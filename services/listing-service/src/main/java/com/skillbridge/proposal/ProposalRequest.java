package com.skillbridge.proposal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProposalRequest(

        @NotNull Long applicationId,
        @NotNull @Positive BigDecimal visitFee,

        @NotNull @Future // Ensures the tech can only propose a visit starting from 'now' + 1ms
        LocalDateTime proposedTime) {
}