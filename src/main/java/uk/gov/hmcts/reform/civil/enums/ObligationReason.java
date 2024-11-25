package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ObligationReason {
    UNLESS_ORDER,
    STAY_A_CASE,
    LIFT_A_STAY,
    DISMISS_CASE,
    PRE_TRIAL_CHECKLIST,
    GENERAL_ORDER,
    RESERVE_JUDGMENT,
    OTHER
}
