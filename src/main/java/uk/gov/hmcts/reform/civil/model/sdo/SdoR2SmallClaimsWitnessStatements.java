package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2SmallClaimsWitnessStatements {

    private String sdoStatementOfWitness;
    private SdoR2SmallClaimsRestrictWitness sdoR2SmallClaimsRestrictWitness;
    private SdoR2SmallClaimsRestrictPages sdoR2SmallClaimsRestrictPages;
    private YesOrNo isRestrictWitness;
    private YesOrNo isRestrictPages;
    private String text;
}
