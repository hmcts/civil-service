package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdatePaymentStatusService {

    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final PaymentServiceHelper paymentServiceHelper;

    @Retryable(value = CaseDataUpdateException.class, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public void updatePaymentStatus(FeeType feeType, String caseReference, CardPaymentStatusResponse cardPaymentStatusResponse) {
        try {
            CaseDetails caseDetails = coreCaseDataService.getCase(Long.valueOf(caseReference));
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

            caseData = paymentServiceHelper.updateCaseDataByFeeType(caseData, feeType.name(),
                                                                    paymentServiceHelper.buildPaymentDetails(cardPaymentStatusResponse));

            paymentServiceHelper.createEvent(caseData, caseReference, feeType.name());
        } catch (Exception ex) {
            throw new CaseDataUpdateException();
        }
    }
}
