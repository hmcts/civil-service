package uk.gov.hmcts.reform.civil.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentMethod {
    CREDIT_CARD("Credit card"),
    CHEQUE("Cheque"),
    BACS("BACS"),
    OTHER(null);

    @Getter
    private final String humanFriendly;
}
