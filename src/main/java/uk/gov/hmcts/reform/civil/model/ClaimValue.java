package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ClaimValue {

    @CCD(label = "Statement of value", searchable = false, min = 100, typeOverride = FieldType.MoneyGBP)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal statementOfValueInPennies;

    @JsonCreator
    public ClaimValue(@JsonProperty("statementOfValueInPennies") BigDecimal statementOfValueInPennies) {
        this.statementOfValueInPennies = statementOfValueInPennies;
    }

    public BigDecimal toPounds() {
        return MonetaryConversions.penniesToPounds(this.statementOfValueInPennies);
    }

    public String formData() {
        return "up to £" + this.toPounds();
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Your fee will be calculated based on the statement of value",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label;
  // ==== end synthesised definition-only fields ====
}