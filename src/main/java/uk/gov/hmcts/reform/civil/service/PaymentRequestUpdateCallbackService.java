package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;

import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESUBMIT_CLAIM;
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
    private final UpdatePaymentStatusService updatePaymentStatusService;
    private final PaymentServiceHelper paymentServiceHelper;

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto, String feeType) {
        log.info("Processing callback for caseId {} with status {}",
                 serviceRequestUpdateDto.getCcdCaseNumber(), serviceRequestUpdateDto.getServiceRequestStatus());

        if (!PAID.equalsIgnoreCase(serviceRequestUpdateDto.getServiceRequestStatus())) {
            return;
        }
        CaseDetails caseDetails = coreCaseDataService.getCase(Long.parseLong(serviceRequestUpdateDto.getCcdCaseNumber()));
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

        if (isValidFeeType(feeType) && isEligibleForUpdate(feeType, caseData)) {
            PaymentDetails updatedPaymentDetails = paymentServiceHelper.buildPaymentDetails(
                buildCardPaymentStatusResponse(serviceRequestUpdateDto)
            );
            caseData = paymentServiceHelper.updateCaseDataByFeeType(caseData, feeType, updatedPaymentDetails);

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
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId,
                                                                                Objects.requireNonNull(
                                                                                    getEventNameFromFeeType(
                                                                                        caseData,
                                                                                        feeType
                                                                                    ))
        );
        CaseDataContent caseDataContent = paymentServiceHelper.buildCaseDataContent(startEventResponse, caseData);
        coreCaseDataService.submitUpdate(caseId, caseDataContent);
    }

    private CaseEvent getEventNameFromFeeType(CaseData caseData, String feeType) {
        if (feeType.equals(FeeType.HEARING.name())) {
            return SERVICE_REQUEST_RECEIVED;
        } else if (feeType.equals(FeeType.CLAIMISSUED.name())) {
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                return CREATE_CLAIM_SPEC_AFTER_PAYMENT;
            } else if (isPaymentFailed(caseData.getClaimIssuedPaymentDetails())) {
                return RESUBMIT_CLAIM;
            } else {
                return CREATE_CLAIM_AFTER_PAYMENT;
            }
        }
        return null;
    }
}
