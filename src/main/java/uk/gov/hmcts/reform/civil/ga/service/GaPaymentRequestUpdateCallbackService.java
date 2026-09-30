package uk.gov.hmcts.reform.civil.ga.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.util.Map;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.isGeneralAppConsentOrder;
import static uk.gov.hmcts.reform.civil.utils.PaymentUtils.isPaymentAlreadyApplied;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaPaymentRequestUpdateCallbackService {

    private final CaseDetailsConverter caseDetailsConverter;
    private final GaCoreCaseDataService coreCaseDataService;
    private final JudicialNotificationService judicialNotificationService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final StateGeneratorService stateGeneratorService;

    public GeneralApplicationCaseData processHwf(GeneralApplicationCaseData caseData) {
        ServiceRequestUpdateDto serviceRequestUpdateDto = new ServiceRequestUpdateDto()
            .setCcdCaseNumber(caseData.getCcdCaseReference().toString())
            .setPayment(PaymentDto.builder()
                .customerReference(caseData.getGeneralAppHelpWithFees()
                    .getHelpWithFeesReferenceNumber())
                .build());
        return processServiceRequest(serviceRequestUpdateDto, caseData, true);
    }

    @Retryable(retryFor = CaseDataUpdateException.class, noRetryFor = IllegalArgumentException.class, backoff = @Backoff(delay = 500))
    public GeneralApplicationCaseData processServiceRequest(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                            GeneralApplicationCaseData caseData,
                                                            boolean hwf) {
        try {
            if (!Objects.isNull(caseData)) {

                switch (caseData.getCcdState()) {
                    case APPLICATION_ADD_PAYMENT:
                        return processAndTriggerAdditionalPayment(caseData, serviceRequestUpdateDto, hwf);
                    case AWAITING_APPLICATION_PAYMENT:
                        return processAndTriggerAwaitingPayment(caseData, serviceRequestUpdateDto, hwf);
                    default:
                        log.error("This Case id {} is not in a valid state APPLICATION_ADD_PAYMENT,"
                                      + "AWAITING_APPLICATION_PAYMENT to process payment callback ",
                                  serviceRequestUpdateDto.getCcdCaseNumber());
                }
            } else {
                log.error("Case id {} not present", serviceRequestUpdateDto.getCcdCaseNumber());
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CaseDataUpdateException(ex.getMessage(), ex);
        }
        return null;
    }

    @Recover
    public void recover(CaseDataUpdateException ex, ServiceRequestUpdateDto serviceRequestUpdateDto) {
        String status = serviceRequestUpdateDto != null ? serviceRequestUpdateDto.getServiceRequestStatus() : "N/A";
        String caseNumber = serviceRequestUpdateDto != null ? serviceRequestUpdateDto.getCcdCaseNumber() : "N/A";

        log.error(
            "Payment status update failed after retries for case {}. Status: {}",
            caseNumber,
            status,
            ex);
    }

    private GeneralApplicationCaseData processAndTriggerAwaitingPayment(GeneralApplicationCaseData caseData,
                                                  ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                  boolean hwf) {
        log.info("Processing the callback for Application Payment "
                     + "for the caseId {}", serviceRequestUpdateDto.getCcdCaseNumber());
        caseData = updateCaseDataWithPaymentDetails(serviceRequestUpdateDto, caseData);
        if (!hwf) {
            createEvent(caseData, INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT,
                    serviceRequestUpdateDto.getCcdCaseNumber());
        }
        return caseData;
    }

    private GeneralApplicationCaseData processAndTriggerAdditionalPayment(GeneralApplicationCaseData caseData,
                                                    ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                    boolean hwf) {
        log.info("Processing the callback for making Additional Payment"
                     + "for the caseId {}", serviceRequestUpdateDto.getCcdCaseNumber());
        if (!isGeneralAppConsentOrder(caseData)) {
            judicialNotificationService.sendNotification(caseData, "respondent");
        }

        caseData = updateCaseDataWithStateAndPaymentDetails(serviceRequestUpdateDto, caseData);
        if (!hwf) {
            createEvent(caseData, MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID,
                        serviceRequestUpdateDto.getCcdCaseNumber());
        }
        return caseData;
    }

    private GeneralApplicationCaseData updateCaseDataWithPaymentDetails(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                                        GeneralApplicationCaseData caseData) {
        GeneralApplicationPbaDetails pbaDetails = caseData.getGeneralAppPBADetails();
        PaymentDetails paymentDetails = buildPaymentDetails(serviceRequestUpdateDto,
                                                            pbaDetails.getPaymentDetails(),
                                                            pbaDetails.getServiceReqReference());

        GeneralApplicationPbaDetails updatedPbaDetails = pbaDetails.copy()
            .setPaymentDetails(paymentDetails)
            .setPaymentSuccessfulDate(time.now());

        return caseData.copy()
            .generalAppPBADetails(updatedPbaDetails)
            .build();
    }

    private GeneralApplicationCaseData updateCaseDataWithStateAndPaymentDetails(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                                                GeneralApplicationCaseData caseData) {
        GeneralApplicationPbaDetails pbaDetails = caseData.getGeneralAppPBADetails();
        PaymentDetails paymentDetails = buildPaymentDetails(serviceRequestUpdateDto,
                                                            pbaDetails.getAdditionalPaymentDetails(),
                                                            pbaDetails.getAdditionalPaymentServiceRef());

        GeneralApplicationPbaDetails updatedPbaDetails = pbaDetails.copy()
            .setAdditionalPaymentDetails(paymentDetails)
            .setPaymentSuccessfulDate(time.now());

        return caseData.copy()
            .ccdState(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData))
            .generalAppPBADetails(updatedPbaDetails)
            .build();
    }

    private PaymentDetails buildPaymentDetails(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                               PaymentDetails existingDetails,
                                               String fallbackCustomerReference) {
        PaymentDto payment = serviceRequestUpdateDto.getPayment();
        String customerReference = ofNullable(payment)
            .map(PaymentDto::getCustomerReference)
            .orElse(fallbackCustomerReference);
        String paymentReference = ofNullable(payment)
            .map(PaymentDto::getPaymentReference)
            .orElse(customerReference);

        return ofNullable(existingDetails)
            .map(PaymentDetails::copy)
            .orElse(new PaymentDetails())
            .setStatus(SUCCESS)
            .setCustomerReference(customerReference)
            .setReference(paymentReference)
            .setErrorCode(null)
            .setErrorMessage(null);
    }

    private void createEvent(GeneralApplicationCaseData updatedStaleCaseData, CaseEvent eventName, String generalApplicationCaseId) {
        StartEventResponse startEventResponse = coreCaseDataService.startGaUpdate(
            generalApplicationCaseId,
            eventName
        );
        GeneralApplicationCaseData freshData = caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails());

        PaymentDetails intendedPayment = getPaymentDetails(eventName, updatedStaleCaseData);
        PaymentDetails freshPayment = getPaymentDetails(eventName, freshData);

        if (isPaymentAlreadyApplied(intendedPayment, freshPayment)) {
            log.info("{} Payment already applied for case {}. Skipping submission.",
                     eventName == INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT ? "Application" : "Additional", generalApplicationCaseId);
            return;
        }

        populateFreshData(updatedStaleCaseData, eventName, freshData);

        CaseDataContent caseDataContent = buildCaseDataContent(
            startEventResponse,
            freshData,
            freshData.getBusinessProcess(),
            generalApplicationCaseId,
            freshData.getGeneralAppParentCaseLink()
        );
        coreCaseDataService.submitGaUpdate(generalApplicationCaseId, caseDataContent);
    }

    private static void populateFreshData(GeneralApplicationCaseData updatedStaleCaseData, CaseEvent eventName, GeneralApplicationCaseData freshData) {
        GeneralApplicationPbaDetails freshPba = ofNullable(freshData.getGeneralAppPBADetails())
            .map(GeneralApplicationPbaDetails::copy)
            .orElse(new GeneralApplicationPbaDetails());

        GeneralApplicationPbaDetails stalePba = updatedStaleCaseData.getGeneralAppPBADetails();

        if (eventName == INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT) {
            freshPba.setPaymentDetails(stalePba.getPaymentDetails());
        } else if (eventName == MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID) {
            freshPba.setAdditionalPaymentDetails(stalePba.getAdditionalPaymentDetails());
        }
        freshPba.setPaymentSuccessfulDate(stalePba.getPaymentSuccessfulDate());

        freshData.setGeneralAppPBADetails(freshPba);
    }

    private PaymentDetails getPaymentDetails(CaseEvent eventName, GeneralApplicationCaseData caseData) {
        GeneralApplicationPbaDetails pbaDetails = caseData.getGeneralAppPBADetails();
        if (pbaDetails == null) {
            return null;
        }
        if (eventName == INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT) {
            return pbaDetails.getPaymentDetails();
        } else if (eventName == MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID) {
            return pbaDetails.getAdditionalPaymentDetails();
        }
        return null;
    }

    private CaseDataContent buildCaseDataContent(StartEventResponse startEventResponse, GeneralApplicationCaseData caseData,
                                                 BusinessProcess businessProcess, String caseId,
                                                 GeneralAppParentCaseLink generalAppParentCaseLink) {
        Map<String, Object> updatedData = caseData.toMap(objectMapper);
        updatedData.put("businessProcess", businessProcess);

        if (generalAppParentCaseLink == null
            || StringUtils.isBlank(generalAppParentCaseLink.getCaseReference())) {
            updatedData.put("generalAppParentCaseLink", new GeneralAppParentCaseLink()
                .setCaseReference(caseId));
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
