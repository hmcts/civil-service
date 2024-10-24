package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SERVICE_REQUEST_RECEIVED;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProcessingHelper {

    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final ObjectMapper objectMapper;

    public CaseData getCaseData(String caseId) {
        log.info("Fetching the Case details based on caseId {}", caseId);
        CaseDetails caseDetails = coreCaseDataService.getCase(Long.valueOf(caseId));
        return caseDetailsConverter.toCaseData(caseDetails);
    }

    public CaseDataContent buildCaseDataContent(StartEventResponse startEventResponse, CaseData caseData) {
        Map<String, Object> updatedData = caseData.toMap(objectMapper);
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(updatedData)
            .build();
    }

    public PaymentDetails retrievePaymentDetails(String feeType, CaseData caseData) {
        return switch (FeeType.valueOf(feeType)) {
            case HEARING -> caseData.getHearingFeePaymentDetails();
            case CLAIMISSUED -> caseData.getClaimIssuedPaymentDetails();
        };
    }

    public CaseData updateCaseDataWithPaymentDetails(String feeType, CaseData caseData, PaymentDetails paymentDetails) {
        return switch (FeeType.valueOf(feeType)) {
            case HEARING -> caseData.toBuilder().hearingFeePaymentDetails(paymentDetails).build();
            case CLAIMISSUED -> caseData.toBuilder().claimIssuedPaymentDetails(paymentDetails).build();
        };
    }

    public CaseEvent getEventNameFromFeeType(CaseData caseData, String feeType, String serviceIdentifier) {
        return switch (serviceIdentifier) {
            case "PaymentRequestUpdate" -> resolvePaymentRequestUpdateEvent(caseData, feeType);
            case "UpdatePaymentStatus" -> resolveUpdatePaymentStatusEvent(feeType);
            default -> throw new IllegalArgumentException("Unknown service identifier: " + serviceIdentifier);
        };
    }

    private CaseEvent resolvePaymentRequestUpdateEvent(CaseData caseData, String feeType) {
        return switch (FeeType.valueOf(feeType)) {
            case HEARING -> SERVICE_REQUEST_RECEIVED;
            case CLAIMISSUED -> CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                ? CREATE_CLAIM_SPEC_AFTER_PAYMENT
                : CREATE_CLAIM_AFTER_PAYMENT;
        };
    }

    private CaseEvent resolveUpdatePaymentStatusEvent(String feeType) {
        return switch (FeeType.valueOf(feeType)) {
            case HEARING -> CaseEvent.CITIZEN_HEARING_FEE_PAYMENT;
            case CLAIMISSUED -> CaseEvent.CITIZEN_CLAIM_ISSUE_PAYMENT;
        };
    }

    public void createAndSubmitEvent(CaseData caseData, String caseId, String feeType, String serviceIdentifier) {
        CaseEvent caseEvent = getEventNameFromFeeType(caseData, feeType, serviceIdentifier);
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, caseEvent);
        CaseDataContent caseDataContent = buildCaseDataContent(startEventResponse, caseData);
        coreCaseDataService.submitUpdate(caseId, caseDataContent);
    }

    public boolean isValidPaymentUpdateHearing(String feeType, CaseData caseData) {
        return FeeType.HEARING.name().equals(feeType)
            && (caseData.getHearingFeePaymentDetails() == null
            || caseData.getHearingFeePaymentDetails().getStatus() == PaymentStatus.FAILED);
    }

    public boolean isValidUpdatePaymentClaimIssue(String feeType, CaseData caseData) {
        return FeeType.CLAIMISSUED.name().equals(feeType)
            && (caseData.getClaimIssuedPaymentDetails() == null
            || caseData.getClaimIssuedPaymentDetails().getStatus() == PaymentStatus.FAILED);
    }
}
