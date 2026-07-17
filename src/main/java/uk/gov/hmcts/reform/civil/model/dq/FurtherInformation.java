package uk.gov.hmcts.reform.civil.model.dq;

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
public class FurtherInformation {

    @CCD(
            label = "Do you intend to make any applications in the future?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo futureApplications;
    @CCD(ignore = true)
    private YesOrNo intentionToMakeFutureApplications;
    @CCD(
            label = "What for?",
            showCondition = "futureApplications = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String reasonForFutureApplications;
    @CCD(
            label = "Please provide any further information the Judge may need, including if you do not agree with the provisional track allocation of this claim",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String otherInformationForJudge;
}
