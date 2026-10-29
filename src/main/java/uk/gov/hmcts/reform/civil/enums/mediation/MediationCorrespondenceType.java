package uk.gov.hmcts.reform.civil.enums.mediation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediationCorrespondenceType {
    INBOUND_TELEPHONE_CALL("Inbound telephone call"),
    OUTBOUND_TELEPHONE_CALL("Outbound telephone call"),
    INBOUND_EMAIL("Inbound email"),
    OUTBOUND_EMAIL("Outbound email"),
    WEBCHAT("Webchat");

    private final String value;
}
