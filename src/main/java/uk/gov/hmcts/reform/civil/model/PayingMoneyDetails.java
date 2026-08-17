package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "PayingExtraInfo", generate = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PayingMoneyDetails {

    @CCD(label = "Claim number", regex = "[0-9A-Za-z]*", searchable = false)
    private String claimNumberText;
    @CCD(label = "Amount owed", searchable = false, typeOverride = FieldType.MoneyGBP)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amountOwed;
    @CCD(label = "Monthly instalment amount", searchable = false, typeOverride = FieldType.MoneyGBP)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal monthlyInstalmentAmount;

}
