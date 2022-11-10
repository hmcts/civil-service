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
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SERVICE_REQUEST_RECEIVED;
import static uk.gov.hmcts.reform.civil.enums.FeeType.CLAIMISSUED;
import static uk.gov.hmcts.reform.civil.enums.FeeType.HEARING;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestUpdateCallbackService {

    public static final String PAID = "Paid";
    public static final String serviceRequestReceived = "ServiceRequestReceived";
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final ObjectMapper objectMapper;
    private final Time time;

    private CaseData data;

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto , String feeType) {
        log.info("Processing the callback for the caseId {} with status {}", serviceRequestUpdateDto.getCcdCaseNumber(),
                 serviceRequestUpdateDto.getServiceRequestStatus());

        if (serviceRequestUpdateDto.getServiceRequestStatus().equalsIgnoreCase(PAID)) {

            log.info("Fetching the Case details based on caseId {}", serviceRequestUpdateDto.getCcdCaseNumber());
            CaseDetails caseDetails = coreCaseDataService.getCase(Long.valueOf(serviceRequestUpdateDto
                                                                                   .getCcdCaseNumber()));
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
            caseData = updateCaseDataWithStateAndPaymentDetails(serviceRequestUpdateDto, caseData, feeType);
            if(feeType.equals(HEARING.name())) {
                createEvent(caseData, SERVICE_REQUEST_RECEIVED,
                            serviceRequestUpdateDto.getCcdCaseNumber()
                );

            } else if (feeType.equals(CLAIMISSUED.name())) {
                createEvent(caseData, CREATE_CLAIM_SPEC_AFTER_PAYMENT,
                            serviceRequestUpdateDto.getCcdCaseNumber()
                );
            }

        } else {

            log.error("Case id {} not present", serviceRequestUpdateDto.getCcdCaseNumber());
        }
    }

    private void createEvent(CaseData caseData, CaseEvent eventName, String CaseId) {

        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
            CaseId,
            eventName
        );

        BusinessProcess businessProcess = null;

        CaseDataContent caseDataContent = buildCaseDataContent(
            startEventResponse,
            caseData,
            businessProcess
        );

        coreCaseDataService.submitUpdate(CaseId, caseDataContent);
    }

    private CaseData updateCaseDataWithStateAndPaymentDetails(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                              CaseData caseData, String feeType) {
        PaymentDetails pbaDetails = null;
        if(feeType.equals(HEARING.name())) {
            pbaDetails = caseData.getHearingFeePaymentDetails();
        } else if (feeType.equals(CLAIMISSUED.name())) {
            pbaDetails = caseData.getClaimIssuedPaymentDetails();
        }
        String customerReference = ofNullable(serviceRequestUpdateDto.getPayment())
            .map(PaymentDto::getCustomerReference)
            .orElse(pbaDetails.getCustomerReference());

        PaymentDetails paymentDetails = ofNullable(pbaDetails)
            .map(PaymentDetails::toBuilder)
            .orElse(PaymentDetails.builder())
            .status(SUCCESS)
            .customerReference(customerReference)
            .reference(serviceRequestUpdateDto.getPayment().getPaymentReference())
            .errorCode(null)
            .errorMessage(null)
            .build();

        if(feeType.equals(HEARING)) {
            caseData = caseData.toBuilder()
                .hearingFeePaymentDetails(paymentDetails)
                .build();
        } else if (feeType.equals(CLAIMISSUED)){
            caseData = caseData.toBuilder()
                .claimIssuedPaymentDetails(paymentDetails)
                .build();
        }
        return caseData;
    }

    private CaseDataContent buildCaseDataContent(StartEventResponse startEventResponse, CaseData caseData,
                                                 BusinessProcess businessProcess) {

        Map<String, Object> updatedData = caseData.toMap(objectMapper);
        updatedData.put("businessProcess", businessProcess);

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
