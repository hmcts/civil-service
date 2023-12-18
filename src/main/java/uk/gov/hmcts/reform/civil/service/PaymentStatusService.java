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

    private final PaymentsClient paymentsClient;

    @Retryable(value = RetryablePaymentException.class, backoff = @Backoff(delay = 500))
    public CardPaymentServiceRequestResponse createGovPayCardPaymentRequest(
        String serviceRequestReference, String authorization, CardPaymentServiceRequestDTO requestDto) {
        try {
            return paymentsClient.createGovPayCardPaymentRequest(serviceRequestReference, authorization, requestDto);
        } catch (FeignException.InternalServerError ex) {
            throw new RetryablePaymentException(ex.contentUTF8(), ex);
        } catch (FeignException ex) {
            log.error("Payments response error \n\tstatus: {} => message: \"{}\"", ex.status(), ex.contentUTF8(), ex);
            log.info("Feign exception caught, payment will not be retried");
            throw new PaymentsApiException(ex.contentUTF8(), ex);
        }
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
