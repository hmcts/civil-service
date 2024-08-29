package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
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

    public static boolean validateIfFutureDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        return date.isAfter(today);
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

    public static boolean isNonDivergentForDJ(CaseData caseData) {
        return  MultiPartyScenario.isOneVOne(caseData)
            || MultiPartyScenario.isTwoVOne(caseData)
            || (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getDefendantDetailsSpec()).isPresent()
            && ofNullable(caseData.getDefendantDetailsSpec().getValue()).isPresent()
            && caseData.getDefendantDetailsSpec().getValue().getLabel().startsWith("Both"));
    }

    public static boolean isNonDivergentForJBA(CaseData caseData) {
        return  MultiPartyScenario.isOneVOne(caseData)
            || MultiPartyScenario.isTwoVOne(caseData)
            || caseData.isLRvLipOneVOne();
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

    @NotNull
    public static String calculateRepaymentBreakdownSummary(JudgmentDetails activeJudgment) {

        BigDecimal totalAmount = MonetaryConversions.penniesToPounds(new BigDecimal(activeJudgment.getTotalAmount()));

        //creates  the text on the page, based on calculated values
        StringBuilder repaymentBreakdown = new StringBuilder();
        repaymentBreakdown.append("The judgment will order the defendants to pay £").append(totalAmount);
        repaymentBreakdown.append(", including the claim fee and interest, if applicable, as shown:");

        BigDecimal orderedAmount = MonetaryConversions.penniesToPounds(new BigDecimal(activeJudgment.getOrderedAmount()));
        if (null != orderedAmount) {
            repaymentBreakdown.append("\n").append("### Claim amount \n £").append(orderedAmount.setScale(2));
        }

        BigDecimal costs = MonetaryConversions.penniesToPounds(new BigDecimal(activeJudgment.getCosts()));
        if (null != costs) {
            repaymentBreakdown.append("\n ### Fixed cost amount \n").append("£").append(costs.setScale(2));
        }

        repaymentBreakdown.append("\n ## Subtotal \n £").append(totalAmount.setScale(2))
            .append("\n");
        repaymentBreakdown.append("\n ## Total still owed \n £").append(totalAmount.setScale(2));

        return repaymentBreakdown.toString();
    }

}
