package com.skillbridge.intervention;

public enum InterventionStatus {
  PLANNED, // Initial state after payment
  IN_PROGRESS, // Tech has arrived and started
  COMPLETED, // Work is done
  CANCELLED, // Should have strict rules (e.g., 24h before)
  DISPUTED; // Client or Tech reported an issue

  public boolean canTransitionTo(InterventionStatus next) {
    return switch (this) {
      case PLANNED -> next == IN_PROGRESS;
      case IN_PROGRESS -> next == COMPLETED || next == CANCELLED || next == DISPUTED;
      case DISPUTED -> next == IN_PROGRESS || next == COMPLETED || next == CANCELLED;
      case COMPLETED, CANCELLED -> false;
    };
  }
}
