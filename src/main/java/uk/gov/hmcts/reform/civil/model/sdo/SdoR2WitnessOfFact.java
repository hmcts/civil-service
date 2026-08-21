package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2WitnessOfFact {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoStatementOfWitness;
    @CCD(label = " ", searchable = false)
    private SdoR2RestrictWitness sdoR2RestrictWitness;
    @CCD(label = " ", searchable = false)
    private SdoR2RestrictPages sdoRestrictPages;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoWitnessDeadline;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoWitnessDeadlineDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoWitnessDeadlineText;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Statements of witnesses", searchable = false, typeOverride = FieldType.Label)
  private String label;
  @CCD(label = "### Deadline", searchable = false, typeOverride = FieldType.Label)
  private String sdoWitnessDeadlineLabel;
  // ==== end synthesised definition-only fields ====
}
