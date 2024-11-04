package uk.gov.hmcts.reform.civil.service.robotics.utils;

import org.jetbrains.annotations.Nullable;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;

public class EventHistoryUtil {

    public static final String BS_REF = "Breathing space reference";
    public static final String BS_START_DT = "actual start date";
    public static final String BS_END_DATE = "actual end date";
    public static final String RPA_REASON_MANUAL_DETERMINATION = "RPA Reason: Manual Determination Required.";
    public static final String RPA_REASON_JUDGMENT_BY_ADMISSION = "RPA Reason: Judgment by Admission requested and claim moved offline.";
    public static final String RPA_IN_MEDIATION = "IN MEDIATION";
    public static final String ENTER = "Enter";
    public static final String LIFTED = "Lifted";

    public static final String PROCEED = "proceed";
    public static final String NOT_PROCEED = "not proceed";

    public static final String CLAIMANT_PROCEEDS = "Claimant proceeds.";
    public static final String CLAIMANTS_PROCEED = "Claimants proceed.";

    public static final String MISC_TEXT_REQUESTED_IJ = "RPA Reason: Summary judgment requested and referred to judge.";
    public static final String MISC_TEXT_GRANTED_IJ = "RPA Reason: Summary judgment granted and referred to judge.";
    public static final String MISC_TEXT_REQUESTED_DJ = "RPA Reason: Default Judgment requested and claim moved offline.";
    public static final String MISC_TEXT_GRANTED_DJ = "RPA Reason: Default Judgment granted and claim moved offline.";

    public static final String RPA_REASON_ONLY_ONE_OF_THE_RESPONDENT_IS_NOTIFIED = "RPA Reason: Only one of the respondent is notified.";

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

    public static LocalDateTime setApplicant1ResponseDate(CaseData caseData) {
        LocalDateTime applicant1ResponseDate = caseData.getApplicant1ResponseDate();
        if (applicant1ResponseDate == null || applicant1ResponseDate.isBefore(LocalDateTime.now())) {
            applicant1ResponseDate = LocalDateTime.now();
        }
        return applicant1ResponseDate;
    }

    public static String getPreferredCourtCode(DQ dq) {
        return ofNullable(dq.getRequestedCourt())
            .map(RequestedCourt::getResponseCourtCode)
            .orElse("");
    }

    public static boolean isStayClaim(DQ dq) {
        return ofNullable(dq.getFileDirectionQuestionnaire())
            .map(FileDirectionsQuestionnaire::getOneMonthStayRequested)
            .orElse(NO) == YES;
    }

    public static String prepareEventDetailsText(DQ dq, String preferredCourtCode) {
        return format(
            "preferredCourtCode: %s; stayClaim: %s",
            preferredCourtCode,
            isStayClaim(dq)
        );
    }

    public static void buildRespondentResponseText(EventHistory.EventHistoryBuilder builder, CaseData caseData, String miscText, LocalDateTime respondentResponseDate) {
        if (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            builder.miscellaneous(Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(respondentResponseDate)
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                    .miscText(miscText)
                    .build())
                .build());
        }
    }

    public static State getPreviousState(List<State> stateHistory) {
        if (stateHistory.size() > 1) {
            return stateHistory.get(stateHistory.size() - 2);
        } else {
            throw new IllegalStateException("Flow state history should have at least two items: " + stateHistory);
        }
    }
}
