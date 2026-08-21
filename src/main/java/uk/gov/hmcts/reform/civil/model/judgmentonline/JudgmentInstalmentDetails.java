package uk.gov.hmcts.reform.civil.model.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.model.PaymentFrequencyList;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "InstalmentPaymentDetails", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class JudgmentInstalmentDetails {

    @CCD(label = "Enter the amount of the instalments agreed", searchable = false, typeOverride = FieldType.MoneyGBP)
    private String amount;
    @CCD(
            label = "Enter the frequency of the instalments agreed",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "PaymentFrequencyList",
            typeParameterClass = PaymentFrequencyList.class
    )
    private PaymentFrequency paymentFrequency;
    @CCD(label = "Enter the date of the first instalment", hint = "For example, 20 04 2021", searchable = false)
    private LocalDate startDate;
}
