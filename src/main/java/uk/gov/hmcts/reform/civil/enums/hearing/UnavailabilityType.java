package uk.gov.hmcts.reform.civil.enums.hearing;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UnavailabilityType {
    AM("AM"),
    PM("PM"),
    ALL_DAY("All Day");

    @JsonValue
    private final String unavailabilityType;
}
