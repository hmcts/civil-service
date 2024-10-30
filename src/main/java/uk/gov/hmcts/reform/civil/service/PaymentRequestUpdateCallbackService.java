package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SERVICE_REQUEST_RECEIVED;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestUpdateCallbackService {

    public static final String PAID = "Paid";
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final ObjectMapper objectMapper;
    private final UpdatePaymentStatusService updatePaymentStatusService;

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto, String feeType) {
        log.info("Processing callback for caseId {} with status {}",
                 serviceRequestUpdateDto.getCcdCaseNumber(), serviceRequestUpdateDto.getServiceRequestStatus());

        if (!PAID.equalsIgnoreCase(serviceRequestUpdateDto.getServiceRequestStatus())) {
            return;
        }
        CaseDetails caseDetails = coreCaseDataService.getCase(Long.parseLong(serviceRequestUpdateDto.getCcdCaseNumber()));
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

        if (isValidFeeType(feeType) && isEligibleForUpdate(feeType, caseData)) {
            caseData = updateCaseDataWithStateAndPaymentDetails(serviceRequestUpdateDto, caseData, feeType);
            if (caseData.isLipvLipOneVOne()) {
                updatePaymentStatusService.updatePaymentStatus(FeeType.valueOf(feeType),
                                                               serviceRequestUpdateDto.getCcdCaseNumber(), buildCardPaymentStatusResponse(serviceRequestUpdateDto));
            } else {
                createEvent(caseData, serviceRequestUpdateDto.getCcdCaseNumber(), feeType);
            }
        }
    }

    private boolean isValidFeeType(String feeType) {
        return FeeType.HEARING.name().equals(feeType) || FeeType.CLAIMISSUED.name().equals(feeType);
    }

    private boolean isEligibleForUpdate(String feeType, CaseData caseData) {
        return (FeeType.HEARING.name().equals(feeType) && isPaymentFailed(caseData.getHearingFeePaymentDetails()))
            || (FeeType.CLAIMISSUED.name().equals(feeType) && isPaymentFailed(caseData.getClaimIssuedPaymentDetails()));
    }

    private boolean isPaymentFailed(PaymentDetails paymentDetails) {
        return paymentDetails == null || FAILED.equals(paymentDetails.getStatus());
    }

    private CardPaymentStatusResponse buildCardPaymentStatusResponse(ServiceRequestUpdateDto serviceRequestUpdateDto) {
        return CardPaymentStatusResponse.builder()
            .paymentReference(serviceRequestUpdateDto.getPayment().getPaymentReference())
            .status(SUCCESS.name())
            .build();
    }

    private void createEvent(CaseData caseData, String caseId, String feeType) {
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
            caseId,
            getEventNameFromFeeType(caseData, feeType)
        );

        CaseDataContent caseDataContent = buildCaseDataContent(startEventResponse, caseData);
        coreCaseDataService.submitUpdate(caseId, caseDataContent);
    }

    private CaseData updateCaseDataWithStateAndPaymentDetails(ServiceRequestUpdateDto serviceRequestUpdateDto, CaseData caseData, String feeType) {
        PaymentDetails updatedPaymentDetails = PaymentDetails.builder()
            .status(SUCCESS)
            .customerReference(serviceRequestUpdateDto.getPayment().getCustomerReference())
            .reference(serviceRequestUpdateDto.getPayment().getPaymentReference())
            .build();

        return getUpdatedCaseData(feeType, caseData, updatedPaymentDetails);
    }

    private CaseData getUpdatedCaseData(String feeType, CaseData caseData, PaymentDetails paymentDetails) {
        return FeeType.HEARING.name().equals(feeType) ? caseData.toBuilder().hearingFeePaymentDetails(paymentDetails).build()
            : caseData.toBuilder().claimIssuedPaymentDetails(paymentDetails).build();
    }

    private CaseEvent getEventNameFromFeeType(CaseData caseData, String feeType) {
        if (FeeType.HEARING.name().equals(feeType)) {
            return SERVICE_REQUEST_RECEIVED;
        } else if (FeeType.CLAIMISSUED.name().equals(feeType) && SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return CREATE_CLAIM_SPEC_AFTER_PAYMENT;
        } else {
            return CREATE_CLAIM_AFTER_PAYMENT;
        }
    }

    private CaseDataContent buildCaseDataContent(StartEventResponse startEventResponse, CaseData caseData) {
        Map<String, Object> updatedData = caseData.toMap(objectMapper);
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(updatedData)
            .build();
    }
}
