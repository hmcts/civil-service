package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_CLAIM_ISSUE_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_HEARING_FEE_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SERVICE_REQUEST_RECEIVED;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentStatusRetryService {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper objectMapper;

    @Retryable(value = CaseDataUpdateException.class, backoff = @Backoff(delay = 500))
    public void updatePaymentStatus(FeeType feeType, String caseReference, CaseData caseData) {
        try {
            Long caseId = Long.valueOf(caseReference);
            submitUpdatePaymentEvent(caseData, caseId, feeType);
        } catch (Exception ex) {
            log.info("Retrying payment status update for case {}", caseReference);
            throw new CaseDataUpdateException();
        }
    }

    @Retryable(value = CaseDataUpdateException.class, backoff = @Backoff(delay = 500))
    public void updatePaymentStatus(FeeType feeType, String caseReference, CardPaymentStatusResponse response) {
        try {
            Long caseId = Long.valueOf(caseReference);
            CaseDetails caseDetails = coreCaseDataService.getCase(caseId);
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
            caseData = updateCaseDataWithPaymentDetails(response, caseData, feeType);
            submitUpdatePaymentEvent(caseData, caseId, feeType);
        } catch (Exception ex) {
            log.info("Retrying payment status update for case {}", caseReference);
            throw new CaseDataUpdateException();
        }
    }

    @Recover
    public void recover(CaseDataUpdateException ex,
                        FeeType feeType,
                        String caseReference,
                        CardPaymentStatusResponse response) {

        String status = response != null ? response.getStatus() : "N/A";
        String errorCode = response != null ? response.getErrorCode() : "N/A";

        log.error(
            "Payment status update failed after retries for case {} and fee type {}. Status: {}, ErrorCode: {}",
            caseReference,
            feeType,
            status,
            errorCode,
            ex
        );
    }

    @Recover
    public void recover(CaseDataUpdateException ex,
                        FeeType feeType,
                        String caseReference,
                        CaseData caseData) {
        log.error("Payment status update (CaseData) failed after retries for case {} and fee type {}", caseReference, feeType, ex);
    }

    CaseData updateCaseDataWithPaymentDetails(CardPaymentStatusResponse response,
                                                      CaseData caseData,
                                                      FeeType feeType) {
        return updateCaseDataWithPaymentDetails(response, caseData, feeType, null);
    }

    CaseData updateCaseDataWithPaymentDetails(CardPaymentStatusResponse response,
                                              CaseData caseData,
                                              FeeType feeType,
                                              String customerReference) {
        PaymentDetails existingPayment = getPaymentDetails(feeType, caseData);
        PaymentDetails paymentDetails = existingPayment != null ? existingPayment : new PaymentDetails();

        paymentDetails.setStatus(resolvePaymentStatus(response.getStatus()));
        paymentDetails.setReference(response.getPaymentReference());
        paymentDetails.setErrorCode(response.getErrorCode());
        paymentDetails.setErrorMessage(response.getErrorDescription());

        if (customerReference != null) {
            paymentDetails.setCustomerReference(customerReference);
        }

        return applyPaymentDetails(caseData, feeType, paymentDetails);
    }

    PaymentDetails getPaymentDetails(FeeType feeType, CaseData caseData) {
        return switch (feeType) {
            case HEARING -> caseData.getHearingFeePaymentDetails();
            case CLAIMISSUED -> caseData.getClaimIssuedPaymentDetails();
            default -> throw new IllegalArgumentException("Unsupported fee type for payment details: " + feeType);
        };
    }

    CaseData applyPaymentDetails(CaseData caseData, FeeType feeType, PaymentDetails paymentDetails) {
        switch (feeType) {
            case HEARING -> caseData.setHearingFeePaymentDetails(paymentDetails);
            case CLAIMISSUED -> caseData.setClaimIssuedPaymentDetails(paymentDetails);
            default -> throw new IllegalArgumentException("Unsupported fee type for case update: " + feeType);
        }
        return caseData;
    }

    PaymentStatus resolvePaymentStatus(String status) {
        return status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;
    }

    private void submitUpdatePaymentEvent(CaseData caseData, Long caseId, FeeType feeType) {
        CaseEvent event = determineEventFromFeeType(caseData, feeType);
        submitEvent(caseData, caseId, event);
    }

    CaseEvent determineEventFromFeeType(CaseData caseData, FeeType feeType) {
        if (caseData.isLipvLipOneVOne()) {
            return switch (feeType) {
                case HEARING -> CITIZEN_HEARING_FEE_PAYMENT;
                case CLAIMISSUED -> CITIZEN_CLAIM_ISSUE_PAYMENT;
                default -> throw new IllegalArgumentException("Unsupported fee type for LiP event: " + feeType);
            };
        } else {
            return switch (feeType) {
                case HEARING -> SERVICE_REQUEST_RECEIVED;
                case CLAIMISSUED -> caseData.getCaseAccessCategory() == CaseCategory.SPEC_CLAIM
                    ? CREATE_CLAIM_SPEC_AFTER_PAYMENT
                    : CREATE_CLAIM_AFTER_PAYMENT;
                default -> throw new IllegalArgumentException("Unsupported fee type for event: " + feeType);
            };
        }
    }

    private void submitEvent(CaseData caseData, Long caseId, CaseEvent event) {
        StartEventResponse startEvent = coreCaseDataService.startUpdate(
            String.valueOf(caseId),
            event
        );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEvent.getToken())
            .event(Event.builder().id(startEvent.getEventId()).build())
            .data(caseData.toMap(objectMapper))
            .build();

        coreCaseDataService.submitUpdate(String.valueOf(caseId), caseDataContent);
    }
}
