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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SERVICE_REQUEST_RECEIVED;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestUpdateCallbackService {

    public static final String PAID = "Paid";
    public static final String serviceRequestReceived = "ServiceRequestReceived";
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;
    private final Time time;

    private CaseData data;

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto, String feeType) {
        log.info("Processing the callback for the caseId {} with status {}", serviceRequestUpdateDto.getCcdCaseNumber(),
                 serviceRequestUpdateDto.getServiceRequestStatus());

        if (serviceRequestUpdateDto.getServiceRequestStatus().equalsIgnoreCase(PAID)) {

            log.info("Fetching the Case details based on caseId {}", serviceRequestUpdateDto.getCcdCaseNumber());
            CaseDetails caseDetails = coreCaseDataService.getCase(Long.valueOf(serviceRequestUpdateDto
                                                                                   .getCcdCaseNumber()));
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
            caseData = updateCaseDataWithStateAndPaymentDetails(serviceRequestUpdateDto, caseData, feeType);
            if (feeType.equals(FeeType.HEARING.name()) || feeType.equals(FeeType.CLAIMISSUED.name())) {
                createEvent(caseData, serviceRequestUpdateDto.getCcdCaseNumber(), feeType);
            }

        } else {
            log.info("Service request status is not PAID for Case id {}",
                      serviceRequestUpdateDto.getCcdCaseNumber());
        }
    }

    private void createEvent(CaseData caseData, String caseId, String feeType) {

        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
            caseId,
            getEventNameFromFeeType(caseData, feeType)
        );

        CaseData startEventData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
        BusinessProcess businessProcess = null;

        if (feeType.equals(FeeType.HEARING.name())) {
            businessProcess = startEventData.getBusinessProcess()
                .updateActivityId(serviceRequestReceived);
        }

        CaseDataContent caseDataContent = buildCaseDataContent(
            startEventResponse,
            caseData,
            businessProcess
        );

        coreCaseDataService.submitUpdate(caseId, caseDataContent);
    }

    private CaseEvent getEventNameFromFeeType(CaseData caseData, String feeType) {
        if (feeType.equals(FeeType.HEARING.name())) {
            return SERVICE_REQUEST_RECEIVED;
        } else if (feeType.equals(FeeType.CLAIMISSUED.name())
                   && SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return CREATE_CLAIM_SPEC_AFTER_PAYMENT;
        } else {
            return CREATE_CLAIM_AFTER_PAYMENT;
        }
    }

    private CaseData updateCaseDataWithStateAndPaymentDetails(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                              CaseData caseData, String feeType) {

        PaymentDetails pbaDetails = getPBADetailsFromFeeType(feeType, caseData);

        String customerReference = ofNullable(serviceRequestUpdateDto.getPayment())
            .map(PaymentDto::getCustomerReference)
            .orElse(ofNullable(pbaDetails).map(PaymentDetails::getCustomerReference).orElse(null));

        PaymentDetails paymentDetails = ofNullable(pbaDetails)
            .map(PaymentDetails::toBuilder)
            .orElse(PaymentDetails.builder())
            .status(SUCCESS)
            .customerReference(customerReference)
            .reference(serviceRequestUpdateDto.getPayment().getPaymentReference())
            .errorCode(null)
            .errorMessage(null)
            .build();

        caseData = getCaseDataFromFeeType(feeType, caseData, paymentDetails);
        return caseData;
    }

    private CaseData getCaseDataFromFeeType(String feeType, CaseData caseData, PaymentDetails paymentDetails) {
        if (feeType.equals(FeeType.HEARING.name())) {
            caseData = caseData.toBuilder()
                .hearingFeePaymentDetails(paymentDetails)
                .build();
        } else if (feeType.equals(FeeType.CLAIMISSUED.name())) {
            caseData = caseData.toBuilder()
                .claimIssuedPaymentDetails(paymentDetails)
                .build();
        }
        return caseData;
    }

    private PaymentDetails getPBADetailsFromFeeType(String feeType, CaseData caseData) {
        PaymentDetails pbaDetails = null;
        if (feeType.equals(FeeType.HEARING.name())) {
            pbaDetails = caseData.getHearingFeePaymentDetails();
        } else if (feeType.equals(FeeType.CLAIMISSUED.name())) {
            pbaDetails = caseData.getClaimIssuedPaymentDetails();
        }
        return pbaDetails;
    }

    private CaseDataContent buildCaseDataContent(StartEventResponse startEventResponse, CaseData caseData,
                                                 BusinessProcess businessProcess) {

        Map<String, Object> updatedData = caseData.toMap(objectMapper);
        if (businessProcess != null) {
            updatedData.put("businessProcess", businessProcess);
        }
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId())
                       .summary(null)
                       .description(null)
                       .build())
            .data(updatedData)
            .build();
    }
}
