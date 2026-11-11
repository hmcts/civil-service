package uk.gov.hmcts.reform.civil.ga.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.service.PaymentStatusService;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.request.CardPaymentServiceRequestDTO;
import uk.gov.hmcts.reform.payments.response.CardPaymentServiceRequestResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaFeesPaymentService {

    private static final String PAYMENT_NOT_READY_MESSAGE = "Payment is not ready yet, please retry in a moment";

    private final CaseDetailsConverter caseDetailsConverter;
    private final GaCoreCaseDataService gaCoreCaseDataService;
    private final PaymentStatusService paymentStatusService;
    private final UpdatePaymentStatusService updatePaymentStatusService;
    @Value("${cui-front-end.url}") String cuiFrontEndUrl;

    public CardPaymentStatusResponse createGovPaymentRequest(String caseReference, String authorization) {

        log.info("Creating general application Gov Payment request url for caseId {}", caseReference);
        CaseDetails caseDetails = gaCoreCaseDataService.getCase(Long.valueOf(caseReference));
        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);
        String parentCaseRef = caseData.getParentCaseReference();
        GeneralApplicationPbaDetails generalAppPbaDetails = caseData.getGeneralAppPBADetails();

        String serviceRequestReference = null;
        if (generalAppPbaDetails != null) {
            serviceRequestReference = caseData.isAdditionalFeeRequested()
                ? generalAppPbaDetails.getAdditionalPaymentServiceRef() : generalAppPbaDetails.getServiceReqReference();
        }
        if (!StringUtils.hasText(serviceRequestReference)) {
            log.warn("General application payment is not ready for case {}", caseReference);
            throw new ResponseStatusException(HttpStatus.CONFLICT, PAYMENT_NOT_READY_MESSAGE);
        }

        String returnUrlSubPath = "/general-application/payment-confirmation/" + parentCaseRef + "/gaid/";

        CardPaymentServiceRequestDTO requestDto = CardPaymentServiceRequestDTO.builder()
            .amount(generalAppPbaDetails.getFee().getCalculatedAmountInPence()
                        .divide(BigDecimal.valueOf(100), RoundingMode.CEILING)
                        .setScale(2, RoundingMode.CEILING))
            .currency("GBP")
            .language(caseData.isApplicantBilingual() ? "cy" : "en")
            .returnUrl(cuiFrontEndUrl + returnUrlSubPath + caseReference)
            .build();
        CardPaymentServiceRequestResponse govPayCardPaymentRequest = paymentStatusService
            .createGovPayCardPaymentRequest(
                serviceRequestReference,
                authorization,
                requestDto
            );
        return CardPaymentStatusResponse.from(govPayCardPaymentRequest);
    }

    public CardPaymentStatusResponse getGovPaymentRequestStatus(String caseReference, String paymentReference, String authorization) {
        log.info("Checking general application payment status for {}", paymentReference);
        PaymentDto cardPaymentDetails = paymentStatusService.getCardPaymentDetails(paymentReference, authorization);
        String paymentStatus = cardPaymentDetails.getStatus();
        CardPaymentStatusResponse response = new CardPaymentStatusResponse()
            .setStatus(paymentStatus)
            .setPaymentReference(cardPaymentDetails.getReference())
            .setExternalReference(cardPaymentDetails.getPaymentGroupReference())
            .setPaymentAmount(cardPaymentDetails.getAmount());

        if (paymentStatus.equals("Failed")) {
            Arrays.stream(cardPaymentDetails.getStatusHistories())
                .filter(h -> h.getStatus().equals(paymentStatus))
                .findFirst()
                .ifPresent(h -> response.setErrorCode(h.getErrorCode()).setErrorDescription(h.getErrorMessage()));
        }

        log.info("Payment status is {} for GA case {}", paymentStatus, caseReference);

        if (PaymentStatus.isValid(paymentStatus)) {
            try {
                updatePaymentStatusService.updatePaymentStatus(caseReference, response);
            } catch (Exception e) {
                log.error(
                    "Update general application payment status failed for claim [{}]. Error: {}",
                    caseReference,
                    e.getMessage(),
                    e
                );
            }
        }

        return response;
    }
}
