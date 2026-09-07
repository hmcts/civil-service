package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.service.Time;

@Component
public class HearingFeeHelper {

    private final Time time;

    public HearingFeeHelper(Time time) {
        this.time = time;
    }

    public boolean isHearingFeePaid(PaymentDetails hearingFeePaymentDetails, CaseData caseData) {
        return isSuccessfulPaymentBeforeDueDate(hearingFeePaymentDetails, caseData)
            || caseData.hearingFeePaymentDoneWithHWF();
    }

    public boolean isHearingFeeUnpaid(PaymentDetails hearingFeePaymentDetails, CaseData caseData) {
        return (isFailedPayment(hearingFeePaymentDetails) || hearingFeePaymentDetails == null)
            && caseData.getHearingDueDate().isBefore(time.now().toLocalDate());
    }

    private boolean isSuccessfulPaymentBeforeDueDate(PaymentDetails hearingFeePaymentDetails, CaseData caseData) {
        return isSuccessfulPayment(hearingFeePaymentDetails)
            && caseData.getHearingDueDate().isBefore(time.now().toLocalDate());
    }

    private boolean isSuccessfulPayment(PaymentDetails paymentDetails) {
        return paymentDetails != null && paymentDetails.getStatus() == PaymentStatus.SUCCESS;
    }

    private boolean isFailedPayment(PaymentDetails paymentDetails) {
        return paymentDetails != null && paymentDetails.getStatus() == PaymentStatus.FAILED;
    }
}
