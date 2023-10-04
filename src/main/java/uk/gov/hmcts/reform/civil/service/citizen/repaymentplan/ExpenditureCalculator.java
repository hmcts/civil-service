package uk.gov.hmcts.reform.civil.service.citizen.repaymentplan;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.DebtLRspec;
import uk.gov.hmcts.reform.civil.model.LoanCardDebtLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1CourtOrderDetails;
import uk.gov.hmcts.reform.civil.model.Respondent1DebtLRspec;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.RecurringExpenseLRspec;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElementsNullSafe;
import static uk.gov.hmcts.reform.civil.utils.PaymentFrequencyCalculator.calculatePaymentPerMonth;

@Component
public class ExpenditureCalculator {

    public double calculateTotalExpenditure(List<Element<RecurringExpenseLRspec>> recurringExpenseElementList,
                                         Respondent1DebtLRspec respondent1DebtLRspec,
                                            List<Element<Respondent1CourtOrderDetails>> courtOrderDetailsElementList) {
        return calculateTotalMonthlyExpenses(recurringExpenseElementList)
            + calculateTotalMonthlyDebt(respondent1DebtLRspec)
            + calculateCourtOrders(courtOrderDetailsElementList);
    }

    private double calculateTotalMonthlyExpenses(List<Element<RecurringExpenseLRspec>> recurringExpenseElementList) {
        List<RecurringExpenseLRspec> expenses = unwrapElementsNullSafe(recurringExpenseElementList);
        return expenses.stream()
            .mapToDouble(expense -> calculatePaymentPerMonth(expense.getAmount().intValue(), expense.getFrequency()))
            .sum();
    }

    private double calculateCourtOrders(List<Element<Respondent1CourtOrderDetails>> courtOrderDetailsElementList) {
        List<Respondent1CourtOrderDetails> courtOrderDetails = unwrapElementsNullSafe(courtOrderDetailsElementList);
        return courtOrderDetails.size() > 0 ? courtOrderDetails.stream().map(Respondent1CourtOrderDetails::getMonthlyInstalmentAmount)
            .collect(Collectors.summingDouble(BigDecimal::doubleValue)) : 0.0;
    }

    private double calculateTotalMonthlyDebt(Respondent1DebtLRspec respondent1DebtLRspec) {
       if(respondent1DebtLRspec.getHasLoanCardDebt() == YesOrNo.YES) {
           return calculateCreditCardDebts(respondent1DebtLRspec.getLoanCardDebtDetails())
               + calculateDepts(respondent1DebtLRspec.getDebtDetails());
       }
       return calculateDepts(respondent1DebtLRspec.getDebtDetails());
    }

    private double calculateCreditCardDebts(List<Element<LoanCardDebtLRspec>> loanCardDebtDetails) {
        List<LoanCardDebtLRspec> cardDebtList = unwrapElementsNullSafe(loanCardDebtDetails);
        return cardDebtList.stream()
            .map(LoanCardDebtLRspec::getMonthlyPayment)
            .collect(Collectors.summingDouble(BigDecimal::doubleValue));
    }

    private double calculateDepts(List<Element<DebtLRspec>> debtDetails) {
        List<DebtLRspec> debts = unwrapElementsNullSafe(debtDetails);
        return debts.stream()
            .mapToDouble(debt -> calculatePaymentPerMonth(debt.getPaymentAmount().intValue(), debt.getPaymentFrequency()))
            .sum();
    }

}
