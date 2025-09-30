package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClaimantDefendantNotAttendingType {

    SATISFIED_REASONABLE_TO_PROCEED("Satisfied reasonable to proceed"),
    SATISFIED_NOTICE_OF_TRIAL("Satisfied notice of trial received, not reasonable to proceed"),
    NOT_SATISFIED_NOTICE_OF_TRIAL("Not satisfied notice of trial received, not reasonable to proceed"),
    NOT_GIVEN_NOTICE_OF_APPLICATION("The defendant was not given notice of this application"),
    SATISFIED_REASONABLE_TO_PROCEED_CLAIMANT("Satisfied reasonable to proceed"),
    SATISFIED_NOTICE_OF_TRIAL_CLAIMANT("Satisfied notice of trial received, not reasonable to proceed"),
    NOT_SATISFIED_NOTICE_OF_TRIAL_CLAIMANT("Not satisfied notice of trial received, not reasonable to proceed"),
    NOT_GIVEN_NOTICE_OF_APPLICATION_CLAIMANT("The claimant was not given notice of this application");

    private final String displayedValue;
}
