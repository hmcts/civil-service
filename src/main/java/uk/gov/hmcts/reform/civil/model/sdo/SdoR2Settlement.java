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
public class SdoR2Settlement {

    @CCD(label = " ", searchable = false)
    private List<IncludeInOrderToggle> includeInOrderToggle;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Each party must inform the Court immediately if the case is settled whether or not it is then possible to upload to the Digital Portal a draft consent order to give effect to their agreement.",
          showCondition = "includeInOrderToggle = \"INCLUDE\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label;
  // ==== end synthesised definition-only fields ====
}
