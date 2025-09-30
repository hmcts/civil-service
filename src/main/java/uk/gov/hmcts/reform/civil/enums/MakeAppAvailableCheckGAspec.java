package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MakeAppAvailableCheckGAspec {
    CONSENT_AGREEMENT_CHECKBOX("Make application visible to all parties");

    private final String displayedValue;
}
