package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FutureApplications {

    @CCD(
            label = "Do you intend to make any applications in the future?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo intentionToMakeFutureApplications;
    @CCD(label = "What for?", showCondition = "intentionToMakeFutureApplications = \"Yes\"", searchable = false)
    private String whatWillFutureApplicationsBeMadeFor;
}
