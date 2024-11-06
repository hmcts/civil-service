package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
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
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_CLAIM_ISSUE_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_HEARING_FEE_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SERVICE_REQUEST_RECEIVED;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestUpdateCallbackService {

    private static final String PAID = "Paid";
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final ObjectMapper objectMapper;

    public void processCallback(ServiceRequestUpdateDto dto, String feeTypeStr) {
        log.info("Processing callback for caseId {} with status {}",
                 dto.getCcdCaseNumber(),
                 dto.getServiceRequestStatus());

        if (!isPaid(dto)) {
            log.info("Service request status is not 'Paid'. No action required.");
            return;
        }

        FeeType feeType = getFeeType(feeTypeStr);
        if (feeType == null) {
            log.error("Invalid feeType: {}", feeTypeStr);
            return;
        }

        if (!isHandledFeeType(feeType)) {
            log.info("FeeType {} is not handled. No action required.", feeType);
            return;
        }

        Long caseId = Long.valueOf(dto.getCcdCaseNumber());
        log.info("Fetching case details for caseId {}", caseId);
        log.debug("ServiceRequestUpdateDto: {}", dto);

        CaseDetails caseDetails = coreCaseDataService.getCase(caseId);
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

        handlePaymentUpdate(dto, caseData, feeType, caseId);
    }

    private boolean isPaid(ServiceRequestUpdateDto dto) {
        return PAID.equalsIgnoreCase(dto.getServiceRequestStatus());
    }

    private FeeType getFeeType(String feeTypeStr) {
        try {
            return FeeType.valueOf(feeTypeStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isHandledFeeType(FeeType feeType) {
        return feeType == FeeType.HEARING || feeType == FeeType.CLAIMISSUED;
    }

    private boolean isPaymentUpdateValid(FeeType feeType, CaseData caseData) {
        return (feeType == FeeType.HEARING && isHearingPaymentUnpaidOrFailed(caseData))
            || (feeType == FeeType.CLAIMISSUED && isClaimIssuePaymentUnpaidOrFailed(caseData));
    }

    private boolean isHearingPaymentUnpaidOrFailed(CaseData caseData) {
        PaymentDetails hearingPayment = caseData.getHearingFeePaymentDetails();
        return hearingPayment == null || hearingPayment.getStatus() == PaymentStatus.FAILED;
    }

    private boolean isClaimIssuePaymentUnpaidOrFailed(CaseData caseData) {
        PaymentDetails claimIssuePayment = caseData.getClaimIssuedPaymentDetails();
        return claimIssuePayment == null || claimIssuePayment.getStatus() == PaymentStatus.FAILED;
    }

    @Retryable(value = CaseDataUpdateException.class, backoff = @Backoff(delay = 500))
    public void updatePaymentStatus(FeeType feeType, String caseReference, CardPaymentStatusResponse paymentStatusResponse) {
        try {
            Long caseId = Long.valueOf(caseReference);
            CaseDetails caseDetails = coreCaseDataService.getCase(caseId);
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
            caseData = updateCaseDataWithPaymentDetails(paymentStatusResponse, caseData, feeType);

            submitUpdatePaymentStatusEvent(caseData, caseId, feeType);
        } catch (Exception ex) {
            log.error("Error updating payment status for case {}: {}", caseReference, ex.getMessage());
            throw new CaseDataUpdateException();
        }
    }

    private void handlePaymentUpdate(ServiceRequestUpdateDto dto, CaseData caseData, FeeType feeType, Long caseId) {
        CardPaymentStatusResponse paymentStatusResponse = buildPaymentStatusResponse(dto);
        caseData = updateCaseDataWithPaymentDetails(paymentStatusResponse, caseData, feeType);

        if (caseData.isLipvLipOneVOne() && isPaymentUpdateValid(feeType, caseData)) {
            updatePaymentStatus(feeType, dto.getCcdCaseNumber(), paymentStatusResponse);
        } else {
            submitProcessCallbackEvent(caseData, caseId, feeType);
        }
    }

    private CardPaymentStatusResponse buildPaymentStatusResponse(ServiceRequestUpdateDto dto) {
        return CardPaymentStatusResponse.builder()
            .paymentReference(dto.getPayment().getPaymentReference())
            .status(PaymentStatus.SUCCESS.name())
            .build();
    }

    private void submitProcessCallbackEvent(CaseData caseData, Long caseId, FeeType feeType) {
        CaseEvent event = determineProcessCallbackEventFromFeeType(caseData, feeType);
        submitEvent(caseData, caseId, event);
    }

    private void submitUpdatePaymentStatusEvent(CaseData caseData, Long caseId, FeeType feeType) {
        CaseEvent event = determineUpdatePaymentStatusEventFromFeeType(feeType);
        submitEvent(caseData, caseId, event);
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

    private CaseEvent determineProcessCallbackEventFromFeeType(CaseData caseData, FeeType feeType) {
        return switch (feeType) {
            case HEARING -> SERVICE_REQUEST_RECEIVED;
            case CLAIMISSUED -> caseData.getCaseAccessCategory() == CaseCategory.SPEC_CLAIM
                ? CREATE_CLAIM_SPEC_AFTER_PAYMENT
                : CREATE_CLAIM_AFTER_PAYMENT;
        };
    }

    private CaseEvent determineUpdatePaymentStatusEventFromFeeType(FeeType feeType) {
        return switch (feeType) {
            case HEARING -> CITIZEN_HEARING_FEE_PAYMENT;
            case CLAIMISSUED -> CITIZEN_CLAIM_ISSUE_PAYMENT;
        };
    }

    private CaseData updateCaseDataWithPaymentDetails(CardPaymentStatusResponse response, CaseData caseData, FeeType feeType) {
        PaymentDetails updatedPayment = PaymentDetails.builder()
            .status(PaymentStatus.valueOf(response.getStatus()))
            .reference(response.getPaymentReference())
            .errorCode(response.getErrorCode())
            .errorMessage(response.getErrorDescription())
            .build();

        PaymentDetails mergedPayment = Optional.ofNullable(getPaymentDetails(feeType, caseData))
            .map(existingPayment -> existingPayment.toBuilder()
                .status(updatedPayment.getStatus())
                .reference(updatedPayment.getReference())
                .errorCode(updatedPayment.getErrorCode())
                .errorMessage(updatedPayment.getErrorMessage())
                .build())
            .orElse(updatedPayment);

        return applyPaymentDetails(caseData, feeType, mergedPayment);
    }

    private PaymentDetails getPaymentDetails(FeeType feeType, CaseData caseData) {
        return switch (feeType) {
            case HEARING -> caseData.getHearingFeePaymentDetails();
            case CLAIMISSUED -> caseData.getClaimIssuedPaymentDetails();
        };
    }

    private CaseData applyPaymentDetails(CaseData caseData, FeeType feeType, PaymentDetails paymentDetails) {
        return switch (feeType) {
            case HEARING -> caseData.toBuilder().hearingFeePaymentDetails(paymentDetails).build();
            case CLAIMISSUED -> caseData.toBuilder().claimIssuedPaymentDetails(paymentDetails).build();
        };
    }
}
