package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventAddedEvents {
    DEFENDANT_RESPONSE_EVENT("Defendant Response Event"),
    CLAIMANT_INTENTION_EVENT("Claimant Intention Event"),
    DJ_EVENT("Request DJ Event");

    private final String value;
}
