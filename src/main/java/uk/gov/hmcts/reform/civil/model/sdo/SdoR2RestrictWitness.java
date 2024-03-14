package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2RestrictWitness {

    private YesOrNo isRestrictWitness;
    private SdoR2RestrictNoOfWitnessDetails restrictNoOfWitnessDetails;

}
