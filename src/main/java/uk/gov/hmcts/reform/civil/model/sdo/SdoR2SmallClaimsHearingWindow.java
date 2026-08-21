package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SdoR2SmallClaimsHearingFromTo", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2SmallClaimsHearingWindow {

    @CCD(label = " ", searchable = false)
    private LocalDate listFrom;
    @CCD(label = " ", searchable = false)
    private LocalDate dateTo;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "**Hearing window**", searchable = false, typeOverride = FieldType.Label)
  private String hearingWindowTitle;
  @CCD(label = "**List from**", searchable = false, typeOverride = FieldType.Label)
  private String hearingWindowFromLbl;
  @CCD(label = "**Date to**", searchable = false, typeOverride = FieldType.Label)
  private String hearingWindowToLbl;
  // ==== end synthesised definition-only fields ====
}
