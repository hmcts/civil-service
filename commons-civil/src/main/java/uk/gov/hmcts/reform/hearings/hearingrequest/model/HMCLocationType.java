package uk.gov.hmcts.reform.hearings.hearingrequest.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HMCLocationType {
    COURT("court"),
    CLUSTER("cluster"),
    REGION("region");

    @JsonValue
    private final String locationLabel;
}
