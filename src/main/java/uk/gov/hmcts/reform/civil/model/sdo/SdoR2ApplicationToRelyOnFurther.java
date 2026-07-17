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
public class SdoR2ApplicationToRelyOnFurther {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo doRequireApplicationToRely;
    @CCD(label = " ", showCondition = "doRequireApplicationToRely = \"Yes\"", searchable = false)
    private SdoR2ApplicationToRelyOnFurtherDetails applicationToRelyOnFurtherDetails;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "### Require application to rely on further medical evidence",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String applicationToRelyLbl;
  // ==== end synthesised definition-only fields ====
}
