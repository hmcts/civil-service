package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2RestrictNoOfPagesDetails {

    @CCD(label = " ", searchable = false)
    private String witnessShouldNotMoreThanTxt;
    @CCD(label = "Number of pages", hint = "For example,4", searchable = false)
    private Integer noOfPages;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String fontDetails;


}
