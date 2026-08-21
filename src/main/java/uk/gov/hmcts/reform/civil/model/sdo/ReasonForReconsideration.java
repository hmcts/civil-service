package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReasonForReconsideration {

    @CCD(
            label = "Enter your reasons",
            hint = "For example, the legal adviser overlooked the following factors or did not give them sufficient importance",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String reasonForReconsiderationTxt;
    @CCD(label = "Requestor", searchable = false)
    private String requestor;
}
