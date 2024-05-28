package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgmentByAdmissionOnlineMapper extends JudgmentOnlineMapper {

    boolean isNonDivergent = false;

    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {
        List<Element<Party>> defendants = new ArrayList<Element<Party>>();
        defendants.add(element(caseData.getRespondent1()));
        if (caseData.isMultiPartyDefendant()) {
            defendants.add(element(caseData.getRespondent2()));
        }
        BigDecimal costs = getValue(caseData.getCcjPaymentDetails().getCcjJudgmentFixedCostAmount());
        BigDecimal orderAmount = getValue(caseData.getCcjPaymentDetails().getCcjJudgmentTotalStillOwed()).subtract(costs);
        isNonDivergent = JudgmentsOnlineHelper.isNonDivergent(caseData);
        PaymentPlanSelection paymentPlan = caseData.isPayByInstallment()
            ? PaymentPlanSelection.PAY_IN_INSTALMENTS : caseData.isPayBySetDate()
            ? PaymentPlanSelection.PAY_BY_DATE : PaymentPlanSelection.PAY_IMMEDIATELY;

        JudgmentDetails activeJudgment = super.addUpdateActiveJudgment(caseData);
        return activeJudgment.toBuilder()
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
            .issueDate(LocalDate.now())
            .orderedAmount(orderAmount.toString())
            .costs(costs.toString())
            .totalAmount(orderAmount.add(costs).toString())
            .build();
    }

    @Override
    protected JudgmentState getJudgmentState(CaseData caseData) {
        return isNonDivergent ? JudgmentState.ISSUED : JudgmentState.REQUESTED;
    }

    private BigDecimal getValue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private JudgmentInstalmentDetails getInstalmentDetails(CaseData caseData) {

        RepaymentPlanLRspec repaymentPlan = caseData.getRespondent1RepaymentPlan() != null
            ? caseData.getRespondent1RepaymentPlan() : caseData.getRespondent2RepaymentPlan();
        if (repaymentPlan != null) {
            return JudgmentInstalmentDetails.builder()
                .amount(String.valueOf(getValue(repaymentPlan.getPaymentAmount())))
                .paymentFrequency(getPaymentFrequency(repaymentPlan.getRepaymentFrequency()))
                .startDate(repaymentPlan.getFirstRepaymentDate())
                .build();
        }
        return null;

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

    private LocalDate getPaymentDeadLineDate(CaseData caseData, PaymentPlanSelection paymentPlan) {
        return PaymentPlanSelection.PAY_BY_DATE.equals(paymentPlan)
            ? caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid() : null;
    }
}
