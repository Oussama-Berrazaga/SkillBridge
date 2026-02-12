package com.skillbridge.listing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.skillbridge.application.Application;
import com.skillbridge.category.Category;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "listings")
public class Listing {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Setter(AccessLevel.NONE)
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private ListingStatus status = ListingStatus.DRAFT; // DRAFT, ACTIVE, CLOSED

  // ID reference to the User-Service
  private Long customerId;

  @Builder.Default
  @ManyToMany
  @JoinTable(name = "listings_categories", joinColumns = @JoinColumn(name = "listing_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
  private Set<Category> categories = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Application> applications = new ArrayList<>();

  // We use a custom method instead of a standard setter
  public void transitionTo(ListingStatus nextStatus) {
    if (!this.status.canTransitionTo(nextStatus)) {
      throw new IllegalStateException(
          String.format("Cannot transition Listing from %s to %s", this.status, nextStatus));
    }
    this.status = nextStatus;
  }
}