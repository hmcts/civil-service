package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;

import java.time.LocalDate;

public class JudgmentsOnlineHelper {

    private JudgmentsOnlineHelper() {
        // Utility class, no instances
    }

    public static String getRTLStatusBasedOnJudgementStatus(JudgmentStatusType judgmentStatus) {
        switch (judgmentStatus) {
            case ISSUED : return JudgmentRTLStatus.REGISTRATION.getRtlState();
            case MODIFIED: return JudgmentRTLStatus.MODIFIED.getRtlState();
            case CANCELLED: return JudgmentRTLStatus.CANCELLATION.getRtlState();
            case SATISFIED: return JudgmentRTLStatus.SATISFACTION.getRtlState();
            default: return "";
        }
    }

    public static String validateIfFutureDates(LocalDate joOrderMadeDate) {
        LocalDate today = LocalDate.now();
        if (joOrderMadeDate.isAfter(today)) {
            return "Date must be in the past";
        } else {
            return null;
        }
    }
}
