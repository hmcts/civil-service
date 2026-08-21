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
public class SdoR2RestrictWitness {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isRestrictWitness;
    @CCD(label = " ", showCondition = "isRestrictWitness = \"Yes\"", searchable = false)
    private SdoR2RestrictNoOfWitnessDetails restrictNoOfWitnessDetails;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Restrict number of witnesses", searchable = false, typeOverride = FieldType.Label)
  private String sdoR2RestrictWitnessLabel;
  // ==== end synthesised definition-only fields ====
}
