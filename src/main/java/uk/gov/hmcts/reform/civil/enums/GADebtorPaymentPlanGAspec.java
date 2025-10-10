package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GADebtorPaymentPlanGAspec {

    INSTALMENT("I will accept the following instalments per month"),
    PAYFULL("I will accept payment in full by a set date");

    private final String displayedValue;
}
