package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum ObligationReason {
    @CCD(label = "Unless order")
    UNLESS_ORDER("Unless order"),
    @CCD(label = "Stay a case")
    STAY_A_CASE("Stay a case"),
    @CCD(label = "Lift a stay")
    LIFT_A_STAY("Lift a stay"),
    @CCD(label = "Dismiss case")
    DISMISS_CASE("Dismiss case"),
    @CCD(label = "Pre-trial checklist")
    PRE_TRIAL_CHECKLIST("Pre trial checklist"),
    @CCD(label = "General order")
    GENERAL_ORDER("General order"),
    @CCD(label = "Reserve judgment")
    RESERVE_JUDGMENT("Reserve Judgment"),
    @CCD(label = "Other")
    OTHER("Other");

    private final String displayedValue;
}
