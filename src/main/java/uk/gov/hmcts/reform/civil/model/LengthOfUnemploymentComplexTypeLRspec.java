package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class LengthOfUnemploymentComplexTypeLRspec {

    @CCD(label = "Years", searchable = false)
    private String numberOfYearsInUnemployment;
    @CCD(label = "Months", searchable = false)
    private String numberOfMonthsInUnemployment;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "How long has your client been unemployed? \n", searchable = false, typeOverride = FieldType.Label)
  private String lengthOfUnemploymentLabel;
  // ==== end synthesised definition-only fields ====
}
