package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;

public interface PaymentCallbackErrorHandler {

    default CaseData updateWithBusinessError(
        CaseData caseData, FeignException e, ObjectMapper objectMapper) throws JsonProcessingException {
        var paymentDto = objectMapper.readValue(e.contentUTF8(), PaymentDto.class);
        var statusHistory = paymentDto.getStatusHistories()[0];
        PaymentDetails paymentDetails = ofNullable(caseData.getClaimIssuedPaymentDetails())
            .map(PaymentDetails::toBuilder).orElse(PaymentDetails.builder())
            .status(FAILED)
            .errorCode(statusHistory.getErrorCode())
            .errorMessage(statusHistory.getErrorMessage())
            .build();

        return caseData.toBuilder()
            .claimIssuedPaymentDetails(paymentDetails)
            .build();
    }

    default CaseData updateWithBusinessErrorBackwardsCompatible(
        CaseData caseData, FeignException e, ObjectMapper objectMapper)
        throws JsonProcessingException {
        var paymentDto = objectMapper.readValue(e.contentUTF8(), PaymentDto.class);
        var statusHistory = paymentDto.getStatusHistories()[0];
        return caseData.toBuilder()
            .paymentDetails(PaymentDetails.builder()
                                .status(FAILED)
                                .errorCode(statusHistory.getErrorCode())
                                .errorMessage(statusHistory.getErrorMessage())
                                .build())
            .build();
    }
}
