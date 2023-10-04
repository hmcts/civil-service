package uk.gov.hmcts.reform.civil.service.citizen.repaymentplan;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentPlanDecisionDto;

@Service
@RequiredArgsConstructor
public class RepaymentPlanDecisionCalculator {

    private IncomeCalculator incomeCalculator;
    private ExpenditureCalculator expenditureCalculator;
    private AllowanceCalculator allowanceCalculator;

    public RepaymentPlanDecisionDto calculateRepaymentDecision(CaseData caseData) {



    }

    private double calculateDisposableIncome(CaseData caseData) {
        double calculatedIncome = incomeCalculator.calculateTotalMonthlyIncome(caseData.getRespondent1DQ().getRespondent1BankAccountList(),
                                                                               caseData.getRecurringIncomeForRespondent1(),
                                                                               caseData.getSpecDefendant1SelfEmploymentDetails());
        double calculatedExpenditure = expenditureCalculator.calculateTotalExpenditure(caseData.getRecurringExpensesForRespondent1(),
                                                                                       caseData.getSpecDefendant1Debts());
        return calculatedIncome - calculatedExpenditure;
    }


}
