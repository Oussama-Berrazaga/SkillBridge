package com.skillbridge.listing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ListingStatus State Machine Tests")
class ListingStatusTest {

  @Test
  @DisplayName("DRAFT can transition to ACTIVE")
  void draft_canTransitionTo_active() {
    assertThat(ListingStatus.DRAFT.canTransitionTo(ListingStatus.ACTIVE)).isTrue();
  }

  @Test
  @DisplayName("DRAFT can transition to ARCHIVED")
  void draft_canTransitionTo_archived() {
    assertThat(ListingStatus.DRAFT.canTransitionTo(ListingStatus.ARCHIVED)).isTrue();
  }

  @Test
  @DisplayName("DRAFT cannot transition to ASSIGNED")
  void draft_cannotTransitionTo_assigned() {
    assertThat(ListingStatus.DRAFT.canTransitionTo(ListingStatus.ASSIGNED)).isFalse();
  }

  @Test
  @DisplayName("ACTIVE can transition to ASSIGNED")
  void active_canTransitionTo_assigned() {
    assertThat(ListingStatus.ACTIVE.canTransitionTo(ListingStatus.ASSIGNED)).isTrue();
  }

  @Test
  @DisplayName("ACTIVE can transition to ARCHIVED")
  void active_canTransitionTo_archived() {
    assertThat(ListingStatus.ACTIVE.canTransitionTo(ListingStatus.ARCHIVED)).isTrue();
  }

  @Test
  @DisplayName("ACTIVE cannot transition to DRAFT")
  void active_cannotTransitionTo_draft() {
    assertThat(ListingStatus.ACTIVE.canTransitionTo(ListingStatus.DRAFT)).isFalse();
  }

  @Test
  @DisplayName("ASSIGNED can transition to COMPLETED")
  void assigned_canTransitionTo_completed() {
    assertThat(ListingStatus.ASSIGNED.canTransitionTo(ListingStatus.COMPLETED)).isTrue();
  }

  @Test
  @DisplayName("ASSIGNED can re-open to ACTIVE")
  void assigned_canTransitionTo_active() {
    assertThat(ListingStatus.ASSIGNED.canTransitionTo(ListingStatus.ACTIVE)).isTrue();
  }

  @Test
  @DisplayName("COMPLETED is a terminal state")
  void completed_isTerminal() {
    for (ListingStatus next : ListingStatus.values()) {
      assertThat(ListingStatus.COMPLETED.canTransitionTo(next)).isFalse();
    }
  }

  @Test
  @DisplayName("ARCHIVED is a terminal state")
  void archived_isTerminal() {
    for (ListingStatus next : ListingStatus.values()) {
      assertThat(ListingStatus.ARCHIVED.canTransitionTo(next)).isFalse();
    }
  }
}