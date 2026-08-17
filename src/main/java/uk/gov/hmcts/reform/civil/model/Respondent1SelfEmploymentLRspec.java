package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.math.BigDecimal;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SelfEmployment", generate = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Respondent1SelfEmploymentLRspec {

    @CCD(label = "Job title", searchable = false)
    private String jobTitle;
    @CCD(label = "Annual turnover", searchable = false, typeOverride = FieldType.MoneyGBP)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal annualTurnover;
    @CCD(label = "Is your client behind on tax payments?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isBehindOnTaxPayment;
    @CCD(
            label = "Amount owed",
            showCondition = "isBehindOnTaxPayment = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amountOwed;
    @CCD(
            label = "Reason",
            showCondition = "isBehindOnTaxPayment = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String reason;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "## If self-employed:\n", searchable = false, typeOverride = FieldType.Label)
  private String selfEmploymentLabel;
  // ==== end synthesised definition-only fields ====
}
