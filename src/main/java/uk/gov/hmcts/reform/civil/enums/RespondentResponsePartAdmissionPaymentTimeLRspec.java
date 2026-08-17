package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum RespondentResponsePartAdmissionPaymentTimeLRspec {
    @CCD(label = "Immediately")
    IMMEDIATELY("Immediately"),
    @CCD(label = "By a set date")
    BY_SET_DATE("By a set date"),
    @CCD(label = "I'll suggest a repayment plan for my client")
    SUGGESTION_OF_REPAYMENT_PLAN("I'll suggest a repayment plan for my client");

    private final String displayedValue;

    public static final int DAYS_TO_PAY_IMMEDIATELY = 5;
}
