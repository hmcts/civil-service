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

import static java.util.Objects.requireNonNull;

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

        SRPbaDetails feePaymentDetails = feeType.equals(FeeType.HEARING)
            ? caseData.getHearingFeePBADetails()
            : caseData.getClaimIssuedPBADetails();

        requireNonNull(feePaymentDetails, "Fee Payment details cannot be null");
        requireNonNull(feePaymentDetails.getServiceReqReference(), "Fee Payment service request cannot be null");

        String returnUrlSubPath = feeType.equals(FeeType.HEARING)
            ? "/hearing-payment-confirmation/" : "/claim-issued-payment-confirmation/";

        CardPaymentServiceRequestDTO requestDto = CardPaymentServiceRequestDTO.builder()
            .amount(feePaymentDetails.getFee().getCalculatedAmountInPence()
                        .divide(BigDecimal.valueOf(100), RoundingMode.CEILING)
                        .setScale(2, RoundingMode.CEILING))
            .currency("GBP")
            .language("En")
            .returnUrl(pinInPostConfiguration.getCuiFrontEndUrl() + returnUrlSubPath + caseReference)
            .build();

        CardPaymentServiceRequestResponse govPayCardPaymentRequest = paymentStatusService
            .createGovPayCardPaymentRequest(
                feePaymentDetails.getServiceReqReference(),
                authorization,
                requestDto
            );
        return CardPaymentStatusResponse.from(govPayCardPaymentRequest);
    }

    public CardPaymentStatusResponse getGovPaymentRequestStatus(
        FeeType feeType, String paymentReference, String authorization) {
        log.info("Checking payment status for {} of fee type {}", paymentReference, feeType);
        PaymentDto cardPaymentDetails = paymentStatusService.getCardPaymentDetails(paymentReference, authorization);
        String paymentStatus = cardPaymentDetails.getStatus();
        CardPaymentStatusResponse.CardPaymentStatusResponseBuilder response = CardPaymentStatusResponse.builder()
            .status(paymentStatus)
            .paymentReference(cardPaymentDetails.getReference())
            .externalReference(cardPaymentDetails.getPaymentGroupReference())
            .paymentFor(feeType.name().toLowerCase())
            .paymentAmount(cardPaymentDetails.getAmount());

        if (paymentStatus.equals("Failed")) {
            Arrays.asList(cardPaymentDetails.getStatusHistories()).stream()
                .filter(h -> h.getStatus().equals(paymentStatus))
                .findFirst()
                .ifPresent(h -> response.errorCode(h.getErrorCode()).errorDescription(h.getErrorMessage()));
        }

        return response.build();
    }
}
