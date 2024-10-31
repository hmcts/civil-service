package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_CLAIM_ISSUE_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_HEARING_FEE_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESUBMIT_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdatePaymentStatusService {

    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final PaymentServiceHelper paymentServiceHelper;

    @Retryable(value = CaseDataUpdateException.class, backoff = @Backoff(delay = 500))
    public void updatePaymentStatus(FeeType feeType, String caseReference, CardPaymentStatusResponse cardPaymentStatusResponse) {
        try {
            CaseDetails caseDetails = coreCaseDataService.getCase(Long.valueOf(caseReference));
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

            paymentServiceHelper.updateCaseDataByFeeType(caseData, feeType.name(),
                                                         paymentServiceHelper.buildPaymentDetails(cardPaymentStatusResponse));

            createEvent(caseData, caseReference, feeType);
        } catch (Exception ex) {
            throw new CaseDataUpdateException();
        }
    }

    private void createEvent(CaseData caseData, String caseReference, FeeType feeType) {
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseReference, getEventNameFromFeeType(feeType, caseData));
        CaseDataContent caseDataContent = paymentServiceHelper.buildCaseDataContent(startEventResponse, caseData);
        coreCaseDataService.submitUpdate(caseReference, caseDataContent);
    }

    private CaseEvent getEventNameFromFeeType(FeeType feeType, CaseData caseData) {
        if (feeType == FeeType.HEARING) {
            return CITIZEN_HEARING_FEE_PAYMENT;
        } else if (feeType == FeeType.CLAIMISSUED && isPaymentFailed(caseData.getClaimIssuedPaymentDetails())) {
            return RESUBMIT_CLAIM;
        } else {
            return CITIZEN_CLAIM_ISSUE_PAYMENT;
        }
    }

    private boolean isPaymentFailed(PaymentDetails paymentDetails) {
        return paymentDetails == null || paymentDetails.getStatus() == FAILED;
    }
}
