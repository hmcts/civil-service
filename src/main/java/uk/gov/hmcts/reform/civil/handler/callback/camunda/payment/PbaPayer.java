package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

public interface PbaPayer {

    default CaseData updateWithCreditAccountPayment(
        CaseData caseData, String authToken, Time time, PaymentsService paymentsService) {
        var paymentReference = paymentsService.createCreditAccountPayment(caseData, authToken).getReference();
        PaymentDetails paymentDetails = ofNullable(caseData.getClaimIssuedPaymentDetails())
            .map(PaymentDetails::toBuilder)
            .orElse(PaymentDetails.builder())
            .status(SUCCESS)
            .reference(paymentReference)
            .errorCode(null)
            .errorMessage(null)
            .build();

        return caseData.toBuilder()
            .claimIssuedPaymentDetails(paymentDetails)
            .paymentSuccessfulDate(time.now())
            .build();
    }
}
