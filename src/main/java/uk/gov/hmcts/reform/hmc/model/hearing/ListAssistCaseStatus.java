package uk.gov.hmcts.reform.hmc.model.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ListAssistCaseStatus {

    CASE_CREATED("Case Created"),
    AWAITING_LISTING("Awaiting Listing"),
    LISTED("Listed"),
    PENDING_RELISTING("Pending Relisting"),
    HEARING_COMPLETED("Hearing Completed"),
    CASE_CLOSED("Case Closed"),
    CLOSED("Closed");

    private final String label;
}
