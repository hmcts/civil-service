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
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElementsNullSafe;
import static uk.gov.hmcts.reform.civil.utils.MonetaryConversions.penniesToPounds;
import static uk.gov.hmcts.reform.civil.utils.PaymentFrequencyCalculator.calculatePaymentPerMonth;

@Component
public class ExpenditureCalculator {

    public double calculateTotalExpenditure(List<Element<RecurringExpenseLRspec>> recurringExpenseElementList,
                                            Respondent1DebtLRspec respondent1DebtLRspec,
                                            List<Element<Respondent1CourtOrderDetails>> courtOrderDetailsElementList) {
        double calculatedResult = calculateTotalMonthlyExpenses(recurringExpenseElementList)
            + calculateTotalMonthlyDebt(respondent1DebtLRspec)
            + calculateCourtOrders(courtOrderDetailsElementList);
        return BigDecimal.valueOf(calculatedResult).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double calculateTotalMonthlyExpenses(List<Element<RecurringExpenseLRspec>> recurringExpenseElementList) {
        List<RecurringExpenseLRspec> expenses = unwrapElementsNullSafe(recurringExpenseElementList);
        return expenses.stream()
            .mapToDouble(expense -> calculatePaymentPerMonth(
                penniesToPounds(expense.getAmount()).doubleValue(),
                expense.getFrequency()
            ))
            .sum();
    }

    private double calculateCourtOrders(List<Element<Respondent1CourtOrderDetails>> courtOrderDetailsElementList) {
        List<Respondent1CourtOrderDetails> courtOrderDetails = unwrapElementsNullSafe(courtOrderDetailsElementList);
        return !courtOrderDetails.isEmpty() ? courtOrderDetails.stream().map(item -> penniesToPounds(item.getMonthlyInstalmentAmount()))
            .collect(Collectors.summingDouble(BigDecimal::doubleValue)) : 0.0;
    }

    private double calculateTotalMonthlyDebt(Respondent1DebtLRspec respondent1DebtLRspec) {
        if (respondent1DebtLRspec == null) {
            return 0.0;
        }
        if (YesOrNo.YES == respondent1DebtLRspec.getHasLoanCardDebt()) {
            return calculateCreditCardDebts(respondent1DebtLRspec.getLoanCardDebtDetails())
                + calculateDebts(respondent1DebtLRspec.getDebtDetails());
        }
        return calculateDebts(respondent1DebtLRspec.getDebtDetails());
    }

    private double calculateCreditCardDebts(List<Element<LoanCardDebtLRspec>> loanCardDebtDetails) {
        List<LoanCardDebtLRspec> cardDebtList = unwrapElementsNullSafe(loanCardDebtDetails);
        return cardDebtList.stream()
            .map(LoanCardDebtLRspec::getMonthlyPayment)
            .map(debt -> penniesToPounds(debt))
            .collect(Collectors.summingDouble(BigDecimal::doubleValue));
    }

    private double calculateDebts(List<Element<DebtLRspec>> debtDetails) {
        List<DebtLRspec> debts = unwrapElementsNullSafe(debtDetails);
        return debts.stream()
            .mapToDouble(debt -> calculatePaymentPerMonth(
                penniesToPounds(debt.getPaymentAmount()).doubleValue(),
                debt.getPaymentFrequency()
            ))
            .sum();
    }

}
