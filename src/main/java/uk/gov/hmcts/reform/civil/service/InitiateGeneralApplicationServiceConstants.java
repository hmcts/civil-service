package uk.gov.hmcts.reform.civil.service;

import java.util.Arrays;
import java.util.List;

public interface InitiateGeneralApplicationServiceConstants {

    int NUMBER_OF_DEADLINE_DAYS = 5;
    String GA_DOC_CATEGORY_ID = "applications";
    String INVALID_UNAVAILABILITY_RANGE = "Unavailability Date From cannot be after "
        + "Unavailability Date to. Please enter valid range.";
    String INVALID_SETTLE_BY_CONSENT = "Settle by consent " +
        "must have been agreed with the respondent " +
        "before raising the application";
    List<String> lipCaseRole = Arrays.asList("[DEFENDANT]", "[CLAIMANT]");

    String MULTI_CLAIM_TRACK = " - Multi Track";
    String INTERMEDIATE_CLAIM_TRACK = " - Intermediate Track";
    String SMALL_CLAIM_TRACK = " - Small Claims";
    String FAST_CLAIM_TRACK = " - Fast Track";


}
