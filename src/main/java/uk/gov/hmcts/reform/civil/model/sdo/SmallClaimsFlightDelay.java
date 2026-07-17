package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmallClaimsFlightDelay {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String relatedClaimsInput;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String legalDocumentsInput;
    @CCD(label = " ", searchable = false)
    private List<OrderDetailsPagesSectionsToggle> smallClaimsFlightDelayToggle;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String label;
  @CCD(label = "## Related claims", searchable = false, typeOverride = FieldType.Label)
  private String relatedClaimsTitle;
  @CCD(label = "## Legal arguments", searchable = false, typeOverride = FieldType.Label)
  private String legalDocumentsTitle;
  // ==== end synthesised definition-only fields ====
}
