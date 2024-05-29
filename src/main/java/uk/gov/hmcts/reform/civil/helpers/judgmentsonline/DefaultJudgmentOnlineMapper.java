package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
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

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultJudgmentOnlineMapper extends JudgmentOnlineMapper {

    boolean isNonDivergent =  false;
    private final InterestCalculator interestCalculator;

    @Override
    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {
        List<Element<Party>> defendants = new ArrayList<>();
        defendants.add(element(caseData.getRespondent1()));
        if (caseData.isMultiPartyDefendant()) {
            defendants.add(element(caseData.getRespondent2()));
        }
        BigInteger orderAmount = MonetaryConversions.poundsToPennies(JudgmentsOnlineHelper.getDebtAmount(caseData, interestCalculator));
        BigInteger costs = MonetaryConversions.poundsToPennies(JudgmentsOnlineHelper.getCostOfJudgmentForDJ(caseData));
        isNonDivergent =  JudgmentsOnlineHelper.isNonDivergent(caseData);
        JudgmentDetails activeJudgment = super.addUpdateActiveJudgment(caseData);
        return activeJudgment.toBuilder()
            .createdTimestamp(LocalDateTime.now())
            .state(getJudgmentState(caseData))
            .type(JudgmentType.DEFAULT_JUDGMENT)
            .instalmentDetails(DJPaymentTypeSelection.REPAYMENT_PLAN.equals(caseData.getPaymentTypeSelection())
                                   ? getInstalmentDetails(caseData) : null)
            .paymentPlan(getPaymentPlan(caseData))
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
