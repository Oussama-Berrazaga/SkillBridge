package com.skillbridge.intervention;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "interventions")
public class Intervention {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  private LocalDateTime scheduledTime;

  private String location;

  private Long technicianId;

  private Long clientId;

  private Long proposalId;

  private Long listingId;

  private BigDecimal finalPrice;

  @Setter(AccessLevel.NONE)
  @Enumerated(EnumType.STRING)
  private InterventionStatus status;

  // Auditing
  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(insertable = false)
  private LocalDateTime updatedAt;

  private LocalDateTime startedAt;
  private LocalDateTime completedAt;

  public void transitionTo(InterventionStatus nextStatus) {
    if (!this.status.canTransitionTo(nextStatus)) {
      throw new IllegalStateException(
          String.format("Invalid transition from %s to %s", this.status, nextStatus));
    }

    // Logic for automatic timestamps
    if (nextStatus == InterventionStatus.IN_PROGRESS && this.startedAt == null) {
      this.startedAt = LocalDateTime.now();
    } else if (nextStatus == InterventionStatus.COMPLETED) {
      this.completedAt = LocalDateTime.now();
    }

    this.status = nextStatus;
  }
}
