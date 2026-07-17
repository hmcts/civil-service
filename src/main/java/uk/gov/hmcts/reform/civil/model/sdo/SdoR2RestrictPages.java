package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2RestrictPages {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isRestrictPages;
    @CCD(label = " ", showCondition = "isRestrictPages = \"Yes\"", searchable = false)
    private SdoR2RestrictNoOfPagesDetails restrictNoOfPagesDetails;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Restrict number of pages", searchable = false, typeOverride = FieldType.Label)
  private String sdoR2RestrictPagesLabel;
  // ==== end synthesised definition-only fields ====
}
