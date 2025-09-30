package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.util.Map;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SERVICE_REQUEST_RECEIVED;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.utils.JudicialDecisionNotificationUtil.isGeneralAppConsentOrder;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestUpdateCallbackService {

    public static final String PAID = "Paid";
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final ObjectMapper objectMapper;
    private final UpdatePaymentStatusService updatePaymentStatusService;
    private final JudicialNotificationService judicialNotificationService;
    private final StateGeneratorService stateGeneratorService;
    private final Time time;

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto, String feeType) {
        log.info("Processing the callback for the caseId {} with status {}", serviceRequestUpdateDto.getCcdCaseNumber(),
                 serviceRequestUpdateDto.getServiceRequestStatus()
        );

        if (serviceRequestUpdateDto.getServiceRequestStatus().equalsIgnoreCase(PAID)) {

            log.info("Fetching the Case details based on caseId {}", serviceRequestUpdateDto.getCcdCaseNumber());
            log.info("Service Req Update Dto: {}", serviceRequestUpdateDto);
            CaseDetails caseDetails = coreCaseDataService.getCase(Long.valueOf(serviceRequestUpdateDto
                                                                                   .getCcdCaseNumber()));
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
            if (feeType.equals(FeeType.HEARING.name()) || feeType.equals(FeeType.CLAIMISSUED.name())) {
                if (caseData.isLipvLipOneVOne()) {
                    log.info("caseIssuePaymentDetails = {} for case {}", caseData.getClaimIssuedPaymentDetails(), serviceRequestUpdateDto.getCcdCaseNumber());
                    if (isValidPaymentUpdateHearing(feeType, caseData) || isValidUpdatePaymentClaimIssue(feeType, caseData)) {
                        log.info("inside payment validation for case {}", serviceRequestUpdateDto.getCcdCaseNumber());
                        updateCaseDataWithStateAndPaymentDetails(serviceRequestUpdateDto, caseData, feeType);
                        CardPaymentStatusResponse cardPaymentStatusResponse = getCardPaymentStatusResponse(serviceRequestUpdateDto);
                        updatePaymentStatusService.updatePaymentStatus(FeeType.valueOf(feeType), serviceRequestUpdateDto.getCcdCaseNumber(), cardPaymentStatusResponse);
                    }
                } else {
                    caseData = updateCaseDataWithStateAndPaymentDetails(serviceRequestUpdateDto, caseData, feeType);
                    createEvent(caseData, serviceRequestUpdateDto.getCcdCaseNumber(), feeType);
                }
            }
        }
    }

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto) {
        log.info("Processing the callback for the caseId {} with status {}", serviceRequestUpdateDto.getCcdCaseNumber(),
                serviceRequestUpdateDto.getServiceRequestStatus());

        if (serviceRequestUpdateDto.getServiceRequestStatus().equalsIgnoreCase(PAID)) {

            log.info("Fetching the Case details based on caseId {}", serviceRequestUpdateDto.getCcdCaseNumber());
            CaseDetails caseDetails = coreCaseDataService.getCase(Long.valueOf(serviceRequestUpdateDto
                    .getCcdCaseNumber()));
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

            processServiceRequest(serviceRequestUpdateDto, caseData, false);
        }
    }

    private CaseData processServiceRequest(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                           CaseData caseData,
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

    private CaseData processAndTriggerAwaitingPayment(CaseData caseData,
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

    private CaseData processAndTriggerAdditionalPayment(CaseData caseData,
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

    private CaseData updateCaseDataWithPaymentDetails(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                      CaseData caseData) {
        GAPbaDetails pbaDetails = caseData.getGeneralAppPBADetails();
        String paymentReference = ofNullable(serviceRequestUpdateDto.getPayment())
                .map(PaymentDto::getCustomerReference)
                .orElse(pbaDetails.getServiceReqReference());

        PaymentDetails paymentDetails = ofNullable(pbaDetails.getPaymentDetails())
                .map(PaymentDetails::toBuilder)
                .orElse(PaymentDetails.builder())
                .status(SUCCESS)
                .customerReference(paymentReference)
                .reference(serviceRequestUpdateDto.getPayment().getPaymentReference())
                .errorCode(null)
                .errorMessage(null)
                .build();

        caseData = caseData.toBuilder()
                .generalAppPBADetails(pbaDetails.toBuilder()
                        .paymentDetails(paymentDetails)
                        .paymentSuccessfulDate(time.now()).build())
                .build();

        return caseData;
    }

    private static boolean isValidPaymentUpdateHearing(String feeType, CaseData caseData) {
        return feeType.equals(FeeType.HEARING.name())
            && (caseData.getHearingFeePaymentDetails() == null || caseData.getHearingFeePaymentDetails().getStatus() == FAILED);
    }

    private static boolean isValidUpdatePaymentClaimIssue(String feeType, CaseData caseData) {
        return feeType.equals(FeeType.CLAIMISSUED.name())
            && (caseData.getClaimIssuedPaymentDetails() == null || caseData.getClaimIssuedPaymentDetails().getStatus() == FAILED);
    }

    private CardPaymentStatusResponse getCardPaymentStatusResponse(ServiceRequestUpdateDto serviceRequestUpdateDto) {

        return CardPaymentStatusResponse.builder().paymentReference(serviceRequestUpdateDto.getPayment().getPaymentReference())
            .status(String.valueOf(SUCCESS)).build();
    }

    private void createEvent(CaseData caseData, String caseId, String feeType) {

        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
            caseId,
            getEventNameFromFeeType(caseData, feeType)
        );

        CaseDataContent caseDataContent = buildCaseDataContent(
            startEventResponse,
            caseData
        );

        coreCaseDataService.submitUpdate(caseId, caseDataContent);
    }

    private void createEvent(CaseData caseData, CaseEvent eventName, String generalApplicationCaseId) {
        StartEventResponse startEventResponse = coreCaseDataService.startGaUpdate(
                generalApplicationCaseId,
                eventName
        );
        CaseData startEventData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());

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

    private CaseData updateCaseDataWithStateAndPaymentDetails(ServiceRequestUpdateDto serviceRequestUpdateDto,
                                                              CaseData caseData) {
        GAPbaDetails pbaDetails = caseData.getGeneralAppPBADetails();
        String customerReference = ofNullable(serviceRequestUpdateDto.getPayment())
                .map(PaymentDto::getCustomerReference)
                .orElse(pbaDetails.getAdditionalPaymentServiceRef());

        PaymentDetails paymentDetails = ofNullable(pbaDetails.getAdditionalPaymentDetails())
                .map(PaymentDetails::toBuilder)
                .orElse(PaymentDetails.builder())
                .status(SUCCESS)
                .customerReference(customerReference)
                .reference(serviceRequestUpdateDto.getPayment().getPaymentReference())
                .errorCode(null)
                .errorMessage(null)
                .build();

        caseData = caseData.toBuilder()
                .ccdState(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData))
                .generalAppPBADetails(pbaDetails.toBuilder()
                        .additionalPaymentDetails(paymentDetails)
                        .paymentSuccessfulDate(time.now()).build()
                ).build();

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

    private CaseDataContent buildCaseDataContent(StartEventResponse startEventResponse, CaseData caseData) {

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

    private CaseDataContent buildCaseDataContent(StartEventResponse startEventResponse, CaseData caseData,
                                                 BusinessProcess businessProcess, String caseId,
                                                 GeneralAppParentCaseLink generalAppParentCaseLink) {
        Map<String, Object> updatedData = caseData.toMap(objectMapper);
        updatedData.put("businessProcess", businessProcess);

        if (generalAppParentCaseLink == null
                || StringUtils.isBlank(generalAppParentCaseLink.getCaseReference())) {
            updatedData.put("generalAppParentCaseLink", GeneralAppParentCaseLink.builder()
                    .caseReference(caseId).build());
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

    public CaseData processHwf(CaseData caseData) {
        ServiceRequestUpdateDto serviceRequestUpdateDto = ServiceRequestUpdateDto
                .builder()
                .ccdCaseNumber(caseData.getCcdCaseReference().toString())
                .payment(PaymentDto.builder()
                        .customerReference(caseData.getGeneralAppHelpWithFees()
                                .getHelpWithFeesReferenceNumber())
                        .build())
                .build();
        return processServiceRequest(serviceRequestUpdateDto, caseData, true);
    }
}
