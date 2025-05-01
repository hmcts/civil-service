package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_CLAIM_ISSUE_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_HEARING_FEE_PAYMENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdatePaymentStatusService {

    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final ObjectMapper objectMapper;

    @Retryable(value = CaseDataUpdateException.class, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public void updatePaymentStatus(FeeType feeType, String caseReference, CardPaymentStatusResponse cardPaymentStatusResponse) {

        try {
            log.info("Fetching case details for case reference [{}]", caseReference);
            CaseDetails caseDetails = coreCaseDataService.getCase(Long.valueOf(caseReference));

            log.info("Converting case details to CaseData for case reference [{}]", caseReference);
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

            log.info("Updating CaseData with state and payment details for case reference [{}], fee type [{}]", caseReference, feeType.name());
            caseData = updateCaseDataWithStateAndPaymentDetails(cardPaymentStatusResponse, caseData, feeType.name());

            log.info("Creating event for case reference [{}] with fee type [{}]", caseReference, feeType.name());
            createEvent(caseData, caseReference, feeType.name());

        } catch (Exception ex) {
            log.error(
                "Error occurred while processing case reference [{}] with fee type [{}]: {}",
                caseReference,
                feeType.name(),
                ex.getMessage(),
                ex
            );
            throw new CaseDataUpdateException();
        }

    }

    private void createEvent(CaseData caseData, String caseReference, String feeType) {

        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(
            caseReference,
            getEventNameFromFeeType(feeType)
        );

        CaseDataContent caseDataContent = buildCaseDataContent(
            startEventResponse,
            caseData
        );

        coreCaseDataService.submitUpdate(caseReference, caseDataContent);
    }

    private CaseEvent getEventNameFromFeeType(String feeType) {

        if (feeType.equals(FeeType.HEARING.name())) {
            return CITIZEN_HEARING_FEE_PAYMENT;
        }  else {
            return CITIZEN_CLAIM_ISSUE_PAYMENT;
        }
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

    private CaseData updateCaseDataWithStateAndPaymentDetails(CardPaymentStatusResponse cardPaymentStatusResponse,
                                                              CaseData caseData, String feeType) {

        PaymentDetails pbaDetails = getPBADetailsFromFeeType(feeType, caseData);

        PaymentDetails paymentDetails = ofNullable(pbaDetails)
            .map(PaymentDetails::toBuilder)
            .orElse(PaymentDetails.builder())
            .status(PaymentStatus.valueOf(cardPaymentStatusResponse.getStatus().toUpperCase()))
            .reference(cardPaymentStatusResponse.getPaymentReference())
            .errorCode(cardPaymentStatusResponse.getErrorCode())
            .errorMessage(cardPaymentStatusResponse.getErrorDescription())
            .build();

        caseData = getCaseDataFromFeeType(feeType, caseData, paymentDetails);
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
}
