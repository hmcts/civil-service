package uk.gov.hmcts.reform.civil.service.robotics.utils;

import org.jetbrains.annotations.Nullable;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;

public class EventHistoryUtil {

    public static final String BS_REF = "Breathing space reference";
    public static final String BS_START_DT = "actual start date";
    public static final String BS_END_DATE = "actual end date";
    public static final String RPA_REASON_MANUAL_DETERMINATION = "RPA Reason: Manual Determination Required.";
    public static final String RPA_REASON_JUDGMENT_BY_ADMISSION = "RPA Reason: Judgment by Admission requested and claim moved offline.";
    public static final String RPA_IN_MEDIATION = "IN MEDIATION";
    public static final String ENTER = "Enter";
    public static final String LIFTED = "Lifted";

    private EventHistoryUtil() {
    }

    public static int prepareEventSequence(EventHistory history) {
        int currentSequence = 0;
        currentSequence = getCurrentSequence(history.getMiscellaneous(), currentSequence);
        currentSequence = getCurrentSequence(history.getAcknowledgementOfServiceReceived(), currentSequence);
        currentSequence = getCurrentSequence(history.getConsentExtensionFilingDefence(), currentSequence);
        currentSequence = getCurrentSequence(history.getDefenceFiled(), currentSequence);
        currentSequence = getCurrentSequence(history.getDefenceAndCounterClaim(), currentSequence);
        currentSequence = getCurrentSequence(history.getReceiptOfPartAdmission(), currentSequence);
        currentSequence = getCurrentSequence(history.getReceiptOfAdmission(), currentSequence);
        currentSequence = getCurrentSequence(history.getReplyToDefence(), currentSequence);
        currentSequence = getCurrentSequence(history.getBreathingSpaceEntered(), currentSequence);
        currentSequence = getCurrentSequence(history.getBreathingSpaceLifted(), currentSequence);
        currentSequence = getCurrentSequence(history.getBreathingSpaceMentalHealthEntered(), currentSequence);
        currentSequence = getCurrentSequence(history.getBreathingSpaceMentalHealthLifted(), currentSequence);
        currentSequence = getCurrentSequence(history.getStatesPaid(), currentSequence);
        currentSequence = getCurrentSequence(history.getDirectionsQuestionnaireFiled(), currentSequence);
        currentSequence = getCurrentSequence(history.getJudgmentByAdmission(), currentSequence);
        currentSequence = getCurrentSequence(history.getGeneralFormOfApplication(), currentSequence);
        currentSequence = getCurrentSequence(history.getDefenceStruckOut(), currentSequence);
        return currentSequence + 1;
    }

    public static int getCurrentSequence(List<Event> events, int currentSequence) {
        for (Event event : events) {
            if (event.getEventSequence() != null && event.getEventSequence() > currentSequence) {
                currentSequence = event.getEventSequence();
            }
        }
        return currentSequence;
    }

    public static String getInstallmentPeriod(CaseData data) {
        if (data.getPaymentTypeSelection().equals(DJPaymentTypeSelection.REPAYMENT_PLAN)) {
            if (data.getRepaymentFrequency().equals(RepaymentFrequencyDJ.ONCE_ONE_WEEK)) {

                return "WK";
            } else if (data.getRepaymentFrequency().equals(RepaymentFrequencyDJ.ONCE_TWO_WEEKS)) {
                return "FOR";
            } else if (data.getRepaymentFrequency().equals(RepaymentFrequencyDJ.ONCE_ONE_MONTH)) {
                return "MTH";
            }

        } else if (data.getPaymentTypeSelection().equals(DJPaymentTypeSelection.IMMEDIATELY)) {
            return "FW";
        }

        return "FUL";
    }

    public static String getInstallmentPeriodForRequestJudgmentByAdmission(Optional<RepaymentPlanLRspec> repaymentPlanLRspec) {
        return repaymentPlanLRspec.map(RepaymentPlanLRspec::getRepaymentFrequency).map(repaymentFrequency -> {
            switch (repaymentFrequency) {
                case ONCE_ONE_WEEK:
                    return "WK";
                case ONCE_TWO_WEEKS:
                    return "FOR";
                case ONCE_ONE_MONTH:
                    return "MTH";
                default:
                    return null;
            }
        }).orElse(null);
    }

    public static BigDecimal getInstallmentAmount(String amount) {
        var regularRepaymentAmountPennies = new BigDecimal(amount);
        return MonetaryConversions.penniesToPounds(regularRepaymentAmountPennies);
    }

    @Nullable
    public static BigDecimal getInstallmentAmount(boolean isResponsePayByInstallment, Optional<RepaymentPlanLRspec> repaymentPlan) {
        return isResponsePayByInstallment
            ? MonetaryConversions.penniesToPounds(
            repaymentPlan.map(RepaymentPlanLRspec::getPaymentAmount).map(amount -> amount.setScale(2)).orElse(ZERO))
            : null;
    }

    @Nullable
    public static LocalDate getFirstInstallmentDate(boolean isResponsePayByInstallment, Optional<RepaymentPlanLRspec> repaymentPlan) {
        return isResponsePayByInstallment
            ? repaymentPlan.map(RepaymentPlanLRspec::getFirstRepaymentDate)
            .orElse(null)
            : null;
    }
}
