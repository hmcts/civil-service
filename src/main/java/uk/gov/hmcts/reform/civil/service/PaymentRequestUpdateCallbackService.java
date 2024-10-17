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
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.util.Map;

import static java.util.Optional.ofNullable;
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
}
