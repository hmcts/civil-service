package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FastTrackAllocation {

    @CCD(label = "Do you assign a complexity band to the case?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo assignComplexityBand;
    @CCD(
            label = "and the court assigns the claim to complexity",
            showCondition = "assignComplexityBand = \"Yes\"",
            searchable = false
    )
    private ComplexityBand band;
    @CCD(
            label = "because",
            showCondition = "assignComplexityBand = \"Yes\" OR assignComplexityBand = \"No\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String reasons;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "****", searchable = false, typeOverride = FieldType.Label)
  private String line1;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String label;
  // ==== end synthesised definition-only fields ====
}
