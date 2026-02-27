package uk.gov.hmcts.reform.civil.ga.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.util.Map;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.isGeneralAppConsentOrder;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaPaymentRequestUpdateCallbackService {

    public static final String PAID = "Paid";

    private final CaseDetailsConverter caseDetailsConverter;
    private final GaCoreCaseDataService coreCaseDataService;
    private final JudicialNotificationService judicialNotificationService;
    private final GeneralApplicationCreationNotificationService gaNotificationService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final StateGeneratorService stateGeneratorService;

    private GeneralApplicationCaseData data;

    public GeneralApplicationCaseData processHwf(GeneralApplicationCaseData caseData) {
        ServiceRequestUpdateDto serviceRequestUpdateDto = new ServiceRequestUpdateDto()
            .setCcdCaseNumber(caseData.getCcdCaseReference().toString())
            .setPayment(PaymentDto.builder()
                .customerReference(caseData.getGeneralAppHelpWithFees()
                    .getHelpWithFeesReferenceNumber())
                .build());
        return processServiceRequest(serviceRequestUpdateDto, caseData, true);
    }

    public GeneralApplicationCaseData processServiceRequest(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                            GeneralApplicationCaseData caseData,
                                                            boolean hwf) {
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
        return null;
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
        try {

            if (!isGeneralAppConsentOrder(caseData)) {
                judicialNotificationService.sendNotification(caseData, "respondent");
            }

            caseData = updateCaseDataWithStateAndPaymentDetails(serviceRequestUpdateDto, caseData);
            if (!hwf) {
                createEvent(caseData, MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID,
                        serviceRequestUpdateDto.getCcdCaseNumber());
            }
            return caseData;

        } catch (NotificationException e) {
            log.info("processing callback failed at Judicial Notification service, "
                         + "please update the caseData with ga status "
                         + "along with the Additional payment details "
                         + "and trigger MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID event  %s ", e);
        }
        return null;
    }

    private GeneralApplicationCaseData updateCaseDataWithPaymentDetails(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                      GeneralApplicationCaseData caseData) {
        GeneralApplicationPbaDetails pbaDetails = caseData.getGeneralAppPBADetails();
        String paymentReference = ofNullable(serviceRequestUpdateDto.getPayment())
            .map(PaymentDto::getCustomerReference)
            .orElse(pbaDetails.getServiceReqReference());

        PaymentDetails paymentDetails = ofNullable(pbaDetails.getPaymentDetails())
            .map(PaymentDetails::copy)
            .orElse(new PaymentDetails())
            .setStatus(SUCCESS)
            .setCustomerReference(paymentReference)
            .setReference(serviceRequestUpdateDto.getPayment().getPaymentReference())
            .setErrorCode(null)
            .setErrorMessage(null)
            ;

        GeneralApplicationPbaDetails updatedPbaDetails = pbaDetails.copy();
        updatedPbaDetails
            .setPaymentDetails(paymentDetails)
            .setPaymentSuccessfulDate(time.now());

        caseData = caseData.copy()
            .generalAppPBADetails(updatedPbaDetails)
            .build();

        return caseData;
    }

    private GeneralApplicationCaseData updateCaseDataWithStateAndPaymentDetails(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                              GeneralApplicationCaseData caseData) {
        GeneralApplicationPbaDetails pbaDetails = caseData.getGeneralAppPBADetails();
        String customerReference = ofNullable(serviceRequestUpdateDto.getPayment())
            .map(PaymentDto::getCustomerReference)
            .orElse(pbaDetails.getAdditionalPaymentServiceRef());

        PaymentDetails paymentDetails = ofNullable(pbaDetails.getAdditionalPaymentDetails())
            .map(PaymentDetails::copy)
            .orElse(new PaymentDetails())
            .setStatus(SUCCESS)
            .setCustomerReference(customerReference)
            .setReference(serviceRequestUpdateDto.getPayment().getPaymentReference())
            .setErrorCode(null)
            .setErrorMessage(null)
            ;

        GeneralApplicationPbaDetails updatedPbaDetails = pbaDetails.copy();
        updatedPbaDetails
            .setAdditionalPaymentDetails(paymentDetails)
            .setPaymentSuccessfulDate(time.now());

        caseData = caseData.copy()
            .ccdState(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData))
            .generalAppPBADetails(updatedPbaDetails)
            .build();

        return caseData;
    }

    private void createEvent(GeneralApplicationCaseData caseData, CaseEvent eventName, String generalApplicationCaseId) {
        StartEventResponse startEventResponse = coreCaseDataService.startGaUpdate(
            generalApplicationCaseId,
            eventName
        );
        GeneralApplicationCaseData startEventData = caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails());

        BusinessProcess businessProcess = startEventData.getBusinessProcess();
        CaseDataContent caseDataContent = buildCaseDataContent(
            startEventResponse,
            caseData,
            businessProcess,
            generalApplicationCaseId,
            startEventData.getGeneralAppParentCaseLink()
        );
        coreCaseDataService.submitGaUpdate(generalApplicationCaseId, caseDataContent);
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
