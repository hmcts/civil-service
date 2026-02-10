package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2ApplicationToRelyOnFurther {

    private YesOrNo doRequireApplicationToRely;
    private SdoR2ApplicationToRelyOnFurtherDetails applicationToRelyOnFurtherDetails;
}
