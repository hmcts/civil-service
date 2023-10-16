package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;

@Data
@Builder(toBuilder = true)
public class CourtLocation {

    private final String applicantPreferredCourt;
    private final DynamicList applicantPreferredCourtLocationList;
    private final CaseLocationCivil caseLocation;
    private final String reasonForHearingAtSpecificCourt;

    @JsonCreator
    CourtLocation(@JsonProperty("applicantPreferredCourt") String applicantPreferredCourt,
                  @JsonProperty("applicantPreferredCourtLocationList") DynamicList applicantPreferredCourtLocationList,
                  @JsonProperty("caseLocation") CaseLocationCivil caseLocation,
                  @JsonProperty("reasonForHearingAtSpecificCourt") String reasonForHearingAtSpecificCourt) {
        this.applicantPreferredCourt = applicantPreferredCourt;
        this.applicantPreferredCourtLocationList = applicantPreferredCourtLocationList;
        this.caseLocation = caseLocation;
        this.reasonForHearingAtSpecificCourt = reasonForHearingAtSpecificCourt;
    }
}
