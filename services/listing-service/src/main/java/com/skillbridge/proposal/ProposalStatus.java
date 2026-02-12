package com.skillbridge.proposal;

public enum ProposalStatus {
  PENDING, ACCEPTED, REJECTED, CANCELLED;

  public boolean canTransitionTo(ProposalStatus next) {
    return switch (this) {
      case PENDING -> next == ACCEPTED || next == REJECTED || next == CANCELLED;
      case ACCEPTED, REJECTED, CANCELLED -> false; // Terminal states
    };
  }
}
