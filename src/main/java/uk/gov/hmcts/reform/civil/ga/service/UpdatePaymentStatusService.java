package uk.gov.hmcts.reform.civil.ga.service;

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
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.utils.PaymentUtils;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdatePaymentStatusService {

    private final CaseDetailsConverter caseDetailsConverter;
    private final GaCoreCaseDataService gaCoreCaseDataService;
    private final ObjectMapper objectMapper;

    @Retryable(retryFor = CaseDataUpdateException.class, noRetryFor = IllegalArgumentException.class, backoff = @Backoff(delay = 500))
    public void updatePaymentStatus(String caseReference, CardPaymentStatusResponse cardPaymentStatusResponse) {
        log.info("Starting updatePaymentStatus for caseReference: {}", caseReference);
        log.debug("CardPaymentStatusResponse received: {}", cardPaymentStatusResponse);

        try {
            CaseDetails caseDetails = gaCoreCaseDataService.getCase(Long.valueOf(caseReference));
            GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);
            caseData = updateCaseDataWithStateAndPaymentDetails(cardPaymentStatusResponse, caseData);

            log.info("Creating event for updated payment status on caseReference: {}", caseReference);
            createEvent(caseData, caseReference);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CaseDataUpdateException(ex.getMessage(), ex);
        }
    }

    @Recover
    public void recover(CaseDataUpdateException ex, String caseReference, CardPaymentStatusResponse cardPaymentStatusResponse) {
        String status = cardPaymentStatusResponse != null ? cardPaymentStatusResponse.getStatus() : "N/A";
        String errorCode = cardPaymentStatusResponse != null ? cardPaymentStatusResponse.getErrorCode() : "N/A";

        log.error(
            "GA Payment status update failed after retries for case {}. Status: {}, ErrorCode: {}",
            caseReference,
            status,
            errorCode,
            ex
        );
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

        GeneralApplicationCaseData freshData = caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails());
        PaymentDetails intendedPayment = getPaymentDetails(caseData);
        PaymentDetails freshPayment = getPaymentDetails(freshData);

        if (PaymentUtils.isPaymentAlreadyApplied(intendedPayment, freshPayment)) {
            String reference = freshPayment != null ? freshPayment.getReference() : "N/A";
            log.info("{} Payment with reference {} already applied for GA case {}. Skipping submission.",
                     caseData.isAdditionalFeeRequested() ? "Additional" : "Application",
                     reference, caseReference);
            return;
        }

        GeneralApplicationPbaDetails freshPba = freshData.getGeneralAppPBADetails() != null
            ? freshData.getGeneralAppPBADetails().copy()
            : new GeneralApplicationPbaDetails();

        GeneralApplicationPbaDetails stalePba = caseData.getGeneralAppPBADetails();

        if (caseData.isAdditionalFeeRequested()) {
            freshPba.setAdditionalPaymentDetails(stalePba.getAdditionalPaymentDetails());
        } else {
            freshPba.setPaymentDetails(stalePba.getPaymentDetails());
        }

        freshData.setGeneralAppPBADetails(freshPba);

        CaseDataContent caseDataContent = buildCaseDataContent(
            startEventResponse,
            freshData
        );

        log.info("Submitting case update with new data for caseReference: {}", caseReference);
        gaCoreCaseDataService.submitUpdate(caseReference, caseDataContent);
    }

    private PaymentDetails getPaymentDetails(GeneralApplicationCaseData caseData) {
        GeneralApplicationPbaDetails pbaDetails = caseData.getGeneralAppPBADetails();
        if (pbaDetails == null) {
            return null;
        }
        return caseData.isAdditionalFeeRequested()
            ? pbaDetails.getAdditionalPaymentDetails()
            : pbaDetails.getPaymentDetails();
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
        GeneralApplicationPbaDetails updatedPbaDetails = pbaDetails == null
            ? new GeneralApplicationPbaDetails()
            : pbaDetails.copy();

        PaymentDetails paymentDetails = new PaymentDetails()
            .setStatus(PaymentStatus.resolvePaymentStatus(cardPaymentStatusResponse.getStatus()))
            .setReference(cardPaymentStatusResponse.getPaymentReference())
            .setErrorCode(cardPaymentStatusResponse.getErrorCode())
            .setErrorMessage(cardPaymentStatusResponse.getErrorDescription())
            ;
        if (caseData.isAdditionalFeeRequested()) {
            updatedPbaDetails.setAdditionalPaymentDetails(paymentDetails);
            log.info("Applied additional payment details for caseReference: {}", caseData.getCcdCaseReference());
        } else {
            updatedPbaDetails.setPaymentDetails(paymentDetails);
            log.info("Applied standard payment details for caseReference: {}", caseData.getCcdCaseReference());
        }
        return caseData.copy()
            .generalAppPBADetails(updatedPbaDetails)
            .build();
    }
}
