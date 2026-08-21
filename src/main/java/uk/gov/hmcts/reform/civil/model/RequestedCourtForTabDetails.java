package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RequestedCourtForTabDetails {

    @CCD(label = "The applicant legal representative requested the following court location code.", searchable = false)
    private String requestedCourt;
    @CCD(label = "Court name", searchable = false)
    private String requestedCourtName;
    @CCD(
            label = "Briefly explain your reasons for requesting this Court location.",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String reasonForHearingAtSpecificCourt;
    @CCD(
            label = "Do you want to ask for the hearing to be held remotely?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo requestHearingHeldRemotely;
    @CCD(
            label = "Briefly explain your reasons for having a remote hearing.",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String requestHearingHeldRemotelyReason;

}
