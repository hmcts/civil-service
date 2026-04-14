package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediationDecision {
    YES("I have read and understood the above"),
    NO("Opt out of mediation");

    private final String displayedValue;
}
