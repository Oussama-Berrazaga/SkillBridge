package com.skillbridge.application;

import com.skillbridge.common.ApplicationStatus;
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

  @Enumerated(EnumType.STRING)
  private ApplicationStatus status; // PENDING, ACCEPTED, REJECTED

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "listing_id")
  private Listing listing;
}
