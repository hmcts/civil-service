package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SERVICE_REQUEST_RECEIVED;

@Component
@RequiredArgsConstructor
public class PaymentServiceHelper {

    private final CoreCaseDataService coreCaseDataService;
    private final ObjectMapper objectMapper;

    public void createEvent(CaseData caseData, String caseId, String feeType) {
        CaseEvent event = determineEventByFeeType(feeType);
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, event);
        CaseDataContent caseDataContent = buildCaseDataContent(startEventResponse, caseData);
        coreCaseDataService.submitUpdate(caseId, caseDataContent);
    }

    public CaseData updateCaseDataByFeeType(CaseData caseData, String feeType, PaymentDetails paymentDetails) {
        return FeeType.HEARING.name().equals(feeType)
                ? caseData.toBuilder().hearingFeePaymentDetails(paymentDetails).build()
                : caseData.toBuilder().claimIssuedPaymentDetails(paymentDetails).build();
    }

    public PaymentDetails buildPaymentDetails(CardPaymentStatusResponse response) {
        return PaymentDetails.builder()
                .status(PaymentStatus.valueOf(response.getStatus().toUpperCase()))
                .reference(response.getPaymentReference())
                .build();
    }

    private CaseDataContent buildCaseDataContent(StartEventResponse startEventResponse, CaseData caseData) {
        Map<String, Object> updatedData = caseData.toMap(objectMapper);
        return CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder().id(startEventResponse.getEventId()).build())
                .data(updatedData)
                .build();
    }

    private CaseEvent determineEventByFeeType(String feeType) {
        if (FeeType.HEARING.name().equals(feeType)) {
            return SERVICE_REQUEST_RECEIVED;
        }
        return CREATE_CLAIM_AFTER_PAYMENT;
    }
}
