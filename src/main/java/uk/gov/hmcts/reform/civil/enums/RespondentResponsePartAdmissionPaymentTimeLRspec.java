package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RespondentResponsePartAdmissionPaymentTimeLRspec {
    IMMEDIATELY("Immediately"),
    BY_SET_DATE("By a set date"),
    SUGGESTION_OF_REPAYMENT_PLAN("I'll suggest a repayment plan for my client");

    private final String displayedValue;
}
