package uk.gov.hmcts.reform.civil.service.citizen.repaymentplan;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Respondent1SelfEmploymentLRspec;
import uk.gov.hmcts.reform.civil.model.account.AccountSimple;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;
import uk.gov.hmcts.reform.civil.utils.PaymentFrequencyCalculator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElementsNullSafe;
import static uk.gov.hmcts.reform.civil.utils.MonetaryConversions.penniesToPounds;

@Component
public class IncomeCalculator {

    public double calculateTotalMonthlyIncome(List<Element<AccountSimple>> bankAccountElements,
                                           List<Element<RecurringIncomeLRspec>> recuringIncomeElements,
                                           Respondent1SelfEmploymentLRspec specDefendant1SelfEmploymentDetails) {
        return calculateRegularIncome(recuringIncomeElements)
            + calculateTotalSavings(bankAccountElements)
            + calculateMonthlyIncomeFromAnnualTurnover(specDefendant1SelfEmploymentDetails);
    }

    public double calculateTotalSavings(List<Element<AccountSimple>> bankAccountElements) {
        List<AccountSimple> bankAccounts = unwrapElementsNullSafe(bankAccountElements);
        return bankAccounts.stream().filter(item -> item.getBalance().compareTo(BigDecimal.ZERO) > 0)
            .map(AccountSimple::getBalance).collect(Collectors.summingDouble(BigDecimal::doubleValue));
    }

    public double calculateRegularIncome(List<Element<RecurringIncomeLRspec>> recurringIncomeElements) {
        List<RecurringIncomeLRspec> recurringIncomes = unwrapElementsNullSafe(recurringIncomeElements);
        return recurringIncomes.stream().filter(income -> income.getAmount().compareTo(BigDecimal.ZERO) > 0)
            .mapToDouble(income -> calculateIncomePerMonth(income)).sum();
    }

    public double calculateMonthlyIncomeFromAnnualTurnover (Respondent1SelfEmploymentLRspec specDefendant1SelfEmploymentDetails) {
      return Optional.ofNullable(specDefendant1SelfEmploymentDetails)
            .map(selfEmploymentDetails -> selfEmploymentDetails.getAnnualTurnover().doubleValue()/12)
            .orElse(0.0);
    }

    private double calculateIncomePerMonth(RecurringIncomeLRspec income) {
        double incomeAmount = penniesToPounds(income.getAmount()).doubleValue();
        return PaymentFrequencyCalculator.calculatePaymentPerMonth(incomeAmount, income.getFrequency());
    }
}
