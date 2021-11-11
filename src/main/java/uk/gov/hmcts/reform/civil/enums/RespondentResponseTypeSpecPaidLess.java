package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RespondentResponseTypeSpecPaidLess {

    PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT(" "),
    PAID_LESS_THAN_CLAIMED_AMOUNT(" ");

    private final String displayedValue;
}
