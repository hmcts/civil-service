package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2SmallClaimsRestrictWitness {

    private Integer noOfWitnessClaimant;
    private Integer noOfWitnessDefendant;
    private String partyIsCountedAsWitnessTxt;
}
