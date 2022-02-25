package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GAHearingType {
    IN_PERSON("In person"),
    VIDEO("Video conference hearing"),
    TELEPHONE("Telephone hearing"),
    WITHOUT_HEARING("Without a hearing");

    private final String displayedValue;
}
