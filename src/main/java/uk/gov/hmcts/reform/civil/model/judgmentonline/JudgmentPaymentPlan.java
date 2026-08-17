package uk.gov.hmcts.reform.civil.model.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "JoPaymentPlan", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class JudgmentPaymentPlan {

    @CCD(label = "Select how the judgment will be paid", searchable = false)
    private PaymentPlanSelection type;
    @CCD(
            label = "Enter the date the judgment will be paid by",
            hint = "For example, 16 4 2021",
            showCondition = "type = \"PAY_BY_DATE\"",
            searchable = false
    )
    private LocalDate paymentDeadlineDate;
}
