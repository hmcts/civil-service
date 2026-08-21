package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DisposalHearingHearingNotesDJ {

    @CCD(label = "This is only seen by the listing officer.", searchable = false, typeOverride = FieldType.TextArea)
    private String input;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String label;
  @CCD(label = "****", searchable = false, typeOverride = FieldType.Label)
  private String line;
  // ==== end synthesised definition-only fields ====
}
