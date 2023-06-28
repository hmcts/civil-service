package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;

@Data
@Builder(toBuilder = true)
public class CourtLocation {

    private final String applicantPreferredCourt;
    private final DynamicList applicantPreferredCourtLocationList;
    private final CaseLocationCivil caseLocation;
    private final String reasonForHearingAtSpecificCourt;
    private final YesOrNo remoteHearingRequested;
    private final String reasonForRemoteHearing;

    @JsonCreator
    CourtLocation(@JsonProperty("applicantPreferredCourt") String applicantPreferredCourt,
                  @JsonProperty("applicantPreferredCourtLocationList") DynamicList applicantPreferredCourtLocationList,
                  @JsonProperty("caseLocation") CaseLocationCivil caseLocation,
                  @JsonProperty("reasonForHearingAtSpecificCourt") String reasonForHearingAtSpecificCourt,
                  @JsonProperty("remoteHearingRequested") YesOrNo remoteHearingRequested,
                  @JsonProperty("reasonForRemoteHearing") String reasonForRemoteHearing) {
        this.applicantPreferredCourt = applicantPreferredCourt;
        this.applicantPreferredCourtLocationList = applicantPreferredCourtLocationList;
        this.caseLocation = caseLocation;
        this.reasonForHearingAtSpecificCourt = reasonForHearingAtSpecificCourt;
        this.remoteHearingRequested = remoteHearingRequested;
        this.reasonForRemoteHearing = reasonForRemoteHearing;
    }
}
