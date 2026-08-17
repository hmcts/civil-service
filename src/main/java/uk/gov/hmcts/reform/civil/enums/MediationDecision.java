package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum MediationDecision {
    @CCD(label = " I have read and understood the above")
    Yes("I have read and understood the above"),
    @CCD(label = "Opt out of mediation")
    No("Opt out of mediation");

    private final String displayedValue;
}
