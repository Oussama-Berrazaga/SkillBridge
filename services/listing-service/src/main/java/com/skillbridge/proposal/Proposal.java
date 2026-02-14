package com.skillbridge.proposal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.skillbridge.application.Application;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proposals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Proposal {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private Application application;

  private LocalDateTime proposedTime;
  private BigDecimal visitFee;

  @Builder.Default
  @Setter(AccessLevel.NONE)
  @Enumerated(EnumType.STRING)
  private ProposalStatus status = ProposalStatus.PENDING; // PENDING, ACCEPTED, REJECTED, CANCELLED

  @Version
  private Integer version;

  public void transitionTo(ProposalStatus nextStatus) {
    if (!this.status.canTransitionTo(nextStatus)) {
      throw new IllegalStateException(
          String.format("Invalid transition from %s to %s", this.status, nextStatus));
    }
    this.status = nextStatus;
  }
}