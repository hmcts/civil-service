package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2SmallClaimsRestrictWitness {

    @CCD(label = "Limit number of witnesses (claimant)", hint = "For example,4", searchable = false)
    private Integer noOfWitnessClaimant;
    @CCD(label = "Limit number of witnesses (defendant)", hint = "For example,4", searchable = false)
    private Integer noOfWitnessDefendant;
    @CCD(label = " ", searchable = false)
    private String partyIsCountedAsWitnessTxt;
}
