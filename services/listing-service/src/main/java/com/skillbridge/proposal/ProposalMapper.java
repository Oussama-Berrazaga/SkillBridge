package com.skillbridge.proposal;

public class ProposalMapper {

  public ProposalResponse toProposalResponse(Proposal proposal) {
    return new ProposalResponse(
        proposal.getId(),
        proposal.getApplication().getId(),
        proposal.getProposedTime(),
        proposal.getVisitFee());
  }
}
