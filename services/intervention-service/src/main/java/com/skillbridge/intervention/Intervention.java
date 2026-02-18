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

  @Enumerated(EnumType.STRING)
  private InterventionStatus status;

  // Auditing
  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(insertable = false)
  private LocalDateTime updatedAt;

  // Lifecycle methods to handle the "In Progress" states
  public void startIntervention() {
    if (this.status != InterventionStatus.PLANNED) {
      throw new IllegalStateException("Can only start a planned intervention");
    }
    this.status = InterventionStatus.IN_PROGRESS;
  }
}
