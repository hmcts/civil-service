package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediationDecision {
    Yes("I have read and understood the above"),
    No("Opt out of mediation");

    private final String displayedValue;
}
