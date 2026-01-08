package uk.gov.hmcts.reform.civil.ga.service;

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
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdatePaymentStatusService {

    private final CaseDetailsConverter caseDetailsConverter;
    private final GaCoreCaseDataService gaCoreCaseDataService;
    private final ObjectMapper objectMapper;

    @Retryable(value = CaseDataUpdateException.class, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public void updatePaymentStatus(String caseReference, CardPaymentStatusResponse cardPaymentStatusResponse) {
        log.info("Starting updatePaymentStatus for caseReference: {}", caseReference);
        log.debug("CardPaymentStatusResponse received: {}", cardPaymentStatusResponse);

        try {
            CaseDetails caseDetails = gaCoreCaseDataService.getCase(Long.valueOf(caseReference));
            GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);
            caseData = updateCaseDataWithStateAndPaymentDetails(cardPaymentStatusResponse, caseData);

            log.info("Creating event for updated payment status on caseReference: {}", caseReference);
            createEvent(caseData, caseReference);
        } catch (Exception ex) {
            throw new CaseDataUpdateException();
        }
    }

    private void createEvent(GeneralApplicationCaseData caseData, String caseReference) {
        CaseEvent caseEvent = caseData.isAdditionalFeeRequested()
            ? CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID
            : CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;
        log.info("Starting event creation with caseEvent: {} for caseReference: {}", caseEvent, caseReference);
        StartEventResponse startEventResponse = gaCoreCaseDataService.startUpdate(
            caseReference,
            caseEvent
        );

        CaseDataContent caseDataContent = buildCaseDataContent(
            startEventResponse,
            caseData
        );

        log.info("Submitting case update with new data for caseReference: {}", caseReference);
        gaCoreCaseDataService.submitUpdate(caseReference, caseDataContent);
    }

    private CaseDataContent buildCaseDataContent(StartEventResponse startEventResponse, GeneralApplicationCaseData caseData) {

        Map<String, Object> updatedData = caseData.toMap(objectMapper);
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId())
                       .summary(null)
                       .description(null)
                       .build())
            .data(updatedData)
            .build();
    }

    private GeneralApplicationCaseData updateCaseDataWithStateAndPaymentDetails(CardPaymentStatusResponse cardPaymentStatusResponse,
                                                                                GeneralApplicationCaseData caseData) {
        log.info("Updating CaseData with new payment status for caseReference: {}", caseData.getCcdCaseReference());

        GeneralApplicationPbaDetails pbaDetails = caseData.getGeneralAppPBADetails();
        GeneralApplicationPbaDetails.GeneralApplicationPbaDetailsBuilder pbaDetailsBuilder;
        pbaDetailsBuilder = pbaDetails == null ? GeneralApplicationPbaDetails.builder() : pbaDetails.toBuilder();

        PaymentDetails paymentDetails = PaymentDetails.builder()
            .status(PaymentStatus.valueOf(cardPaymentStatusResponse.getStatus().toUpperCase()))
            .reference(cardPaymentStatusResponse.getPaymentReference())
            .errorCode(cardPaymentStatusResponse.getErrorCode())
            .errorMessage(cardPaymentStatusResponse.getErrorDescription())
            .build();
        if (caseData.isAdditionalFeeRequested()) {
            pbaDetails = pbaDetailsBuilder.additionalPaymentDetails(paymentDetails).build();
            log.info("Applied additional payment details for caseReference: {}", caseData.getCcdCaseReference());
        } else {
            pbaDetails = pbaDetailsBuilder.paymentDetails(paymentDetails).build();
            log.info("Applied standard payment details for caseReference: {}", caseData.getCcdCaseReference());
        }
        return caseData.toBuilder()
            .generalAppPBADetails(pbaDetails)
            .build();
    }
}
