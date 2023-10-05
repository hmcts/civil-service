package uk.gov.hmcts.reform.civil.service.citizen.repaymentplan;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentPlanDecisionDto;
import uk.gov.hmcts.reform.civil.model.repaymentplan.ClaimantProposedPlan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;

@Service
@RequiredArgsConstructor
public class RepaymentPlanDecisionCalculator {

    private IncomeCalculator incomeCalculator;
    private ExpenditureCalculator expenditureCalculator;
    private AllowanceCalculator allowanceCalculator;

    public RepaymentDecisionType calculateRepaymentDecision(CaseData caseData, ClaimantProposedPlan claimantProposedPlan ) {
      double disposableIncome = calculateDisposableIncome(caseData);
      BigDecimal claimTotalAmount = Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmountPounds()).orElse(caseData.getTotalClaimAmount());
      LocalDate proposedDefendantRepaymentDate = getProposedDefendantRepaymentDate(caseData, claimTotalAmount);

      if(claimantProposedPlan.hasProposedPayImmediatly()){
          return calculateDecisionBasedOnAmountAndDisposableIncome(claimTotalAmount.doubleValue(), disposableIncome);
      }
      if(claimantProposedPlan.hasProposedPayBySetDate()) {
          return calculateDecisionBasedOnProposedDate(proposedDefendantRepaymentDate, claimantProposedPlan.getRepaymentByDate());
      }
      if(claimantProposedPlan.hasProposedPayByInstallments()) {
         return calculateDecisionBasedOnAmountAndDisposableIncome(claimantProposedPlan.getCalculatedPaymentPerMonthFromRepaymentPlan(), disposableIncome);
      }
      return RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT;
    }

    private LocalDate getProposedDefendantRepaymentDate(CaseData caseData, BigDecimal claimTotalAmount) {
        RespondentResponsePartAdmissionPaymentTimeLRspec respondentResponseType = caseData.getDefenceAdmitPartPaymentTimeRouteRequired();
        RepaymentPlanLRspec defendantRepaymentPlan = caseData.getRespondent1RepaymentPlan();
        return respondentResponseType == BY_SET_DATE ?
            caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid() : defendantRepaymentPlan.finalPaymentBy(
            claimTotalAmount);
    }

    private RepaymentDecisionType calculateDecisionBasedOnAmountAndDisposableIncome(double totalAmount, double disposableIncome) {
        if(totalAmount > disposableIncome) {
            return RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT;
        }
        return RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT;
    }

    private RepaymentDecisionType calculateDecisionBasedOnProposedDate(LocalDate defendantProposedDate, LocalDate claimantProposedDate) {
        if(claimantProposedDate.isAfter(defendantProposedDate)) {
            return RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT;
        }
        return RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT;
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
