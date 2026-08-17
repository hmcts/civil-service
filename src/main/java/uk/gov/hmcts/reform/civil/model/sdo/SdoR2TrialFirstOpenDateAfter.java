package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SdoR2FirstOpenDateAfter", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2TrialFirstOpenDateAfter {

    @CCD(label = " ", searchable = false)
    private LocalDate listFrom;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### First open date after", searchable = false, typeOverride = FieldType.Label)
  private String sdoR2FirstOpenDateAfterLabel;
  @CCD(label = "### List from", searchable = false, typeOverride = FieldType.Label)
  private String listFromLabel;
  // ==== end synthesised definition-only fields ====
}
