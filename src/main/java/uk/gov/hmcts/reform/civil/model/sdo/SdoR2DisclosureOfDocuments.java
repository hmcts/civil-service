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
public class SdoR2DisclosureOfDocuments {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String standardDisclosureTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate standardDisclosureDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String inspectionTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate inspectionDate;
    @CCD(label = "Request will be compiled with", searchable = false)
    private String requestsWillBeCompiledLabel;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Standard disclosure", searchable = false, typeOverride = FieldType.Label)
  private String label;
  @CCD(label = "### Inspection", searchable = false, typeOverride = FieldType.Label)
  private String inspectionLabel;
  // ==== end synthesised definition-only fields ====
}
