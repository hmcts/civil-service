package uk.gov.hmcts.reform.civil.model.judgmentonline;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "JoPaymentPlanTypeList", generate = true)
public enum PaymentPlanSelection {
    @CCD(label = "Paid in instalments")
    PAY_IN_INSTALMENTS,
    @CCD(label = "Paid by date")
    PAY_BY_DATE,
    @CCD(label = "Paid immediately")
    PAY_IMMEDIATELY
}
