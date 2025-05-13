package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgmentByAdmissionOnlineMapper extends JudgmentOnlineMapper {

    boolean isNonDivergent = false;
    private final RoboticsAddressMapper addressMapper;
    private final JudgementService judgementService;
    private final InterestCalculator interestCalculator;

    @Override
    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {

        BigDecimal costsInPounds = getCosts(caseData);
        BigInteger costs = MonetaryConversions.poundsToPennies(costsInPounds);
        BigInteger orderAmount = MonetaryConversions.poundsToPennies(getOrderAmount(caseData));
        BigInteger claimFeeAmount = MonetaryConversions.poundsToPennies(getClaimFeeAmount(caseData));
        BigInteger totalStillOwed = MonetaryConversions.poundsToPennies(getTotalStillOwed(caseData));
        BigInteger amountAlreadyPaid = MonetaryConversions.poundsToPennies(getAmountAlreadyPaid(caseData));
        BigInteger totalInterest = nonNull(judgementService.ccjJudgmentInterest(caseData))
            ? MonetaryConversions.poundsToPennies(judgementService.ccjJudgmentInterest(caseData)) : BigInteger.ZERO;
        isNonDivergent = JudgmentsOnlineHelper.isNonDivergentForJBA(caseData);
        PaymentPlanSelection paymentPlan = getPaymentPlan(caseData);

        JudgmentDetails activeJudgment = super.addUpdateActiveJudgment(caseData);
        activeJudgment = super.updateDefendantDetails(activeJudgment, caseData, addressMapper);
        JudgmentDetails activeJudgmentDetails = activeJudgment.toBuilder()
            .createdTimestamp(LocalDateTime.now())
            .state(getJudgmentState(caseData))
            .type(JudgmentType.JUDGMENT_BY_ADMISSION)
            .paymentPlan(JudgmentPaymentPlan.builder()
                             .type(paymentPlan)
                             .paymentDeadlineDate(getPaymentDeadLineDate(caseData, paymentPlan))
                             .build())
            .instalmentDetails(paymentPlan.equals(PaymentPlanSelection.PAY_IN_INSTALMENTS)
                                   ? getInstalmentDetails(caseData) : null)
            .isRegisterWithRTL(isNonDivergent ? YesOrNo.YES : YesOrNo.NO)
            .rtlState(isNonDivergent ? JudgmentRTLStatus.ISSUED.getRtlState() : null)
            .issueDate(LocalDate.now())
            .orderedAmount(addInterest(orderAmount, totalInterest, caseData))
            .costs(costs.toString())
            .claimFeeAmount(claimFeeAmount.toString())
            .amountAlreadyPaid(amountAlreadyPaid.toString())
            .totalAmount(orderAmount.add(costs).add(claimFeeAmount).add(totalInterest).toString())
            .build();

        super.updateJudgmentTabDataWithActiveJudgment(activeJudgmentDetails, caseData);

        return activeJudgmentDetails;
    }

    private String addInterest(BigInteger orderAmount, BigInteger totalInterest, CaseData caseData){
        if (judgementService.isLrFullAdmitRepaymentPlan(caseData)) {
            return orderAmount.toString();
        } else {
            return orderAmount.add(totalInterest).toString();
        }
    }

    public CaseData.CaseDataBuilder addUpdateActiveJudgment(CaseData caseData, CaseData.CaseDataBuilder builder) {
        JudgmentDetails activeJudgmentDetails = addUpdateActiveJudgment(caseData);
        builder.activeJudgment(activeJudgmentDetails);
        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        super.updateJudgmentTabDataWithActiveJudgment(activeJudgmentDetails, builder, interest);
        return builder;
    }

    @NotNull
    private BigDecimal getOrderAmount(CaseData caseData) {
        return caseData.getCcjPaymentDetails() != null
            ? getValue(caseData.getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount()) : BigDecimal.ZERO;
    }

    @NotNull
    private BigDecimal getClaimFeeAmount(CaseData caseData) {
        return caseData.getCcjPaymentDetails() != null
            ? getValue(caseData.getCcjPaymentDetails().getCcjJudgmentAmountClaimFee()) : BigDecimal.ZERO;
    }

    @NotNull
    private BigDecimal getTotalStillOwed(CaseData caseData) {
        return caseData.getCcjPaymentDetails() != null
            ? getValue(caseData.getCcjPaymentDetails().getCcjJudgmentTotalStillOwed()) : BigDecimal.ZERO;
    }

    @NotNull
    private BigDecimal getAmountAlreadyPaid(CaseData caseData) {
        return caseData.getCcjPaymentDetails() != null
            ? getValue(caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeAmountInPounds()) : BigDecimal.ZERO;
    }

    @NotNull
    private static BigDecimal getCosts(CaseData caseData) {
        return caseData.getCcjPaymentDetails() != null && caseData.getCcjPaymentDetails().getCcjJudgmentFixedCostAmount() != null
            ? caseData.getCcjPaymentDetails().getCcjJudgmentFixedCostAmount() : BigDecimal.ZERO;
    }

    @Override
    protected JudgmentState getJudgmentState(CaseData caseData) {
        return isNonDivergent ? JudgmentState.ISSUED : JudgmentState.REQUESTED;
    }

    private BigDecimal getValue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private JudgmentInstalmentDetails getInstalmentDetails(CaseData caseData) {
        if (caseData.hasApplicant1CourtDecisionInFavourOfClaimant()) {
            BigInteger instalmentsAmount =
                MonetaryConversions.poundsToPennies(caseData.getApplicant1SuggestInstalmentsPaymentAmountForDefendantSpec());
            return buildJudgmentInstalmentDetails(
                String.valueOf(instalmentsAmount),
                getClaimantLipSuggestedPaymentFrequency(
                    caseData.getApplicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec()),
                caseData.getApplicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec()
            );
        }

        RepaymentPlanLRspec repaymentPlan = caseData.getRespondent1RepaymentPlan() != null
            ? caseData.getRespondent1RepaymentPlan() : caseData.getRespondent2RepaymentPlan();
        if (repaymentPlan != null) {
            return buildJudgmentInstalmentDetails(
                String.valueOf(getValue(repaymentPlan.getPaymentAmount())),
                getPaymentFrequency(repaymentPlan.getRepaymentFrequency()),
                repaymentPlan.getFirstRepaymentDate()
            );
        }
        return null;
    }

    private JudgmentInstalmentDetails buildJudgmentInstalmentDetails(
        String paymentAmount, PaymentFrequency paymentFrequency, LocalDate firstRepaymentDate) {
        return JudgmentInstalmentDetails.builder()
            .amount(paymentAmount)
            .paymentFrequency(paymentFrequency)
            .startDate(firstRepaymentDate)
            .build();
    }

    private PaymentFrequency getPaymentFrequency(PaymentFrequencyLRspec frequencyLRspec) {
        switch (frequencyLRspec) {
            case ONCE_ONE_WEEK:
                return PaymentFrequency.WEEKLY;
            case ONCE_TWO_WEEKS:
                return PaymentFrequency.EVERY_TWO_WEEKS;
            default:
                return PaymentFrequency.MONTHLY;
        }
    }

    private PaymentFrequency getClaimantLipSuggestedPaymentFrequency(PaymentFrequencyClaimantResponseLRspec repaymentFrequency) {
        return switch (repaymentFrequency) {
            case ONCE_ONE_WEEK -> PaymentFrequency.WEEKLY;
            case ONCE_TWO_WEEKS -> PaymentFrequency.EVERY_TWO_WEEKS;
            default -> PaymentFrequency.MONTHLY;
        };
    }

    private LocalDate getPaymentDeadLineDate(CaseData caseData, PaymentPlanSelection paymentPlan) {
        if (PaymentPlanSelection.PAY_BY_DATE.equals(paymentPlan)) {
            if (caseData.hasApplicant1CourtDecisionInFavourOfClaimant()) {
                return (caseData.getApplicant1RequestedPaymentDateForDefendantSpec()) != null
                    ? caseData.getApplicant1RequestedPaymentDateForDefendantSpec().getPaymentSetDate() : null;
            }
            return caseData.getRespondToClaimAdmitPartLRspec() != null
                ? caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid() : null;
        }
        return null;
    }

    private PaymentPlanSelection getPaymentPlan(CaseData caseData) {
        if (caseData.hasApplicant1CourtDecisionInFavourOfClaimant()) {
            if (PaymentType.REPAYMENT_PLAN.equals(caseData.getApplicant1RepaymentOptionForDefendantSpec())) {
                return PaymentPlanSelection.PAY_IN_INSTALMENTS;
            } else if (PaymentType.SET_DATE.equals(caseData.getApplicant1RepaymentOptionForDefendantSpec())) {
                return PaymentPlanSelection.PAY_BY_DATE;
            }
            return PaymentPlanSelection.PAY_IMMEDIATELY;
        }

        if (caseData.isPayByInstallment()) {
            return PaymentPlanSelection.PAY_IN_INSTALMENTS;
        } else if (caseData.isPayBySetDate()) {
            return PaymentPlanSelection.PAY_BY_DATE;
        }
        return PaymentPlanSelection.PAY_IMMEDIATELY;
    }
}
