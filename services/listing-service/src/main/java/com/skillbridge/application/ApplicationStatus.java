package com.skillbridge.application;

public enum ApplicationStatus {
  PENDING, ACCEPTED, VISIT_PROPOSED, REJECTED, WITHDRAWN, PROPOSAL_ACCEPTED, BOOKED;

  public boolean canTransitionTo(ApplicationStatus next) {
    return switch (this) {
      case PENDING -> next == ACCEPTED || next == REJECTED || next == WITHDRAWN;
      case ACCEPTED -> next == VISIT_PROPOSED || next == WITHDRAWN; // Allow withdrawal after acceptance
      case VISIT_PROPOSED -> next == PROPOSAL_ACCEPTED;
      case PROPOSAL_ACCEPTED -> next == BOOKED;
      case REJECTED, WITHDRAWN, BOOKED -> false; // Terminal states
    };
  }
}
