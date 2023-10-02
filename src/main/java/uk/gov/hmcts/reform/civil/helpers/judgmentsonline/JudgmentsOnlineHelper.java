package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class JudgmentsOnlineHelper {

    private JudgmentsOnlineHelper() {
        // Utility class, no instances
    }

    public static String getRTLStatusBasedOnJudgementStatus(JudgmentStatusType judgmentStatus) {
        switch (judgmentStatus) {
            case ISSUED : return JudgmentRTLStatus.REGISTRATION.getRtlState();
            case MODIFIED: return JudgmentRTLStatus.MODIFIED.getRtlState();
            case CANCELLED: return JudgmentRTLStatus.CANCELLATION.getRtlState();
            case SET_ASIDE: return JudgmentRTLStatus.CANCELLATION.getRtlState();
            case SATISFIED: return JudgmentRTLStatus.SATISFACTION.getRtlState();
            default: return "";
        }
    }

    public static boolean validateIfFutureDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isAfter(today)) {
            return true;
        }
        return false;
    }

    public static boolean checkIfDateDifferenceIsGreaterThan30Days(LocalDate firstDate, LocalDate secondDate) {
        if (ChronoUnit.DAYS.between(firstDate, secondDate) > 30) {
            return true;
        }
        return false;
    }
}
