package uk.gov.hmcts.reform.civil.service;

import lombok.Getter;

@Getter
public enum InitiateGeneralApplicationServiceConstants {

    GA_DOC_CATEGORY_ID("applications"),
    INVALID_UNAVAILABILITY_RANGE("Unavailability Date From cannot be after Unavailability Date to. Please enter valid range."),
    INVALID_SETTLE_BY_CONSENT("Settle by consent must have been agreed with the respondent before raising the application"),
    MULTI_CLAIM_TRACK(" - Multi Track"),
    INTERMEDIATE_CLAIM_TRACK(" - Intermediate Track"),
    SMALL_CLAIM_TRACK(" - Small Claims"),
    FAST_CLAIM_TRACK(" - Fast Track");

    private final String value;

    InitiateGeneralApplicationServiceConstants(String value) {
        this.value = value;
    }

}
