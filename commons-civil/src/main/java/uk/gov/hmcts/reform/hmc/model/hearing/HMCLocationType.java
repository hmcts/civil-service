package uk.gov.hmcts.reform.hmc.model.hearing;

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
