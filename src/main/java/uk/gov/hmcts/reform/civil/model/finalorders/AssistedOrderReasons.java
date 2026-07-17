package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssistedOrderReasons {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String reasonsText;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Brief reasons", searchable = false, typeOverride = FieldType.Label)
  private String reasonsLabel;
  // ==== end synthesised definition-only fields ====
}
