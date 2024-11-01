package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdatePaymentStatusService {

    private final PaymentProcessingHelper paymentProcessingHelper;

    @Retryable(value = CaseDataUpdateException.class, backoff = @Backoff(delay = 500))
    public void updatePaymentStatus(FeeType feeType, String caseReference, CardPaymentStatusResponse cardPaymentStatusResponse) {
        try {
            CaseData caseData = paymentProcessingHelper.getCaseData(caseReference);
            caseData = updateCaseDataWithStateAndPaymentDetails(cardPaymentStatusResponse, caseData, feeType.name());

            paymentProcessingHelper.createAndSubmitEvent(
                caseData,
                caseReference,
                feeType.name(),
                "UpdatePaymentStatus"
            );
        } catch (Exception ex) {
            log.error("Error updating payment status for case {}: {}", caseReference, ex.getMessage(), ex);
            throw new CaseDataUpdateException("Error updating payment status for case " + caseReference + ": " + ex.getMessage());
        }
    }

    private CaseData updateCaseDataWithStateAndPaymentDetails(CardPaymentStatusResponse cardPaymentStatusResponse,
                                                              CaseData caseData, String feeType) {
        PaymentDetails existingDetails = paymentProcessingHelper.retrievePaymentDetails(feeType, caseData);

        PaymentDetails paymentDetails = existingDetails != null
            ? existingDetails.toBuilder()
            .status(PaymentStatus.valueOf(cardPaymentStatusResponse.getStatus().toUpperCase()))
            .reference(cardPaymentStatusResponse.getPaymentReference())
            .errorCode(cardPaymentStatusResponse.getErrorCode())
            .errorMessage(cardPaymentStatusResponse.getErrorDescription())
            .build()
            : PaymentDetails.builder()
            .status(PaymentStatus.valueOf(cardPaymentStatusResponse.getStatus().toUpperCase()))
            .reference(cardPaymentStatusResponse.getPaymentReference())
            .errorCode(cardPaymentStatusResponse.getErrorCode())
            .errorMessage(cardPaymentStatusResponse.getErrorDescription())
            .build();

        return paymentProcessingHelper.updateCaseDataWithPaymentDetails(feeType, caseData, paymentDetails);
    }
}
