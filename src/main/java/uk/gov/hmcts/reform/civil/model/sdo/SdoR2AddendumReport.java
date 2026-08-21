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
public class SdoR2AddendumReport {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoAddendumReportTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoAddendumReportDate;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Addendum report upload", searchable = false, typeOverride = FieldType.Label)
  private String label;
  // ==== end synthesised definition-only fields ====
}
