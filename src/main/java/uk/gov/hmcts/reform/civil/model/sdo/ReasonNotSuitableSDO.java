package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReasonNotSuitableSDO {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String label;
  @CCD(
          label = "The case will be taken offline automatically.\n\nIf you are judge submitting this information, the case will be sent to a listing officer. If a legal advisor has submitted this information, the case will be sent to a judge for review.\n\nGive reasons",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String text;
  // ==== end synthesised definition-only fields ====
}
