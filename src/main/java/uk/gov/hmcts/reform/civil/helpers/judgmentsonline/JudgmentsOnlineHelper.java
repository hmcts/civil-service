package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class JudgmentsOnlineHelper {

    private static final String ERROR_MESSAGE_DATE_PAID_BY_MUST_BE_IN_FUTURE = "Date the judgment will be paid by must be in the future";
    private static final String ERROR_MESSAGE_DATE_FIRST_INSTALMENT_MUST_BE_IN_FUTURE = "Date of first instalment must be in the future";
    private static final String ERROR_MESSAGE_DATE_ORDER_MUST_BE_IN_PAST = "Date judge made the order must be in the past";
    
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

    public static boolean checkIfDateDifferenceIsGreaterThan31Days(LocalDate firstDate, LocalDate secondDate) {
        return ChronoUnit.DAYS.between(firstDate, secondDate) > 31;
    }

    public static List<String> validateMidCallbackData(CaseData caseData) {

        List<String> errors = new ArrayList<>();
        boolean isOrderMadeFutureDate =
            JudgmentsOnlineHelper.validateIfFutureDate(caseData.getJoOrderMadeDate());
        if (isOrderMadeFutureDate) {
            errors.add(ERROR_MESSAGE_DATE_ORDER_MUST_BE_IN_PAST);
        }
        if (caseData.getJoPaymentPlanSelection().equals(PaymentPlanSelection.PAY_BY_DATE)) {
            boolean isFutureDate =
                JudgmentsOnlineHelper.validateIfFutureDate(caseData.getJoPaymentToBeMadeByDate());
            if (!isFutureDate) {
                errors.add(ERROR_MESSAGE_DATE_PAID_BY_MUST_BE_IN_FUTURE);
            }
        } else if (caseData.getJoPaymentPlanSelection().equals(PaymentPlanSelection.PAY_IN_INSTALMENTS)) {
            boolean isFutureDate =
                JudgmentsOnlineHelper.validateIfFutureDate(caseData.getJoJudgmentInstalmentDetails().getFirstInstalmentDate());
            if (!isFutureDate) {
                errors.add(ERROR_MESSAGE_DATE_FIRST_INSTALMENT_MUST_BE_IN_FUTURE);
            }
        }
        return errors;
    }
}
