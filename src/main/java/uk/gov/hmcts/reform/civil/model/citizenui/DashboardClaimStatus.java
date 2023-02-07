package uk.gov.hmcts.reform.civil.model.citizenui;

public enum DashboardClaimStatus {
    NO_STATUS,
    NO_RESPONSE,
    RESPONSE_OVERDUE,
    RESPONSE_DUE_NOW,
    ADMIT_PAY_IMMEDIATELY,
    ADMIT_PAY_BY_SET_DATE,
    ADMIT_PAY_INSTALLMENTS,
    ELIGIBLE_FOR_CCJ_AFTER_FULL_ADMIT_PAY_IMMEDIATELY_PAST_DEADLINE,
    ELIGIBLE_FOR_CCJ,
    MORE_TIME_REQUESTED,
    CLAIMANT_ACCEPTED_STATES_PAID,
    PAID_IN_FULL_CCJ_CANCELLED,
    PAID_IN_FULL_CCJ_SATISFIED,
    TRANSFERRED,
    REQUESTED_COUNTRY_COURT_JUDGEMENT
}
