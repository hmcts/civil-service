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

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentStatusService {

    private final PaymentsClient paymentsClient;

    @Retryable(value = RetryablePaymentException.class, backoff = @Backoff(delay = 500))
    public PaymentDto getCardPaymentDetails(String paymentReference, String authorization) {
        try {
            PaymentDto cardPaymentStatus = paymentsClient.getGovPayCardPaymentStatus(paymentReference, authorization);
            String status = cardPaymentStatus.getStatus();
            if (status.equals("Initiated")) {
                String message = "Need to check payment status again as current payment status is still Initiated";
                throw new RetryablePaymentException(message);
            }
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
