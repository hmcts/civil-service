package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2RestrictPages {

    private YesOrNo isRestrictPages;
    private SdoR2RestrictNoOfPagesDetails restrictNoOfPagesDetails;

}
