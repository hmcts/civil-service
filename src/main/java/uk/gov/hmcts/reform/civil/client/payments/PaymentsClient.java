package uk.gov.hmcts.reform.civil.client.payments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.client.payments.models.PaymentDto;
import uk.gov.hmcts.reform.civil.client.payments.request.CardPaymentRequest;
import uk.gov.hmcts.reform.civil.client.payments.request.CardPaymentServiceRequestDTO;
import uk.gov.hmcts.reform.civil.client.payments.request.CreateServiceRequestDTO;
import uk.gov.hmcts.reform.civil.client.payments.request.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.civil.client.payments.request.PBAServiceRequestDTO;
import uk.gov.hmcts.reform.civil.client.payments.response.CardPaymentServiceRequestResponse;
import uk.gov.hmcts.reform.civil.client.payments.response.PBAServiceRequestResponse;
import uk.gov.hmcts.reform.civil.client.payments.response.PaymentServiceResponse;

@Service
@ConditionalOnProperty(prefix = "payments", name = "api.url")
public class PaymentsClient {
    private PaymentsApi paymentsApi;
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    public PaymentsClient(PaymentsApi paymentsApi, AuthTokenGenerator authTokenGenerator) {
        this.paymentsApi = paymentsApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    public PaymentDto createCreditAccountPayment(String authorisation, CreditAccountPaymentRequest paymentRequest) {
        return paymentsApi.createCreditAccountPayment(
                authorisation,
                authTokenGenerator.generate(),
                paymentRequest
        );
    }

    public PaymentDto createCardPayment(String authorisation, CardPaymentRequest paymentRequest,
                                        String redirectUrl, String serviceCallbackUrl) {
        return paymentsApi.createCardPayment(
                authorisation,
                authTokenGenerator.generate(),
                redirectUrl,
                serviceCallbackUrl,
                paymentRequest
        );
    }

    public PaymentDto retrieveCardPayment(String authorisation, String paymentReference) {
        return paymentsApi.retrieveCardPayment(
                paymentReference,
                authorisation,
                authTokenGenerator.generate()
        );
    }

    public void cancelCardPayment(String authorisation, String paymentReference) {
        paymentsApi.cancelCardPayment(
                paymentReference,
                authorisation,
                authTokenGenerator.generate()
        );
    }

    public PaymentServiceResponse createServiceRequest(String authorisation, CreateServiceRequestDTO paymentRequest) {
        return paymentsApi.createServiceRequest(
                authorisation,
                authTokenGenerator.generate(),
                paymentRequest
        );
    }

    public PBAServiceRequestResponse createPbaPayment(String serviceReqReference, String authorisation,
                                                      PBAServiceRequestDTO paymentRequest) {
        return paymentsApi.createPbaPayment(
                serviceReqReference,
                authorisation,
                authTokenGenerator.generate(),
                paymentRequest
        );
    }

    public CardPaymentServiceRequestResponse createGovPayCardPaymentRequest(
            String serviceReqReference,
            String authorization,
            CardPaymentServiceRequestDTO paymentRequest) {
        return paymentsApi.createGovPayCardPaymentRequest(
                serviceReqReference, authorization, authTokenGenerator.generate(), paymentRequest);
    }

    public PaymentDto getGovPayCardPaymentStatus(String paymentReference, String authorization) {
        return paymentsApi.retrieveCardPaymentStatus(paymentReference, authorization, authTokenGenerator.generate());
    }
}
