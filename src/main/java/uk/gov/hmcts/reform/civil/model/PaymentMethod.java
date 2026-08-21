package uk.gov.hmcts.reform.civil.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "HowWasThisAmountPaid", generate = true)
@RequiredArgsConstructor
public enum PaymentMethod {
    @CCD(label = "Credit card")
    CREDIT_CARD("Credit card"),
    @CCD(label = "Cheque")
    CHEQUE("Cheque"),
    BACS("BACS"),
    @CCD(label = "Other")
    OTHER(null);

    @Getter
    private final String humanFriendly;
}
