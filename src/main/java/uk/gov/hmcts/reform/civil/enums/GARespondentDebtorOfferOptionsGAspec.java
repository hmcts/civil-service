package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GARespondentDebtorOfferOptionsGAspec {

    ACCEPT("I accept the debtor's offer"),
    DECLINE("I DO NOT accept the debtor's offer");

    private final String displayedValue;
}
