package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.DebtTypeLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;

import java.math.BigDecimal;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "Debt", generate = true)
@Data
@Accessors(chain = true)
public class DebtLRspec {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.FixedList, typeParameterOverride = "DebtType")
    private DebtTypeLRspec debtType;
    @CCD(
            label = "Repayment amount",
            hint = "Only include the amount paid to clear previously missed regular payments",
            searchable = false,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal paymentAmount;
    @CCD(label = "Select one option.", searchable = false)
    private PaymentFrequencyLRspec paymentFrequency;

}
