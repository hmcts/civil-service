package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HmctsServiceId {
    UNSPEC_SERVICE_ID("AAA6"),
    SPEC_SERVICE_ID("AAA7");

    private final String serviceId;
}
