package com.skillbridge.proposal;

public enum ProposalStatus {
  PENDING, ACCEPTED, PAID, REJECTED, CANCELLED;

  public boolean canTransitionTo(ProposalStatus next) {
    return switch (this) {
      case PENDING -> next == ACCEPTED || next == REJECTED || next == CANCELLED;
      case ACCEPTED -> next == PAID;
      case PAID, REJECTED, CANCELLED -> false; // Terminal states
    };
  }
}
