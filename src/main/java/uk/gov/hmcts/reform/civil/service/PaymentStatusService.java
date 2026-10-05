package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.exceptions.PaymentsApiException;
import uk.gov.hmcts.reform.civil.exceptions.RetryablePaymentException;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.request.CardPaymentServiceRequestDTO;
import uk.gov.hmcts.reform.payments.response.CardPaymentServiceRequestResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentStatusService {

    private static final int PRECONDITION_FAILED = 412;
    private static final String SERVICE_REQUEST_ALREADY_PAID = "serviceRequest has already been paid";

    private final PaymentsClient paymentsClient;

    @Retryable(value = RetryablePaymentException.class, backoff = @Backoff(delay = 500))
    public CardPaymentServiceRequestResponse createGovPayCardPaymentRequest(
        String serviceRequestReference, String authorization, CardPaymentServiceRequestDTO requestDto) {
        try {
            return paymentsClient.createGovPayCardPaymentRequest(serviceRequestReference, authorization, requestDto);
        } catch (FeignException.InternalServerError ex) {
            throw new RetryablePaymentException(ex.contentUTF8(), ex);
        } catch (FeignException ex) {
            if (isServiceRequestAlreadyPaid(ex)) {
                log.info("Service request {} has already been paid, treating create payment request as successful",
                         serviceRequestReference);
                return new CardPaymentServiceRequestResponse(
                    serviceRequestReference,
                    serviceRequestReference,
                    "Success",
                    null,
                    null
                );
            }
            log.error("Payments response error \n\tstatus: {} => message: \"{}\"", ex.status(), ex.contentUTF8(), ex);
            log.info("Feign exception caught, payment will not be retried");
            throw new PaymentsApiException(ex.contentUTF8(), ex);
        }
    }

    private boolean isServiceRequestAlreadyPaid(FeignException ex) {
        return ex.status() == PRECONDITION_FAILED
            && ex.contentUTF8().contains(SERVICE_REQUEST_ALREADY_PAID);
    }

    @Retryable(value = RetryablePaymentException.class, maxAttempts = 5, backoff = @Backoff(delay = 500))
    public PaymentDto getCardPaymentDetails(String paymentReference, String authorization) {
        try {
            PaymentDto cardPaymentStatus = paymentsClient.getGovPayCardPaymentStatus(paymentReference, authorization);
            log.info("Payment status for payment reference {} is {}", paymentReference, cardPaymentStatus.getStatus());
            return cardPaymentStatus;
        } catch (FeignException.InternalServerError ex) {
            throw new RetryablePaymentException(ex.contentUTF8(), ex);
        } catch (FeignException ex) {
            log.error("Payments response error \n\tstatus: {} => message: \"{}\"", ex.status(), ex.contentUTF8(), ex);
            log.info("Feign exception caught, payment will not be retried");
            throw new PaymentsApiException(ex.contentUTF8(), ex);
        }
    }
}
