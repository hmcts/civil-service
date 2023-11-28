package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.exceptions.CompleteTaskException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.request.CardPaymentServiceRequestDTO;
import uk.gov.hmcts.reform.payments.response.CardPaymentServiceRequestResponse;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeesPaymentService {

    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final PaymentsClient paymentsClient;
    private final PinInPostConfiguration pinInPostConfiguration;

    public CardPaymentServiceRequestResponse createGovPaymentRequest(
        FeeType feeType, String caseReference, String authorization) {

        log.info("Creating gov Payment request url for caseId {} for fee type {}", caseReference, feeType);
        CaseDetails caseDetails = coreCaseDataService.getCase(Long.valueOf(caseReference));
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

        SRPbaDetails hearingFeePaymentDetails = extractHearingFeePaymentDetails(feeType, caseData);

        String returnUrlSubPath = feeType.equals(FeeType.HEARING)
            ? "/hearing-payment-confirmation/" : "/claim-issued-payment-confirmation/";

        CardPaymentServiceRequestDTO requestDto = CardPaymentServiceRequestDTO.builder()
            .amount(hearingFeePaymentDetails.getFee().getCalculatedAmountInPence().multiply(BigDecimal.valueOf(100)))
            .currency("GBP")
            .language("English")
            .returnUrl(pinInPostConfiguration.getCuiFrontEndUrl() + returnUrlSubPath + caseReference)
            .build();
        return paymentsClient.createGovPayCardPaymentRequest(
            hearingFeePaymentDetails.getServiceReqReference(),
            authorization,
            requestDto
        );
    }

    private SRPbaDetails extractHearingFeePaymentDetails(FeeType feeType, CaseData caseData) {
        if (feeType.equals(FeeType.HEARING)) {
            return caseData.getHearingFeePBADetails().toBuilder().fee(caseData.getHearingFee()).build();
        } else {
            return caseData.getClaimIssuedPBADetails().toBuilder().fee(caseData.getClaimFee()).build();
        }
    }

    public CardPaymentStatusResponse getGovPaymentRequestStatus(
        FeeType feeType, String paymentReference, String authorization) {
        PaymentDto cardPaymentStatus = getCardPaymentStatus(paymentReference, authorization);
        return CardPaymentStatusResponse.builder()
            .status(cardPaymentStatus.getStatus())
            .paymentReference(cardPaymentStatus.getPaymentReference())
            .externalReference(cardPaymentStatus.getExternalReference())
            .dateCreated(cardPaymentStatus.getDateCreated())
            .build();
    }

    @Retryable(value = RuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 500))
    private PaymentDto getCardPaymentStatus(String paymentReference, String authorization) {
        try {
            PaymentDto cardPaymentStatus = paymentsClient.getGovPayCardPaymentStatus(paymentReference, authorization);
            String status = cardPaymentStatus.getStatus();
            if (!status.equals("Success")) {
                throw new Exception("Need to check payment status again as current payment status is " + status);
            }
            return cardPaymentStatus;
        } catch (Exception e) {
            log.error("Payment status check failed due to {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
