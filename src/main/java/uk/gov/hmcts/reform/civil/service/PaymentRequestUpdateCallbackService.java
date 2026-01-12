package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestUpdateCallbackService {

    private static final String PAID = "Paid";
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final ObjectMapper objectMapper;
    private final PaymentStatusRetryService retryService;

    public void processCallback(ServiceRequestUpdateDto dto, String feeTypeStr) {
        log.info("Processing callback for caseId {} with status {}",
                dto.getCcdCaseNumber(),
                dto.getServiceRequestStatus());

        if (!isPaid(dto)) {
            log.info("Service request status is not 'Paid'. No action required for case {}", dto.getCcdCaseNumber());
            return;
        }

        FeeType feeType = getFeeType(feeTypeStr);
        if (!isHandledFeeType(feeType) || feeType == null) {
            log.info("FeeType {} is not handled or invalid. No action required for case {}", feeType, dto.getCcdCaseNumber());
            return;
        }

        Long caseId = Long.valueOf(dto.getCcdCaseNumber());
        log.info("Fetching case details for caseId {}", caseId);
        log.debug("ServiceRequestUpdateDto: {}", dto);

        CaseDetails caseDetails = coreCaseDataService.getCase(caseId);
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

        if ((caseData.isLipvLipOneVOne() && isPaymentUpdateValid(feeType, caseData)) || !caseData.isLipvLipOneVOne()) {
            handlePaymentUpdate(dto, caseData, feeType);
        }
    }

    private void handlePaymentUpdate(ServiceRequestUpdateDto dto, CaseData caseData, FeeType feeType) {
        CardPaymentStatusResponse paymentStatusResponse = buildPaymentStatusResponse(dto);
        String customerReference = getCustomerReference(dto, caseData, feeType);
        retryService.updatePaymentStatus(feeType, dto.getCcdCaseNumber(),
                                         retryService.updateCaseDataWithPaymentDetails(paymentStatusResponse, caseData, feeType, customerReference));
    }

    private boolean isPaid(ServiceRequestUpdateDto dto) {
        return PAID.equalsIgnoreCase(dto.getServiceRequestStatus());
    }

    private FeeType getFeeType(String feeTypeStr) {
        try {
            return FeeType.valueOf(feeTypeStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isHandledFeeType(FeeType feeType) {
        return feeType == FeeType.HEARING || feeType == FeeType.CLAIMISSUED;
    }

    private boolean isPaymentUpdateValid(FeeType feeType, CaseData caseData) {
        return (feeType == FeeType.HEARING && isHearingPaymentUnpaidOrFailed(caseData))
                || (feeType == FeeType.CLAIMISSUED && isClaimIssuePaymentUnpaidOrFailed(caseData));
    }

    private boolean isHearingPaymentUnpaidOrFailed(CaseData caseData) {
        PaymentDetails hearingPayment = caseData.getHearingFeePaymentDetails();
        return hearingPayment == null || hearingPayment.getStatus() == PaymentStatus.FAILED;
    }

    private boolean isClaimIssuePaymentUnpaidOrFailed(CaseData caseData) {
        PaymentDetails claimIssuePayment = caseData.getClaimIssuedPaymentDetails();
        return claimIssuePayment == null || claimIssuePayment.getStatus() == PaymentStatus.FAILED;
    }

    private CardPaymentStatusResponse buildPaymentStatusResponse(ServiceRequestUpdateDto dto) {
        return new CardPaymentStatusResponse()
                .setPaymentReference(dto.getPayment().getPaymentReference())
                .setStatus(PaymentStatus.SUCCESS.name());
    }

    private String getCustomerReference(ServiceRequestUpdateDto dto, CaseData caseData, FeeType feeType) {
        return Optional.ofNullable(dto.getPayment())
                .map(PaymentDto::getCustomerReference)
                .orElse(Optional.ofNullable(retryService.getPaymentDetails(feeType, caseData))
                        .map(PaymentDetails::getCustomerReference)
                        .orElse(null));
    }
}
