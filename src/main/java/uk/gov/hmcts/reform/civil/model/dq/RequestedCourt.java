package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RequestedCourt {

    @CCD(
            label = "Do you want to ask for the hearing to be held at a specific court?",
            hint = "If not, it'll be held at the claimant's preferred court.",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo requestHearingAtSpecificCourt;
    @CCD(label = "The claimant legal representative requested the following court location code.", searchable = false)
    private String otherPartyPreferredSite;
    @CCD(
            label = "The code of your preferred County Court hearing centre",
            showCondition = "requestHearingAtSpecificCourt = \"DO NOT SHOW IN UI\"",
            regex = "[0-9]{3}",
            searchable = false
    )
    private String responseCourtCode;
    @CCD(
            label = "Briefly explain your reasons",
            showCondition = "requestHearingAtSpecificCourt = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String reasonForHearingAtSpecificCourt;
    @CCD(
            label = "Preferred court hearing centre",
            showCondition = "requestHearingAtSpecificCourt = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.DynamicList
    )
    private DynamicList responseCourtLocations;
    @CCD(label = " ", showCondition = "requestHearingAtSpecificCourt = \"DO NOT SHOW IN UI\"", searchable = false)
    private CaseLocationCivil caseLocation;
    @CCD(ignore = true)
    private String responseCourtName;

    public RequestedCourt copy() {
        return new RequestedCourt()
            .setRequestHearingAtSpecificCourt(requestHearingAtSpecificCourt)
            .setOtherPartyPreferredSite(otherPartyPreferredSite)
            .setResponseCourtCode(responseCourtCode)
            .setReasonForHearingAtSpecificCourt(reasonForHearingAtSpecificCourt)
            .setResponseCourtLocations(responseCourtLocations)
            .setCaseLocation(caseLocation)
            .setResponseCourtName(responseCourtName);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "The claimant legal representative requested the following court location code. \n ## ${courtLocation.applicantPreferredCourt}",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String applicantRequestedCourt;
  // ==== end synthesised definition-only fields ====
}
