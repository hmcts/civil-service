package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultJudgmentOnlineMapper extends JudgmentOnlineMapper {

    boolean isNonDivergent =  false;
    private final InterestCalculator interestCalculator;
    private final RoboticsAddressMapper addressMapper;

    @Override
    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {

        BigInteger orderAmount = MonetaryConversions.poundsToPennies(JudgmentsOnlineHelper.getDebtAmount(caseData, interestCalculator));
        BigInteger costs = MonetaryConversions.poundsToPennies(JudgmentsOnlineHelper.getCostOfJudgmentForDJ(caseData));
        isNonDivergent =  JudgmentsOnlineHelper.isNonDivergentForDJ(caseData);
        JudgmentDetails activeJudgment = super.addUpdateActiveJudgment(caseData);
        activeJudgment = super.updateDefendantDetails(activeJudgment, caseData, addressMapper);
        JudgmentDetails judgmentDetails = activeJudgment.toBuilder()
            .createdTimestamp(LocalDateTime.now())
            .state(getJudgmentState(caseData))
            .type(JudgmentType.DEFAULT_JUDGMENT)
            .instalmentDetails(DJPaymentTypeSelection.REPAYMENT_PLAN.equals(caseData.getPaymentTypeSelection())
                                   ? getInstalmentDetails(caseData) : null)
            .paymentPlan(getPaymentPlan(caseData))
            .isRegisterWithRTL(isNonDivergent ? YesOrNo.YES : YesOrNo.NO)
            .rtlState(isNonDivergent ? JudgmentRTLStatus.ISSUED.getRtlState() : null)
            .issueDate(LocalDate.now())
            .orderedAmount(orderAmount.toString())
            .costs(costs.toString())
            .totalAmount(orderAmount.add(costs).toString())
            .build();
        super.updateJudgmentTabDataWithActiveJudgment(judgmentDetails, caseData);

        return judgmentDetails;
    }

    public CaseData.CaseDataBuilder addUpdateActiveJudgment(CaseData caseData, CaseData.CaseDataBuilder builder) {
        JudgmentDetails activeJudgmentDetails = this.addUpdateActiveJudgment(caseData);
        builder.activeJudgment(activeJudgmentDetails);
        super.updateJudgmentTabDataWithActiveJudgment(activeJudgmentDetails, builder);
        return builder;
    }

    @Override
    protected JudgmentState getJudgmentState(CaseData caseData) {
        return isNonDivergent ? JudgmentState.ISSUED : JudgmentState.REQUESTED;
    }

    private JudgmentInstalmentDetails getInstalmentDetails(CaseData caseData) {
        return JudgmentInstalmentDetails.builder()
            .amount(caseData.getRepaymentSuggestion())
            .startDate(caseData.getRepaymentDate())
            .paymentFrequency(getPaymentFrequency(caseData.getRepaymentFrequency())).build();
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
        return JudgmentPaymentPlan.builder()
            .type(getPaymentPlanSeletion(caseData.getPaymentTypeSelection()))
            .paymentDeadlineDate(getPaymentDeadLineDate(caseData))
            .build();
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
