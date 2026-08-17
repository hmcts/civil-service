package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum RespondentResponseTypeSpecPaidStatus {
    /**
     * if we send null, browser does not clear the current value, so we need a different value
     * to prevent backtracking errors.
     */
    @CCD(
            label = "This should be an empty string or Hidden. CCD requires a string to be displayed. So we cannot use this variable as a logic workaround"
    )
    DID_NOT_PAY(" "),
    @CCD(
            label = "This should be an empty string or Hidden. CCD requires a string to be displayed. So we cannot use this variable as a logic workaround"
    )
    PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT(" "),
    @CCD(
            label = "This should be an empty string or Hidden. CCD requires a string to be displayed. So we cannot use this variable as a logic workaround"
    )
    PAID_LESS_THAN_CLAIMED_AMOUNT(" ");

    private final String displayedValue;
}
