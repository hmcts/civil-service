package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestUpdateCallbackService {

    private static final String PAID = "Paid";
    private final PaymentProcessingHelper paymentProcessingHelper;
    private final UpdatePaymentStatusService updatePaymentStatusService;

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto, String feeType) {
        String caseId = serviceRequestUpdateDto.getCcdCaseNumber();
        log.info(
            "Processing the callback for the caseId {} with status {}",
            caseId,
            serviceRequestUpdateDto.getServiceRequestStatus()
        );

        if (!isPaid(serviceRequestUpdateDto) || !isValidFeeType(feeType)) {
            return;
        }

        CaseData caseData = paymentProcessingHelper.getCaseData(caseId);

        if (caseData.isLipvLipOneVOne()) {
            processLiPCase(serviceRequestUpdateDto, feeType, caseId, caseData);
        } else {
            processNonLiPCase(serviceRequestUpdateDto, feeType, caseId, caseData);
        }
    }

    private boolean isPaid(ServiceRequestUpdateDto serviceRequestUpdateDto) {
        return PAID.equalsIgnoreCase(serviceRequestUpdateDto.getServiceRequestStatus());
    }

    private boolean isValidFeeType(String feeType) {
        return FeeType.HEARING.name().equals(feeType) || FeeType.CLAIMISSUED.name().equals(feeType);
    }

    private void processLiPCase(
        ServiceRequestUpdateDto serviceRequestUpdateDto,
        String feeType,
        String caseId,
        CaseData caseData
    ) {
        if (paymentProcessingHelper.isValidPaymentUpdateHearing(feeType, caseData)
            || paymentProcessingHelper.isValidUpdatePaymentClaimIssue(feeType, caseData)) {
            log.info("Updating payment details for LiP case {}", caseId);
            PaymentDetails existingPaymentDetails = paymentProcessingHelper.retrievePaymentDetails(feeType, caseData);

            PaymentDetails updatedDetails = buildPaymentDetails(serviceRequestUpdateDto, existingPaymentDetails);
            caseData = paymentProcessingHelper.updateCaseDataWithPaymentDetails(feeType, caseData, updatedDetails);

            paymentProcessingHelper.createAndSubmitEvent(
                caseData,
                caseId,
                feeType,
                "PaymentRequestUpdate"
            );

            CardPaymentStatusResponse cardPaymentStatusResponse = buildCardPaymentStatusResponse(serviceRequestUpdateDto);
            updatePaymentStatusService.updatePaymentStatus(
                FeeType.valueOf(feeType),
                caseId,
                cardPaymentStatusResponse
            );
        }
    }

    private void processNonLiPCase(
        ServiceRequestUpdateDto serviceRequestUpdateDto,
        String feeType,
        String caseId,
        CaseData caseData
    ) {
        PaymentDetails existingPaymentDetails = paymentProcessingHelper.retrievePaymentDetails(feeType, caseData);
        if (shouldUpdatePayment(existingPaymentDetails)) {
            log.info("Updating payment details for Non-LiP case {}", caseId);
            PaymentDetails updatedDetails = buildPaymentDetails(serviceRequestUpdateDto, existingPaymentDetails);
            caseData = paymentProcessingHelper.updateCaseDataWithPaymentDetails(feeType, caseData, updatedDetails);

            paymentProcessingHelper.createAndSubmitEvent(
                caseData,
                caseId,
                feeType,
                "PaymentRequestUpdate"
            );

            updatePaymentStatus(serviceRequestUpdateDto, feeType);
        }
    }

    private boolean shouldUpdatePayment(PaymentDetails paymentDetails) {
        return paymentDetails == null || FAILED.equals(paymentDetails.getStatus());
    }

    private void updatePaymentStatus(ServiceRequestUpdateDto serviceRequestUpdateDto, String feeType) {
        String caseId = serviceRequestUpdateDto.getCcdCaseNumber();
        CardPaymentStatusResponse cardPaymentStatusResponse = buildCardPaymentStatusResponse(serviceRequestUpdateDto);
        updatePaymentStatusService.updatePaymentStatus(
            FeeType.valueOf(feeType),
            caseId,
            cardPaymentStatusResponse
        );
    }

    private PaymentDetails buildPaymentDetails(
        ServiceRequestUpdateDto serviceRequestUpdateDto,
        PaymentDetails existingDetails
    ) {
        String customerReference = Optional.ofNullable(serviceRequestUpdateDto.getPayment())
            .map(PaymentDto::getCustomerReference)
            .orElse(Optional.ofNullable(existingDetails)
                        .map(PaymentDetails::getCustomerReference)
                        .orElse(null));

        return PaymentDetails.builder()
            .status(SUCCESS)
            .customerReference(customerReference)
            .reference(serviceRequestUpdateDto.getPayment().getPaymentReference())
            .errorCode(null)
            .errorMessage(null)
            .build();
    }

    private CardPaymentStatusResponse buildCardPaymentStatusResponse(
        ServiceRequestUpdateDto serviceRequestUpdateDto
    ) {
        return CardPaymentStatusResponse.builder()
            .paymentReference(serviceRequestUpdateDto.getPayment().getPaymentReference())
            .status(SUCCESS.toString())
            .build();
    }
}
