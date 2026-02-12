package com.skillbridge.listing;

public enum ListingStatus {
  DRAFT,
  ACTIVE,
  ASSIGNED,
  COMPLETED,
  ARCHIVED;

  public boolean canTransitionTo(ListingStatus next) {
    return switch (this) {
      case DRAFT -> next == ACTIVE || next == ARCHIVED;
      case ACTIVE -> next == ASSIGNED || next == ARCHIVED;
      case ASSIGNED -> next == COMPLETED || next == ACTIVE; // Re-open if technician drops out
      case COMPLETED, ARCHIVED -> false; // Terminal states
    };
  }
}