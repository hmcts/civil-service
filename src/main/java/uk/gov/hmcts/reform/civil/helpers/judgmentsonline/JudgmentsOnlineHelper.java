package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;

public class JudgmentsOnlineHelper {

    private static final String ERROR_MESSAGE_DATE_PAID_BY_MUST_BE_IN_FUTURE = "Date the judgment will be paid by must be in the future";
    private static final String ERROR_MESSAGE_DATE_FIRST_INSTALMENT_MUST_BE_IN_FUTURE = "Date of first instalment must be in the future";
    private static final String ERROR_MESSAGE_DATE_ORDER_MUST_BE_IN_PAST = "Date judge made the order must be in the past";

    private JudgmentsOnlineHelper() {
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
        if (caseData.getJoPaymentPlan().getType().equals(PaymentPlanSelection.PAY_BY_DATE)) {
            boolean isFutureDate =
                JudgmentsOnlineHelper.validateIfFutureDate(caseData.getJoPaymentPlan().getPaymentDeadlineDate());
            if (!isFutureDate) {
                errors.add(ERROR_MESSAGE_DATE_PAID_BY_MUST_BE_IN_FUTURE);
            }
        } else if (caseData.getJoPaymentPlan().getType().equals(PaymentPlanSelection.PAY_IN_INSTALMENTS)) {
            boolean isFutureDate =
                JudgmentsOnlineHelper.validateIfFutureDate(caseData.getJoInstalmentDetails().getStartDate());
            if (!isFutureDate) {
                errors.add(ERROR_MESSAGE_DATE_FIRST_INSTALMENT_MUST_BE_IN_FUTURE);
            }
        }
        return errors;
    }

    public static boolean isNonDivergent(CaseData caseData) {
        return  MultiPartyScenario.isOneVOne(caseData)
            || MultiPartyScenario.isTwoVOne(caseData)
            || (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getDefendantDetailsSpec()).isPresent()
            && ofNullable(caseData.getDefendantDetailsSpec().getValue()).isPresent()
            && caseData.getDefendantDetailsSpec().getValue().getLabel().startsWith("Both"));
    }

    public static BigDecimal getCostOfJudgmentForDJ(CaseData data) {

        if (data.getOutstandingFeeInPounds() != null) {
            return data.getOutstandingFeeInPounds();
        }

        String repaymentSummary = data.getRepaymentSummaryObject();
        BigDecimal fixedCost = null;
        BigDecimal claimCost = null;
        if (null != repaymentSummary) {
            fixedCost = repaymentSummary.contains("Fixed")
                ? new BigDecimal(repaymentSummary.substring(
                repaymentSummary.indexOf("Fixed cost amount \n£") + 20,
                repaymentSummary.indexOf("\n### Claim fee amount ")
            )) : null;
            claimCost = new BigDecimal(repaymentSummary.substring(
                repaymentSummary.indexOf("Claim fee amount \n £") + 20,
                repaymentSummary.indexOf("\n ## Subtotal")
            ));
        }

        return fixedCost != null && claimCost != null ? fixedCost.add(claimCost)
            : claimCost != null ? claimCost : ZERO;

    }

    public static BigDecimal getDebtAmount(CaseData caseData, InterestCalculator interestCalculator) {
        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        var subTotal = caseData.getTotalClaimAmount()
            .add(interest);
        subTotal = subTotal.subtract(getPartialPayment(caseData));

        return subTotal;
    }

    public static BigDecimal getPartialPayment(CaseData caseData) {

        BigDecimal partialPaymentPounds = new BigDecimal(0);
        //Check if partial payment was selected by user, and assign value if so.
        if (caseData.getPartialPaymentAmount() != null) {
            var partialPaymentPennies = new BigDecimal(caseData.getPartialPaymentAmount());
            partialPaymentPounds = MonetaryConversions.penniesToPounds(partialPaymentPennies);
        }
        return partialPaymentPounds;
    }

    public static BigDecimal getMoneyValue(String val) {
        return val != null ? new BigDecimal(val) : ZERO;
    }

}
