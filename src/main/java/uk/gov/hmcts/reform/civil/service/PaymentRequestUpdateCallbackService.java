package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.*;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.*;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.*;

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

        if (caseData.isLipvLipOneVOne()) {
            processLipvCase(dto, caseData, feeType, caseId);
        } else {
            handleNonLipvPaymentUpdate(dto, caseData, feeType, caseId);
        }
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

    private void processLipvCase(ServiceRequestUpdateDto dto, CaseData caseData, FeeType feeType, Long caseId) {
        if (isPaymentUpdateValid(feeType, caseData)) {
            handleValidPaymentUpdate(dto, caseData, feeType, caseId);
        } else {
            log.info("Payment update is not valid for case {} and feeType {}", caseId, feeType);
        }
    }

    @Retryable(value = CaseDataUpdateException.class, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public void updatePaymentStatus(FeeType feeType, String caseReference, CardPaymentStatusResponse paymentStatusResponse) {
        try {
            Long caseId = Long.valueOf(caseReference);
            CaseDetails caseDetails = coreCaseDataService.getCase(caseId);
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
            caseData = updateCaseDataWithPaymentDetails(paymentStatusResponse, caseData, feeType);

            createAndSubmitEvent(caseData, caseId, feeType, false);
        } catch (Exception ex) {
            log.error("Error updating payment status for case {}: {}", caseReference, ex.getMessage());
            throw new CaseDataUpdateException();
        }
    }

    private boolean isPaymentUpdateValid(FeeType feeType, CaseData caseData) {
        return (feeType == FeeType.HEARING && isHearingPaymentFailed(caseData)) ||
            (feeType == FeeType.CLAIMISSUED && isClaimIssuePaymentFailed(caseData));
    }

    private boolean isHearingPaymentFailed(CaseData caseData) {
        PaymentDetails hearingPayment = caseData.getHearingFeePaymentDetails();
        return hearingPayment == null || hearingPayment.getStatus() == PaymentStatus.FAILED;
    }

    private boolean isClaimIssuePaymentFailed(CaseData caseData) {
        PaymentDetails claimIssuePayment = caseData.getClaimIssuedPaymentDetails();
        return claimIssuePayment == null || claimIssuePayment.getStatus() == PaymentStatus.FAILED;
    }

    private void handleValidPaymentUpdate(ServiceRequestUpdateDto dto, CaseData caseData, FeeType feeType, Long caseId) {
        caseData = updateCaseDataWithPaymentDetails(dto, caseData, feeType);
        CardPaymentStatusResponse paymentStatusResponse = buildPaymentStatusResponse(dto);
        updatePaymentStatus(feeType, dto.getCcdCaseNumber(), paymentStatusResponse);
        createAndSubmitEvent(caseData, caseId, feeType, true);
    }

    private void handleNonLipvPaymentUpdate(ServiceRequestUpdateDto dto, CaseData caseData, FeeType feeType, Long caseId) {
        caseData = updateCaseDataWithPaymentDetails(dto, caseData, feeType);
        createAndSubmitEvent(caseData, caseId, feeType, true);
    }

    private CardPaymentStatusResponse buildPaymentStatusResponse(ServiceRequestUpdateDto dto) {
        return CardPaymentStatusResponse.builder()
            .paymentReference(dto.getPayment().getPaymentReference())
            .status(PaymentStatus.SUCCESS.name())
            .build();
    }

    private void createAndSubmitEvent(CaseData caseData, Long caseId, FeeType feeType, boolean isCallback) {
        CaseEvent event = determineEvent(caseData, feeType, isCallback);
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

    private CaseEvent determineEvent(CaseData caseData, FeeType feeType, boolean isCallback) {
        return switch (feeType) {
            case HEARING -> isCallback ? SERVICE_REQUEST_RECEIVED : CITIZEN_HEARING_FEE_PAYMENT;
            case CLAIMISSUED -> {
                if (isCallback) {
                    yield caseData.getCaseAccessCategory() == CaseCategory.SPEC_CLAIM
                        ? CREATE_CLAIM_SPEC_AFTER_PAYMENT
                        : CREATE_CLAIM_AFTER_PAYMENT;
                } else {
                    yield CITIZEN_CLAIM_ISSUE_PAYMENT;
                }
            }
        };
    }

    private CaseData updateCaseDataWithPaymentDetails(ServiceRequestUpdateDto dto, CaseData caseData, FeeType feeType) {
        PaymentDetails existingPayment = getPaymentDetails(feeType, caseData);
        String customerReference = Optional.ofNullable(dto.getPayment())
            .map(PaymentDto::getCustomerReference)
            .orElseGet(() -> Optional.ofNullable(existingPayment).map(PaymentDetails::getCustomerReference).orElse(null));

        PaymentDetails updatedPayment = PaymentDetails.builder()
            .status(PaymentStatus.SUCCESS)
            .customerReference(customerReference)
            .reference(dto.getPayment().getPaymentReference())
            .errorCode(null)
            .errorMessage(null)
            .build();

        return applyPaymentDetails(caseData, feeType, updatedPayment);
    }

    private CaseData updateCaseDataWithPaymentDetails(CardPaymentStatusResponse response, CaseData caseData, FeeType feeType) {
        PaymentDetails existingPayment = getPaymentDetails(feeType, caseData);

        PaymentDetails updatedPayment = PaymentDetails.builder()
            .status(PaymentStatus.valueOf(response.getStatus()))
            .reference(response.getPaymentReference())
            .errorCode(response.getErrorCode())
            .errorMessage(response.getErrorDescription())
            .build();

        if (existingPayment != null) {
            updatedPayment = existingPayment.toBuilder()
                .status(updatedPayment.getStatus())
                .reference(updatedPayment.getReference())
                .errorCode(updatedPayment.getErrorCode())
                .errorMessage(updatedPayment.getErrorMessage())
                .build();
        }

        return applyPaymentDetails(caseData, feeType, updatedPayment);
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
