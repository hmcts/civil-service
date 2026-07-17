package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SmallClaimsWitnessStatement {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input1;
    @CCD(label = " ", showCondition = "smallClaimsNumberOfWitnessesToggle=\"SHOW\"", searchable = false)
    private String input2;
    @CCD(label = " ", showCondition = "smallClaimsNumberOfWitnessesToggle=\"SHOW\"", searchable = false)
    private String input3;
    @CCD(label = " ", showCondition = "smallClaimsNumberOfWitnessesToggle=\"SHOW\"", searchable = false)
    private String input4;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String text;
    @CCD(label = " ", searchable = false)
    private List<OrderDetailsPagesSectionsToggle> smallClaimsNumberOfWitnessesToggle;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String label;
  // ==== end synthesised definition-only fields ====
}
