package uk.gov.hmcts.reform.civil.client.payments;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.civil.client.payments.config.PaymentClientConfiguration;
import uk.gov.hmcts.reform.civil.client.payments.models.PaymentDto;
import uk.gov.hmcts.reform.civil.client.payments.request.CardPaymentRequest;
import uk.gov.hmcts.reform.civil.client.payments.request.CardPaymentServiceRequestDTO;
import uk.gov.hmcts.reform.civil.client.payments.request.CreateServiceRequestDTO;
import uk.gov.hmcts.reform.civil.client.payments.request.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.civil.client.payments.request.PBAServiceRequestDTO;
import uk.gov.hmcts.reform.civil.client.payments.response.CardPaymentServiceRequestResponse;
import uk.gov.hmcts.reform.civil.client.payments.response.PBAServiceRequestResponse;
import uk.gov.hmcts.reform.civil.client.payments.response.PaymentServiceResponse;

@FeignClient(name = "payments-api", url = "${payments.api.url}", configuration = PaymentClientConfiguration.class)
public interface PaymentsApi {
    @PostMapping(value = "/credit-account-payments", consumes = "application/json")
    PaymentDto createCreditAccountPayment(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestBody CreditAccountPaymentRequest paymentRequest
    );

    @PostMapping(value = "/card-payments", consumes = "application/json")
    PaymentDto createCardPayment(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestHeader("return-url") String redirectUrl,
            @RequestHeader("service-callback-url") String serviceCallbackUrl,
            @RequestBody CardPaymentRequest paymentRequest
    );

    @GetMapping("/card-payments/{paymentReference}")
    PaymentDto retrieveCardPayment(
            @PathVariable("paymentReference") String paymentReference,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization
    );

    @GetMapping("/card-payments/{paymentReference}/statuses")
    PaymentDto retrieveCardPaymentStatus(
            @PathVariable("paymentReference") String paymentReference,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization
    );

    @PostMapping(value = "/card-payments/{paymentReference}/cancel")
    void cancelCardPayment(
            @PathVariable("paymentReference") String paymentReference,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization
    );

    @GetMapping(value = "/payments/{payment-reference}")
    PaymentDto getGovPayCardPaymentStatus(
            @PathVariable("payment-reference") String paymentReference,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization
    );

    @PostMapping(value = "/service-request", consumes = "application/json")
    PaymentServiceResponse createServiceRequest(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestBody CreateServiceRequestDTO paymentRequest
    );

    @PostMapping(value = "/service-request/{service-request-reference}/pba-payments", consumes = "application/json")
    PBAServiceRequestResponse createPbaPayment(
            @PathVariable("service-request-reference") String serviceReqReference,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestBody PBAServiceRequestDTO paymentRequest
    );

    @PostMapping(value = "/service-request/{service-request-reference}/card-payments", consumes = "application/json")
    CardPaymentServiceRequestResponse createGovPayCardPaymentRequest(
            @PathVariable("service-request-reference") String serviceReqReference,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestBody CardPaymentServiceRequestDTO paymentRequest
    );
}
