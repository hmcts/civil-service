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
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaPaymentRequestUpdateCallbackService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;
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
    private final GaPaymentRequestUpdateCallbackService gaPaymentRequestUpdateCallbackService;
    private final ObjectMapper objectMapper;

    public void processCallback(ServiceRequestUpdateDto dto, String feeTypeStr) {
        log.info("Processing callback for caseId {} with status {}",
                dto.getCcdCaseNumber(),
                dto.getServiceRequestStatus());

        if (!isPaid(dto)) {
            log.info("Service request status is not 'Paid'. No action required for case {}", dto.getCcdCaseNumber());
            return;
        }

        FeeType feeType = getFeeType(feeTypeStr);
        if (!isHandledFeeType(feeType) || feeType == null) {
            log.info("FeeType {} is not handled or invalid. No action required for case {}", feeType, dto.getCcdCaseNumber());
            return;
        }

        Long caseId = Long.valueOf(dto.getCcdCaseNumber());
        log.info("Fetching case details for caseId {}", caseId);
        log.debug("ServiceRequestUpdateDto: {}", dto);

        CaseDetails caseDetails = coreCaseDataService.getCase(caseId);

        if (Objects.equals(caseDetails.getCaseTypeId(), GENERALAPPLICATION_CASE_TYPE) && dto.getServiceRequestStatus().equalsIgnoreCase(PAID)) {
            log.info("Processing payment callback for General Application Case details for caseId {}", caseId);

            GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);
            gaPaymentRequestUpdateCallbackService.processServiceRequest(dto, caseData, false);
        } else {
            log.info("Processing payment callback for Civil Case details for caseId {}", caseId);

            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

            if ((caseData.isLipvLipOneVOne() && isPaymentUpdateValid(
                feeType,
                caseData
            )) || !caseData.isLipvLipOneVOne()) {
                handlePaymentUpdate(dto, caseData, feeType);
            }
        }
    }

    private void handlePaymentUpdate(ServiceRequestUpdateDto dto, CaseData caseData, FeeType feeType) {
        CardPaymentStatusResponse paymentStatusResponse = buildPaymentStatusResponse(dto);
        String customerReference = getCustomerReference(dto, caseData, feeType);
        updatePaymentStatus(feeType, dto.getCcdCaseNumber(), updateCaseDataWithPaymentDetails(paymentStatusResponse, caseData, feeType, customerReference));
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
    public void updatePaymentStatus(FeeType feeType, String caseReference, CaseData caseData) {
        try {
            Long caseId = Long.valueOf(caseReference);
            submitUpdatePaymentEvent(caseData, caseId, feeType);
        } catch (Exception ex) {
            log.error("Error updating payment status for case {}: {}", caseReference, ex.getMessage());
            throw new CaseDataUpdateException();
        }
    }

    @Retryable(value = CaseDataUpdateException.class, backoff = @Backoff(delay = 500))
    public void updatePaymentStatus(FeeType feeType, String caseReference, CardPaymentStatusResponse paymentStatusResponse) {
        try {
            Long caseId = Long.valueOf(caseReference);
            CaseDetails caseDetails = coreCaseDataService.getCase(caseId);
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
            caseData = updateCaseDataWithPaymentDetails(paymentStatusResponse, caseData, feeType);

            submitUpdatePaymentEvent(caseData, caseId, feeType);
        } catch (Exception ex) {
            log.error("Error updating payment status for case {}: {}", caseReference, ex.getMessage());
            throw new CaseDataUpdateException();
        }
    }

    private CardPaymentStatusResponse buildPaymentStatusResponse(ServiceRequestUpdateDto dto) {
        return CardPaymentStatusResponse.builder()
                .paymentReference(dto.getPayment().getPaymentReference())
                .status(PaymentStatus.SUCCESS.name())
                .build();
    }

    private void submitUpdatePaymentEvent(CaseData caseData, Long caseId, FeeType feeType) {
        CaseEvent event = determineEventFromFeeType(caseData, feeType);
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

    private CaseEvent determineEventFromFeeType(CaseData caseData, FeeType feeType) {
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

    private CaseData updateCaseDataWithPaymentDetails(CardPaymentStatusResponse response,
                                                      CaseData caseData,
                                                      FeeType feeType) {
        return updateCaseDataWithPaymentDetails(response, caseData, feeType, null);
    }

    private CaseData updateCaseDataWithPaymentDetails(CardPaymentStatusResponse response,
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

    private PaymentDetails getPaymentDetails(FeeType feeType, CaseData caseData) {
        return switch (feeType) {
            case HEARING -> caseData.getHearingFeePaymentDetails();
            case CLAIMISSUED -> caseData.getClaimIssuedPaymentDetails();
            default -> throw new IllegalArgumentException("Unsupported fee type for payment details: " + feeType);
        };
    }

    private CaseData applyPaymentDetails(CaseData caseData, FeeType feeType, PaymentDetails paymentDetails) {
        switch (feeType) {
            case HEARING -> caseData.setHearingFeePaymentDetails(paymentDetails);
            case CLAIMISSUED -> caseData.setClaimIssuedPaymentDetails(paymentDetails);
            default -> throw new IllegalArgumentException("Unsupported fee type for case update: " + feeType);
        }
        return caseData;
    }

    private PaymentStatus resolvePaymentStatus(String status) {
        return status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;
    }

    private String getCustomerReference(ServiceRequestUpdateDto dto, CaseData caseData, FeeType feeType) {
        return Optional.ofNullable(dto.getPayment())
                .map(PaymentDto::getCustomerReference)
                .orElse(Optional.ofNullable(getPaymentDetails(feeType, caseData))
                        .map(PaymentDetails::getCustomerReference)
                        .orElse(null));
    }
}
