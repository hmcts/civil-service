package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CourtLocation {

    @CCD(label = "Court location", regex = "[0-9]{3}", searchable = false)
    private String applicantPreferredCourt;
    @CCD(
            label = "Please select your preferred court hearing location",
            searchable = false,
            typeOverride = FieldType.DynamicList
    )
    private DynamicList applicantPreferredCourtLocationList;
    @CCD(label = "Case Location", searchable = false)
    private CaseLocationCivil caseLocation;
    @CCD(label = "Briefly explain your reasons", searchable = false, typeOverride = FieldType.TextArea)
    private String reasonForHearingAtSpecificCourt;

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