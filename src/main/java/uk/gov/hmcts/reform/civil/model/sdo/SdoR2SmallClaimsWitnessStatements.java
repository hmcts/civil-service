package uk.gov.hmcts.reform.civil.model.sdo;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2SmallClaimsWitnessStatements {

    @CCD(label = "Statements of witnesses", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoStatementOfWitness;
    @CCD(label = " ", showCondition = "isRestrictWitness = \"Yes\"", searchable = false)
    private SdoR2SmallClaimsRestrictWitness sdoR2SmallClaimsRestrictWitness;
    @CCD(label = " ", showCondition = "isRestrictPages = \"Yes\"", searchable = false)
    private SdoR2SmallClaimsRestrictPages sdoR2SmallClaimsRestrictPages;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isRestrictWitness;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isRestrictPages;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String text;

    @CCD(label = " ", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate deadlineDate;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Statements of witnesses", searchable = false, typeOverride = FieldType.Label)
  private String label;
  @CCD(label = "**Restrict number of witnesses**", searchable = false, typeOverride = FieldType.Label)
  private String restrictWitnessLbl;
  @CCD(label = "**Restrict number of pages**", searchable = false, typeOverride = FieldType.Label)
  private String restrictPagesLbl;
  @CCD(label = "### A witness statement must", searchable = false, typeOverride = FieldType.Label)
  private String label1;
  // ==== end synthesised definition-only fields ====
}
