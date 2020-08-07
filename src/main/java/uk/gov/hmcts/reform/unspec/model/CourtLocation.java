package uk.gov.hmcts.reform.unspec.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourtLocation {

    private final String applicantPreferredCourt;

    @JsonCreator
    CourtLocation(@JsonProperty("applicantPreferredCourt") String applicantPreferredCourt) {
        this.applicantPreferredCourt = applicantPreferredCourt;
    }
}
