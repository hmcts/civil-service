package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HearingMethod {
    IN_PERSON("In Person"),
    TELEPHONE("Telephone"),
    VIDEO("Video"),
    NOT_IN_ATTENDANCE("Not in Attendance");

    private final String label;
}
