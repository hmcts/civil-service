package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

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

            PaymentDetails paymentDetails = paymentServiceHelper.buildPaymentDetails(cardPaymentStatusResponse);
            caseData = paymentServiceHelper.updateCaseDataByFeeType(caseData, feeType.name(), paymentDetails);

            boolean isFailedPayment = PaymentStatus.FAILED.name().equalsIgnoreCase(cardPaymentStatusResponse.getStatus());
            paymentServiceHelper.createEvent(caseData, caseReference, feeType.name(), isFailedPayment);
        } catch (Exception ex) {
            throw new CaseDataUpdateException();
        }
    }
}
