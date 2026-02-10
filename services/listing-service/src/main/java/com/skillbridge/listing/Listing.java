package com.skillbridge.listing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.skillbridge.application.Application;
import com.skillbridge.category.Category;
import com.skillbridge.common.Status;

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

  @Enumerated(EnumType.STRING)
  private Status status; // DRAFT, ACTIVE, FILLED, etc.

  // ID reference to the User-Service
  private Long customerId;

  @Builder.Default
  @ManyToMany
  @JoinTable(name = "listings_categories", joinColumns = @JoinColumn(name = "listing_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
  private Set<Category> categories = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Application> applications = new ArrayList<>();
}