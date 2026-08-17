package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2SmallClaimsBundleOfDocs {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String physicalBundlePartyTxt;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "**Bundle of documents**", searchable = false, typeOverride = FieldType.Label)
  private String label;
  // ==== end synthesised definition-only fields ====
}
