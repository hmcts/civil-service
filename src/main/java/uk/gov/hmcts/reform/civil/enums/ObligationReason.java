package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ObligationReason {
    UNLESS_ORDER("Unless order"),
    STAY_A_CASE("Stay a case"),
    LIFT_A_STAY("Lift a stay"),
    DISMISS_CASE("Dismiss case"),
    PRE_TRIAL_CHECKLIST("Pre trial checklist"),
    GENERAL_ORDER("General order"),
    RESERVE_JUDGMENT("Reserve Judgment"),
    OTHER("Other");

    private final String displayedValue;
}
