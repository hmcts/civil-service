package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtherDetails {

    @CCD(label = "Not suitable for SDO", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo notSuitableForSDO;
    @CCD(label = "Reason", searchable = false)
    private String reasonNotSuitableForSDO;
}
