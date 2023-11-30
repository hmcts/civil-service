package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.request.CardPaymentServiceRequestDTO;
import uk.gov.hmcts.reform.payments.response.CardPaymentServiceRequestResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeesPaymentService {

    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final PaymentsClient paymentsClient;
    private final PinInPostConfiguration pinInPostConfiguration;
    private final PaymentStatusService paymentStatusService;

    public CardPaymentStatusResponse createGovPaymentRequest(
        FeeType feeType, String caseReference, String authorization) {

        log.info("Creating gov Payment request url for caseId {} for fee type {}", caseReference, feeType);
        CaseDetails caseDetails = coreCaseDataService.getCase(Long.valueOf(caseReference));
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

        SRPbaDetails hearingFeePaymentDetails = extractHearingFeePaymentDetails(feeType, caseData);

        String returnUrlSubPath = feeType.equals(FeeType.HEARING)
            ? "/hearing-payment-confirmation/" : "/claim-issued-payment-confirmation/";

        CardPaymentServiceRequestDTO requestDto = CardPaymentServiceRequestDTO.builder()
            .amount(hearingFeePaymentDetails.getFee().getCalculatedAmountInPence().divide(
                BigDecimal.valueOf(100),
                RoundingMode.CEILING
            ).setScale(2, RoundingMode.CEILING))
            .currency("GBP")
            .language("English")
            .returnUrl(pinInPostConfiguration.getCuiFrontEndUrl() + returnUrlSubPath + caseReference)
            .build();

        CardPaymentServiceRequestResponse govPayCardPaymentRequest = paymentStatusService
            .createGovPayCardPaymentRequest(
                hearingFeePaymentDetails.getServiceReqReference(),
                authorization,
                requestDto
            );
        return CardPaymentStatusResponse.from(govPayCardPaymentRequest);
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
        PaymentDto cardPaymentDetails = paymentStatusService.getCardPaymentDetails(paymentReference, authorization);
        String paymentStatus = cardPaymentDetails.getStatus();
        CardPaymentStatusResponse.CardPaymentStatusResponseBuilder response = CardPaymentStatusResponse.builder()
            .status(paymentStatus)
            .paymentReference(cardPaymentDetails.getPaymentReference())
            .externalReference(cardPaymentDetails.getExternalReference())
            .dateCreated(cardPaymentDetails.getDateCreated());

        if (!paymentStatus.equals("Success")) {
            Arrays.asList(cardPaymentDetails.getStatusHistories()).stream()
                .filter(h -> h.getStatus().equals(paymentStatus))
                .findFirst()
                .ifPresent(h -> response.errorCode(h.getErrorCode()).errorDescription(h.getErrorMessage()));
        }

        return response.build();
    }
}
