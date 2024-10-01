package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DebtPaymentOptions {
    UPLOAD_EVIDENCE("I want to upload evidence that this debt has been paid in full"),
    MADE_FULL_PAYMENT("I made full payment to the court"),
    NO_EVIDENCE("I am unable to provide evidence of full payment");

    private final String label;
}
