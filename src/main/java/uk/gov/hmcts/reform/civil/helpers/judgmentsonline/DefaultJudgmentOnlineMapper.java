package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
public class DefaultJudgmentOnlineMapper extends JudgmentOnlineMapper {

    boolean isNonDivergent =  false;
    private final InterestCalculator interestCalculator;
    private final RoboticsAddressMapper addressMapper;

    public DefaultJudgmentOnlineMapper(Time time, InterestCalculator interestCalculator, RoboticsAddressMapper addressMapper) {
        super(time);
        this.interestCalculator = interestCalculator;
        this.addressMapper = addressMapper;
    }

    @Override
    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {

        BigInteger orderAmount = MonetaryConversions.poundsToPennies(JudgmentsOnlineHelper.getDebtAmount(caseData, interestCalculator));
        BigInteger costs = MonetaryConversions.poundsToPennies(JudgmentsOnlineHelper.getFixedCostsOfJudgmentForDJ(caseData));
        BigInteger claimFee = MonetaryConversions.poundsToPennies(JudgmentsOnlineHelper.getClaimFeeOfJudgmentForDJ(caseData));
        isNonDivergent =  JudgmentsOnlineHelper.isNonDivergentForDJ(caseData);
        JudgmentDetails activeJudgment = super.addUpdateActiveJudgment(caseData);
        activeJudgment = super.updateDefendantDetails(activeJudgment, caseData, addressMapper);
        activeJudgment
            .setCreatedTimestamp(LocalDateTime.now())
            .setState(getJudgmentState(caseData))
            .setType(JudgmentType.DEFAULT_JUDGMENT)
            .setInstalmentDetails(DJPaymentTypeSelection.REPAYMENT_PLAN.equals(caseData.getPaymentTypeSelection())
                                   ? getInstalmentDetails(caseData) : null)
            .setPaymentPlan(getPaymentPlan(caseData))
            .setIsRegisterWithRTL(isNonDivergent ? YesOrNo.YES : YesOrNo.NO)
            .setRtlState(isNonDivergent ? JudgmentRTLStatus.ISSUED.getRtlState() : null)
            .setIssueDate(LocalDate.now())
            .setOrderedAmount(orderAmount.toString())
            .setClaimFeeAmount(claimFee.toString())
            .setCosts(costs.toString())
            .setTotalAmount(orderAmount.add(costs).add(claimFee).toString());
        super.updateJudgmentTabDataWithActiveJudgment(activeJudgment, caseData);

        return activeJudgment;
    }

    @Override
    protected JudgmentState getJudgmentState(CaseData caseData) {
        return isNonDivergent ? JudgmentState.ISSUED : JudgmentState.REQUESTED;
    }

    private JudgmentInstalmentDetails getInstalmentDetails(CaseData caseData) {
        return new JudgmentInstalmentDetails()
            .setAmount(caseData.getRepaymentSuggestion())
            .setStartDate(caseData.getRepaymentDate())
            .setPaymentFrequency(getPaymentFrequency(caseData.getRepaymentFrequency()));
    }

    private PaymentFrequency getPaymentFrequency(RepaymentFrequencyDJ freqDJ) {
        switch (freqDJ) {
            case ONCE_ONE_WEEK:
                return PaymentFrequency.WEEKLY;
            case ONCE_ONE_MONTH:
                return PaymentFrequency.MONTHLY;
            case ONCE_TWO_WEEKS:
                return PaymentFrequency.EVERY_TWO_WEEKS;
            default:
                return null;
        }
    }

    private JudgmentPaymentPlan getPaymentPlan(CaseData caseData) {
        return new JudgmentPaymentPlan()
            .setType(getPaymentPlanSeletion(caseData.getPaymentTypeSelection()))
            .setPaymentDeadlineDate(getPaymentDeadLineDate(caseData));
    }

    private PaymentPlanSelection getPaymentPlanSeletion(DJPaymentTypeSelection paymentType) {
        switch (paymentType) {
            case IMMEDIATELY:
                return PaymentPlanSelection.PAY_IMMEDIATELY;
            case SET_DATE:
                return PaymentPlanSelection.PAY_BY_DATE;
            case REPAYMENT_PLAN:
                return PaymentPlanSelection.PAY_IN_INSTALMENTS;
            default:
                return null;
        }
    }

    private LocalDate getPaymentDeadLineDate(CaseData caseData) {
        return DJPaymentTypeSelection.SET_DATE.equals(caseData.getPaymentTypeSelection()) ? caseData.getPaymentSetDate() : null;
    }
}
