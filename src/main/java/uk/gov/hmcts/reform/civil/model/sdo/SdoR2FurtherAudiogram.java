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
public class SdoR2FurtherAudiogram {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoClaimantShallUndergoTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoClaimantShallUndergoDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoServiceReportTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoServiceReportDate;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "### Claimant shall undergo a single further audiogram",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String sdoClaimantShallUndergoLbl;
  @CCD(label = "### Service of report", searchable = false, typeOverride = FieldType.Label)
  private String sdoServiceReportLbl;
  // ==== end synthesised definition-only fields ====
}
