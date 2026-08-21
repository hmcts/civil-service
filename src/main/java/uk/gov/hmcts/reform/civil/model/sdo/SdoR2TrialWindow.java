package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SdoR2TrialFromTo", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2TrialWindow {

    @CCD(label = " ", searchable = false)
    private LocalDate listFrom;
    @CCD(label = " ", searchable = false)
    private LocalDate dateTo;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Trial window", searchable = false, typeOverride = FieldType.Label)
  private String sdoR2TrialWindowLabel;
  @CCD(label = "### List from", searchable = false, typeOverride = FieldType.Label)
  private String listFromLabel;
  @CCD(label = "### Date to", searchable = false, typeOverride = FieldType.Label)
  private String dateToLabel;
  // ==== end synthesised definition-only fields ====
}
