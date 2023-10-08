package uk.gov.hmcts.reform.civil.service.citizen.repaymentplan;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Respondent1SelfEmploymentLRspec;
import uk.gov.hmcts.reform.civil.model.account.AccountSimple;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElementsNullSafe;
import static uk.gov.hmcts.reform.civil.utils.MonetaryConversions.penniesToPounds;
import static uk.gov.hmcts.reform.civil.utils.PaymentFrequencyCalculator.calculatePaymentPerMonth;

@Component
public class IncomeCalculator {

    public double calculateTotalMonthlyIncome(List<Element<AccountSimple>> bankAccountElements,
                                           List<Element<RecurringIncomeLRspec>> recurringIncomeElements,
                                           Respondent1SelfEmploymentLRspec specDefendant1SelfEmploymentDetails) {
        double result = calculateRegularIncome(recurringIncomeElements)
            + calculateTotalSavings(bankAccountElements)
            + calculateMonthlyIncomeFromAnnualTurnover(specDefendant1SelfEmploymentDetails);
        return new BigDecimal(result).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double calculateTotalSavings(List<Element<AccountSimple>> bankAccountElements) {
        List<AccountSimple> bankAccounts = unwrapElementsNullSafe(bankAccountElements);
        return bankAccounts.stream().filter(item -> item.getBalance().compareTo(BigDecimal.ZERO) > 0)
            .map(AccountSimple::getBalance).collect(Collectors.summingDouble(BigDecimal::doubleValue));
    }

    private double calculateRegularIncome(List<Element<RecurringIncomeLRspec>> recurringIncomeElements) {
        List<RecurringIncomeLRspec> recurringIncomes = unwrapElementsNullSafe(recurringIncomeElements);
        return recurringIncomes.stream().filter(income -> income.getAmount().compareTo(BigDecimal.ZERO) > 0)
            .mapToDouble(income -> calculateIncomePerMonth(income)).sum();
    }

    private double calculateMonthlyIncomeFromAnnualTurnover (Respondent1SelfEmploymentLRspec specDefendant1SelfEmploymentDetails) {
      return Optional.ofNullable(specDefendant1SelfEmploymentDetails)
            .map(selfEmploymentDetails -> new BigDecimal(penniesToPounds(selfEmploymentDetails.getAnnualTurnover()).doubleValue()/12 )
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue())
            .orElse(0.0);
    }

    private double calculateIncomePerMonth(RecurringIncomeLRspec income) {
        double incomeAmount = penniesToPounds(income.getAmount()).doubleValue();
        double paymentPerMonth = calculatePaymentPerMonth(incomeAmount, income.getFrequency());
        return paymentPerMonth;
    }
}
