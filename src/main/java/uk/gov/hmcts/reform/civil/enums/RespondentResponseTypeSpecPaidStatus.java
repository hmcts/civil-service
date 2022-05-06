package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RespondentResponseTypeSpecPaidStatus {
    /**
     * if we send null, browser does not clear the current value, so we need a different value
     * to prevent backtracking errors.
     */
    DID_NOT_PAY(" "),
    PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT(" "),
    PAID_LESS_THAN_CLAIMED_AMOUNT(" ");

    private final String displayedValue;
}
