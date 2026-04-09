package com.skillbridge.application;

import com.skillbridge.proposal.ProposalStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApplicationStatus & ProposalStatus State Machine Tests")
class StatusStateMachineTest {

  // --- APPLICATION STATUS ---

  @Test
  @DisplayName("PENDING can transition to ACCEPTED")
  void pending_canTransitionTo_accepted() {
    assertThat(ApplicationStatus.PENDING.canTransitionTo(ApplicationStatus.ACCEPTED)).isTrue();
  }

  @Test
  @DisplayName("PENDING can transition to REJECTED")
  void pending_canTransitionTo_rejected() {
    assertThat(ApplicationStatus.PENDING.canTransitionTo(ApplicationStatus.REJECTED)).isTrue();
  }

  @Test
  @DisplayName("PENDING can transition to WITHDRAWN")
  void pending_canTransitionTo_withdrawn() {
    assertThat(ApplicationStatus.PENDING.canTransitionTo(ApplicationStatus.WITHDRAWN)).isTrue();
  }

  @Test
  @DisplayName("ACCEPTED can transition to VISIT_PROPOSED")
  void accepted_canTransitionTo_visitProposed() {
    assertThat(ApplicationStatus.ACCEPTED.canTransitionTo(ApplicationStatus.VISIT_PROPOSED)).isTrue();
  }

  @Test
  @DisplayName("VISIT_PROPOSED can transition to PROPOSAL_ACCEPTED")
  void visitProposed_canTransitionTo_proposalAccepted() {
    assertThat(ApplicationStatus.VISIT_PROPOSED.canTransitionTo(ApplicationStatus.PROPOSAL_ACCEPTED)).isTrue();
  }

  @Test
  @DisplayName("REJECTED is a terminal state")
  void rejected_isTerminal() {
    for (ApplicationStatus next : ApplicationStatus.values()) {
      assertThat(ApplicationStatus.REJECTED.canTransitionTo(next)).isFalse();
    }
  }

  @Test
  @DisplayName("BOOKED is a terminal state")
  void booked_isTerminal() {
    for (ApplicationStatus next : ApplicationStatus.values()) {
      assertThat(ApplicationStatus.BOOKED.canTransitionTo(next)).isFalse();
    }
  }

  @Test
  @DisplayName("PENDING cannot jump directly to VISIT_PROPOSED")
  void pending_cannotSkipTo_visitProposed() {
    assertThat(ApplicationStatus.PENDING.canTransitionTo(ApplicationStatus.VISIT_PROPOSED)).isFalse();
  }

  // --- PROPOSAL STATUS ---

  @Test
  @DisplayName("PENDING proposal can be ACCEPTED")
  void proposalPending_canTransitionTo_accepted() {
    assertThat(ProposalStatus.PENDING.canTransitionTo(ProposalStatus.ACCEPTED)).isTrue();
  }

  @Test
  @DisplayName("PENDING proposal can be REJECTED")
  void proposalPending_canTransitionTo_rejected() {
    assertThat(ProposalStatus.PENDING.canTransitionTo(ProposalStatus.REJECTED)).isTrue();
  }

  @Test
  @DisplayName("PENDING proposal can be CANCELLED")
  void proposalPending_canTransitionTo_cancelled() {
    assertThat(ProposalStatus.PENDING.canTransitionTo(ProposalStatus.CANCELLED)).isTrue();
  }

  @Test
  @DisplayName("ACCEPTED proposal can transition to PAID")
  void proposalAccepted_canTransitionTo_paid() {
    assertThat(ProposalStatus.ACCEPTED.canTransitionTo(ProposalStatus.PAID)).isTrue();
  }

  @Test
  @DisplayName("PAID proposal is a terminal state")
  void proposalPaid_isTerminal() {
    for (ProposalStatus next : ProposalStatus.values()) {
      assertThat(ProposalStatus.PAID.canTransitionTo(next)).isFalse();
    }
  }

  @Test
  @DisplayName("REJECTED proposal is a terminal state")
  void proposalRejected_isTerminal() {
    for (ProposalStatus next : ProposalStatus.values()) {
      assertThat(ProposalStatus.REJECTED.canTransitionTo(next)).isFalse();
    }
  }

  @Test
  @DisplayName("PENDING proposal cannot jump to PAID")
  void proposalPending_cannotSkipTo_paid() {
    assertThat(ProposalStatus.PENDING.canTransitionTo(ProposalStatus.PAID)).isFalse();
  }
}