package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Experts {

    @CCD(label = "Do you want to use an expert?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo expertRequired;
    @CCD(
            label = "Have you already sent expert reports or similar to other parties?",
            showCondition = "expertRequired = \"Yes\"",
            searchable = false
    )
    private ExpertReportsSent expertReportsSent;
    @CCD(
            label = "Do you think the case is suitable for a joint expert?",
            showCondition = "expertRequired = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo jointExpertSuitable;
    @CCD(
            label = "Expert details",
            hint = "If the name is unknown at this time please add TBC to both the first name and last name lines. Then use the Manage Contact Information event to provide the name when known\n",
            showCondition = "expertRequired = \"Yes\"",
            searchable = false
    )
    private List<Element<Expert>> details;

    public Experts copy() {
        return new Experts()
            .setExpertRequired(expertRequired)
            .setExpertReportsSent(expertReportsSent)
            .setJointExpertSuitable(jointExpertSuitable)
            .setDetails(details);
    }
}
