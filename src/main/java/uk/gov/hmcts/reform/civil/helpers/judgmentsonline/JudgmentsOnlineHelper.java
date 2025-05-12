package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import camundajar.impl.scala.collection.mutable.StringBuilder;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentAddress;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.DefaultJudgmentUtils;
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

    private static final String regex = "[ˆ`´¨]";

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

    @NotNull
    public static BigDecimal getJudgmentAmount(CaseData caseData, InterestCalculator interestCalculator) {
        BigDecimal judgmentAmount = calculateJudgmentAmountForFixedCosts(caseData, interestCalculator)
            .add(JudgmentsOnlineHelper.getFixedCostsOnCommencement(caseData)).add(getClaimFeePounds(caseData, caseData.getClaimFee()));
        return judgmentAmount;
    }

    public static BigDecimal getClaimFeePounds(CaseData caseData, Fee claimfee) {
        BigDecimal claimFeePounds;
        if (caseData.getOutstandingFeeInPounds() != null) {
            claimFeePounds = caseData.getOutstandingFeeInPounds();
        } else {
            claimFeePounds = MonetaryConversions.penniesToPounds(claimfee.getCalculatedAmountInPence());
        }
        return claimFeePounds;
    }

    private static BigDecimal calculateJudgmentAmountForFixedCosts(CaseData caseData, InterestCalculator interestCalculator) {
        BigDecimal interest = interestCalculator.calculateInterest(caseData);

        BigDecimal subTotal = caseData.getTotalClaimAmount().add(interest);
        BigDecimal partialPaymentPounds = getPartialPayment(caseData);
        return calculateOverallTotal(partialPaymentPounds, subTotal);
    }

    private static BigDecimal calculateOverallTotal(BigDecimal partialPaymentPounds, BigDecimal subTotal) {
        return subTotal.subtract(partialPaymentPounds);
    }

    public static BigDecimal getFixedCostsOnCommencement(CaseData caseData) {
        BigDecimal fixedCostsCommencement = BigDecimal.valueOf(0);
        if (caseData.getFixedCosts() != null && YesOrNo.YES.equals(caseData.getFixedCosts().getClaimFixedCosts())) {
            fixedCostsCommencement = MonetaryConversions.penniesToPounds(BigDecimal.valueOf(Integer.parseInt(
                caseData.getFixedCosts().getFixedCostAmount())));
        }
        return fixedCostsCommencement;
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
    public static String calculateRepaymentBreakdownSummary(JudgmentDetails activeJudgment, BigDecimal interest) {

        BigDecimal totalAmount = MonetaryConversions.penniesToPounds(new BigDecimal(activeJudgment.getTotalAmount()));

        //creates  the text on the page, based on calculated values
        StringBuilder repaymentBreakdown = new StringBuilder();
        repaymentBreakdown.append("The judgment will order the defendants to pay £").append(totalAmount);
        repaymentBreakdown.append(", including the claim fee and interest, if applicable, as shown:");

        BigDecimal orderedAmount = MonetaryConversions.penniesToPounds(new BigDecimal(activeJudgment.getOrderedAmount()));
        if (null != orderedAmount) {
            repaymentBreakdown.append("\n").append("### Claim amount \n £").append(orderedAmount.setScale(2));
        }

        if (interest != null && interest.compareTo(BigDecimal.ZERO) != 0) {
            repaymentBreakdown.append("\n ### Claim interest amount \n").append("£").append(interest.setScale(2));
        }

        if (null != activeJudgment.getCosts()) {
            BigDecimal costs = MonetaryConversions.penniesToPounds(new BigDecimal(activeJudgment.getCosts()));
            if (costs.compareTo(BigDecimal.ZERO) != 0) {
                repaymentBreakdown.append("\n ### Fixed cost amount \n").append("£").append(costs.setScale(2));
            }
        }

        if (null != activeJudgment.getClaimFeeAmount()) {
            BigDecimal claimFeeAmount = MonetaryConversions.penniesToPounds(new BigDecimal(activeJudgment.getClaimFeeAmount()));
            if (claimFeeAmount.compareTo(BigDecimal.ZERO) != 0) {
                repaymentBreakdown.append("\n ### Claim fee amount \n").append("£").append(claimFeeAmount.setScale(2));
            }
        }

        repaymentBreakdown.append("\n ## Subtotal \n £").append(totalAmount.setScale(2))
            .append("\n");

        BigDecimal amountAlreadyPaid = ZERO;
        if (null != activeJudgment.getAmountAlreadyPaid()) {
            amountAlreadyPaid = MonetaryConversions.penniesToPounds(new BigDecimal(activeJudgment.getAmountAlreadyPaid()));
            if (amountAlreadyPaid.compareTo(BigDecimal.ZERO) != 0) {
                repaymentBreakdown.append("\n ### Amount already paid \n").append("£").append(amountAlreadyPaid.setScale(
                    2));
            }
        }

        BigDecimal totalStillOwed = (null != activeJudgment.getAmountAlreadyPaid())
            ? totalAmount.subtract(amountAlreadyPaid)
            : totalAmount;
        repaymentBreakdown.append("\n ## Total still owed \n £").append(totalStillOwed.setScale(2));

        return repaymentBreakdown.toString();
    }

    public static String getRepaymentBreakdownSummaryForJO(JudgmentDetails activeJudgment, BigDecimal interest,
                                                                  CaseData caseData) {

        BigDecimal claimAmountTotal = MonetaryConversions
            .penniesToPounds(new BigDecimal(activeJudgment.getOrderedAmount()))
            .setScale(2);
        if (interest != null) {
            claimAmountTotal = claimAmountTotal.add(
                interest.setScale(2)
            );
        }

        BigDecimal commencementFixedCosts = DefaultJudgmentUtils
            .calculateFixedCosts(caseData)
            .setScale(2);

        BigDecimal costs = BigDecimal.ZERO;
        if (activeJudgment.getCosts() != null) {
            costs = MonetaryConversions
                .penniesToPounds(new BigDecimal(activeJudgment.getCosts()))
                .setScale(2);
        }
        BigDecimal fixedCostTotal = commencementFixedCosts.add(costs);

        BigDecimal claimFeeAmount = BigDecimal.ZERO;
        if (activeJudgment.getClaimFeeAmount() != null) {
            claimFeeAmount = MonetaryConversions
                .penniesToPounds(new BigDecimal(activeJudgment.getClaimFeeAmount()))
                .setScale(2);
            if (claimFeeAmount.compareTo(BigDecimal.ZERO) == 0) {
                claimFeeAmount = BigDecimal.ZERO;
            }
        }

        StringBuilder repaymentBreakdown = new StringBuilder();

        BigDecimal totalAmount = claimAmountTotal
            .add(fixedCostTotal)
            .add(claimFeeAmount);

        repaymentBreakdown
            .append("The judgment will order the defendants to pay £")
            .append(totalAmount.setScale(2))
            .append(" which include the amounts shown:");

        repaymentBreakdown
            .append("\n### Claim amount\n£")
            .append(claimAmountTotal);

        if (fixedCostTotal.compareTo(BigDecimal.ZERO) > 0) {
            repaymentBreakdown
                .append("\n### Fixed cost amount\n£")
                .append(fixedCostTotal);
        }

        if (claimFeeAmount.compareTo(BigDecimal.ZERO) > 0) {
            repaymentBreakdown
                .append("\n### Claim fee amount\n£")
                .append(claimFeeAmount);
        }

        repaymentBreakdown
            .append("\n## Subtotal\n£")
            .append(totalAmount.setScale(2))
            .append("\n");

        return repaymentBreakdown.toString();
    }

    public static JudgmentAddress getJudgmentAddress(Address address, RoboticsAddressMapper addressMapper) {

        Address newAddress = Address.builder()
            .addressLine1(removeWelshCharacters(address.getAddressLine1()))
            .addressLine2(removeWelshCharacters(address.getAddressLine2()))
            .addressLine3(removeWelshCharacters(address.getAddressLine3()))
            .postCode(removeWelshCharacters(address.getPostCode()))
            .postTown(removeWelshCharacters(address.getPostTown()))
            .county(removeWelshCharacters(address.getCounty()))
            .country(removeWelshCharacters(address.getCountry())).build();

        RoboticsAddress roboticsAddress = addressMapper.toRoboticsAddress(newAddress);
        return JudgmentAddress.builder()
            .defendantAddressLine1(trimDownTo35(roboticsAddress.getAddressLine1()))
            .defendantAddressLine2(trimDownTo35(roboticsAddress.getAddressLine2()))
            .defendantAddressLine3(trimDownTo35(roboticsAddress.getAddressLine3()))
            .defendantAddressLine4(trimDownTo35(roboticsAddress.getAddressLine4()))
            .defendantAddressLine5(trimDownTo35(roboticsAddress.getAddressLine5()))
            .defendantPostCode(roboticsAddress.getPostCode()).build();
    }

    public static String removeWelshCharacters(String input) {
        return input != null ? input.replaceAll(regex, "") : input;
    }

    private static String trimDownTo35(String input) {
        return input != null && input.length() > 35 ? input.substring(0, 35) : input;
    }

    public static  String formatAddress(JudgmentAddress address) {
        String formattedLine = new StringBuilder()
            .addAll(formatAddressLine(address.getDefendantAddressLine1()))
            .addAll(formatAddressLine(address.getDefendantAddressLine2()))
            .addAll(formatAddressLine(address.getDefendantAddressLine3()))
            .addAll(formatAddressLine(address.getDefendantAddressLine4()))
            .addAll(formatAddressLine(address.getDefendantAddressLine5()))
            .result().trim();
        return formattedLine.length() > 0 ? formattedLine.substring(0, formattedLine.length() - 1) : "";
    }

    private static String formatAddressLine(String line) {
        return line != null ? line + ", " : "";
    }
}
