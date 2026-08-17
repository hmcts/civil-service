package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2VariationOfDirections {

    @CCD(label = " ", searchable = false)
    private List<IncludeInOrderToggle> includeInOrderToggle;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "The parties may, by written agreement, extend time for compliance with a direction where that is permitted by CPR 3.8(4). Otherwise, the time for compliance with a direction may only be extended by making an application.",
          showCondition = "includeInOrderToggle = \"INCLUDE\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label;
  // ==== end synthesised definition-only fields ====
}
