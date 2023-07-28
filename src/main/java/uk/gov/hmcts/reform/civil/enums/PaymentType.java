package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentType {
    IMMEDIATELY("Immediately"),
    SET_DATE("By a set date"),
    REPAYMENT_PLAN("By repayment plan");

    private final String displayedValue;
}
