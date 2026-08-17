package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec;

import java.math.BigDecimal;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "RecurringExpense", generate = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RecurringExpenseLRspec {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.FixedList, typeParameterOverride = "ExpenseType")
    private ExpenseTypeLRspec type;
    @CCD(label = "Type of expense", showCondition = "type = \"OTHER\"", searchable = false)
    private String typeOtherDetails;
    /**
     * amount in pence.
     */
    @CCD(
            label = "Amount paid",
            hint = "Only include the amount your client is paying to clear previously missed regular payments",
            searchable = false,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amount;
    @CCD(label = "Paid every:", searchable = false)
    private PaymentFrequencyLRspec frequency;
}
