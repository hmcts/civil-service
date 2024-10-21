package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DebtPaymentOptions {
    UPLOAD_EVIDENCE_DEBT_PAID_IN_FULL,
    MADE_FULL_PAYMENT_TO_COURT,
    UNABLE_TO_PROVIDE_EVIDENCE_OF_FULL_PAYMENT;
}
