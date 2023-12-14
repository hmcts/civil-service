package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentFrequencyClaimantResponseLRspec {
    ONCE_ONE_WEEK("Every week"),
    ONCE_TWO_WEEKS("Every 2 weeks"),
    ONCE_ONE_MONTH("Every month");

    private final String label;
}
