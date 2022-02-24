package uk.gov.hmcts.reform.civil.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CREDIT_CARD("Credit card"),
    CHEQUE("Cheque"),
    BACS("BACS"),
    OTHER("Other");

    private final String humanFriendly;
}
