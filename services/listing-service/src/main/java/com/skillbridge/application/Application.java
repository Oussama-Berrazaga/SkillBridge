package com.skillbridge.application;

import com.skillbridge.listing.Listing;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "applications")
public class Application {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // External ID reference to the technician (User-Service)
  private Long technicianId;

  private String message;
  @Setter(AccessLevel.NONE)
  @Enumerated(EnumType.STRING)
  private ApplicationStatus status; // PENDING, ACCEPTED, REJECTED

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "listing_id")
  private Listing listing;

  public void transitionTo(ApplicationStatus nextStatus) {
    if (!this.status.canTransitionTo(nextStatus)) {
      throw new IllegalStateException(
          String.format("Cannot transition Application from %s to %s", this.status, nextStatus));
    }
    this.status = nextStatus;
  }
}
