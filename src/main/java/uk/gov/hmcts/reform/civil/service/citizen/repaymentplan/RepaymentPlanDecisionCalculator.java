package uk.gov.hmcts.reform.civil.service.citizen.repaymentplan;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentPlanDecisionDto;
import uk.gov.hmcts.reform.civil.model.repaymentplan.ClaimantProposedPlan;

import java.math.BigDecimal;
import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;

@Service
@RequiredArgsConstructor
public class RepaymentPlanDecisionCalculator {

    private IncomeCalculator incomeCalculator;
    private ExpenditureCalculator expenditureCalculator;
    private AllowanceCalculator allowanceCalculator;

    public RepaymentPlanDecisionDto calculateRepaymentDecision(CaseData caseData, ClaimantProposedPlan claimantProposedPlan ) {
      double disposableIncome = calculateDisposableIncome(caseData);
      BigDecimal claimTotalAmount = caseData.getTotalClaimAmount();
      RepaymentPlanLRspec defendantRepaymentPlan = caseData.getRespondent1RepaymentPlan();
      RespondentResponsePartAdmissionPaymentTimeLRspec respondentResponseType = caseData.getDefenceAdmitPartPaymentTimeRouteRequired();
      LocalDate proposedDefendantRepaymentDate = respondentResponseType == BY_SET_DATE ?
          caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(): defendantRepaymentPlan.finalPaymentBy(claimTotalAmount);
      if(claimantProposedPlan)

    }

    private double calculateDisposableIncome(CaseData caseData) {
        double calculatedIncome = incomeCalculator.calculateTotalMonthlyIncome(caseData.getRespondent1DQ().getRespondent1BankAccountList(),
                                                                               caseData.getRecurringIncomeForRespondent1(),
                                                                               caseData.getSpecDefendant1SelfEmploymentDetails());
        double calculatedExpenditure = expenditureCalculator.calculateTotalExpenditure(caseData.getRecurringExpensesForRespondent1(),
                                                                                       caseData.getSpecDefendant1Debts(),
                                                                                       caseData.getRespondent1CourtOrderDetails());
        double calculatedAllowance = allowanceCalculator.calculateAllowance(caseData);
        return calculatedIncome - calculatedExpenditure - calculatedAllowance;
    }


}
