package uk.gov.hmcts.reform.civil.service.citizen.repaymentplan;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Respondent1SelfEmploymentLRspec;
import uk.gov.hmcts.reform.civil.model.account.AccountSimple;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec.ONCE_THREE_WEEKS;
import static uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec.ONCE_TWO_WEEKS;
import static uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec.JOB;
import static uk.gov.hmcts.reform.civil.model.account.AccountType.CURRENT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

class IncomeCalculatorTest {

    @Test
    void shouldCalculateTotalSavings_positiveOnlyValues() {
        //Given
        double expectedResult = 1500.55;
        List<Element<AccountSimple>> bankAccountElements = createBankAccountsWithPositiveAndNegativeBalance();
        //When
        double result = new IncomeCalculator().calculateTotalMonthlyIncome(bankAccountElements, null, null);
        //Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldCalculateTotalRegularIncome_positiveValuesOnly() {
        //Given
        double expectedResult = 2384.0;
        List<Element<RecurringIncomeLRspec>> recurringIncomeList = createRecurringIncomeWithPositiveAndNegativeAmount();
        //When
        double result = new IncomeCalculator().calculateTotalMonthlyIncome(null, recurringIncomeList, null);
        //Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldCalculateMonthlyIncomeFromAnnualTurnover() {
        //Given
        double expectedResult = 5583.33;
        Respondent1SelfEmploymentLRspec annualTurnover = createAnnualIncome();
        //When
        double result = new IncomeCalculator().calculateTotalMonthlyIncome(null, null, annualTurnover);
        //Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldCombineTotalSavingsMonthlyIncomeMonthlyTurnoverInIncomeCalculation() {
        //Given
        double expectedResult = 9467.88;
        Respondent1SelfEmploymentLRspec annualTurnover = createAnnualIncome();
        List<Element<AccountSimple>> bankAccountElements = createBankAccountsWithPositiveAndNegativeBalance();
        List<Element<RecurringIncomeLRspec>> recurringIncomeList = createRecurringIncomeWithPositiveAndNegativeAmount();
        //When
        double result = new IncomeCalculator().calculateTotalMonthlyIncome(
            bankAccountElements,
            recurringIncomeList,
            annualTurnover
        );
        //Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldReturnZero_whenNoIncome() {
        //Given
        double expectedResult = 0.00;
        //When
        double result = new IncomeCalculator().calculateTotalMonthlyIncome(null, null, null);
        //Then
        assertThat(result).isEqualTo(expectedResult);

    }

    private static List<Element<AccountSimple>> createBankAccountsWithPositiveAndNegativeBalance() {
        List<Element<AccountSimple>> bankAccountElements =
            wrapElements(
                new AccountSimple()
                    .setBalance(new BigDecimal(1000.55))
                    .setJointAccount(YesOrNo.YES)
                    .setAccountType(CURRENT),
                new AccountSimple()
                    .setBalance(new BigDecimal(-789))
                    .setJointAccount(YesOrNo.NO)
                    .setAccountType(CURRENT),
                new AccountSimple()
                    .setBalance(new BigDecimal(500))
                    .setJointAccount(YesOrNo.YES)
                    .setAccountType(CURRENT)
            );
        return bankAccountElements;
    }

    private static List<Element<RecurringIncomeLRspec>> createRecurringIncomeWithPositiveAndNegativeAmount() {
        return
            wrapElements(
                new RecurringIncomeLRspec()
                    .setAmount(new BigDecimal(10000))
                    .setFrequency(ONCE_TWO_WEEKS)
                    .setType(JOB),
                new RecurringIncomeLRspec()
                    .setAmount(new BigDecimal(-6000))
                    .setFrequency(ONCE_TWO_WEEKS)
                    .setType(JOB),
                new RecurringIncomeLRspec()
                    .setAmount(new BigDecimal(150000))
                    .setFrequency(ONCE_THREE_WEEKS)
                    .setType(JOB)
            );
    }

    private static Respondent1SelfEmploymentLRspec createAnnualIncome() {
        return new Respondent1SelfEmploymentLRspec()
            .setAnnualTurnover(new BigDecimal(6700000))
            ;
    }
}
