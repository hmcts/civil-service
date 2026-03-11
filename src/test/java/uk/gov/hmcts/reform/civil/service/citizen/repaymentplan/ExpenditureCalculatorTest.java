package uk.gov.hmcts.reform.civil.service.citizen.repaymentplan;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.DebtTypeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.DebtLRspec;
import uk.gov.hmcts.reform.civil.model.LoanCardDebtLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1CourtOrderDetails;
import uk.gov.hmcts.reform.civil.model.Respondent1DebtLRspec;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.RecurringExpenseLRspec;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec.ONCE_TWO_WEEKS;
import static uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec.MORTGAGE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

class ExpenditureCalculatorTest {

    @Test
    void shouldCalculateMonthlyExpenses() {
        //Given
        double expectedResult = 433.0;
        List<Element<RecurringExpenseLRspec>> expenses = createExpensesList();
        //When
        double result = new ExpenditureCalculator()
            .calculateTotalExpenditure(expenses, null, null);
        //Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldCalculateDebtsWithoutCreditCards() {
        //Given
        double expectedResult = 433.0;
        Respondent1DebtLRspec debtsWithoutCreditCardDebts = createDebtsWithoutCreditCard();
        //When
        double result = new ExpenditureCalculator()
            .calculateTotalExpenditure(null, debtsWithoutCreditCardDebts, null);
        //Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldCalculateDebtsWitCreditCards() {
        //Given
        double expectedResult = 733.0;
        Respondent1DebtLRspec debtsWithCreditCardDebts = createDebtWithCreditCard();
        //When
        double result = new ExpenditureCalculator()
            .calculateTotalExpenditure(null, debtsWithCreditCardDebts, null);
        //Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldCalculateCourtOrders() {
        //Given
        double expectedResult = 300.0;
        List<Element<Respondent1CourtOrderDetails>> courtOrders = createCourtOrders();
        //When
        double result = new ExpenditureCalculator()
            .calculateTotalExpenditure(null, null, courtOrders);
        //Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldCalculateTotalExpenditure() {
        //Given
        double expectedResult = 1466.0;
        List<Element<RecurringExpenseLRspec>> expenses = createExpensesList();
        Respondent1DebtLRspec debtsWithCreditCardDebts = createDebtWithCreditCard();
        List<Element<Respondent1CourtOrderDetails>> courtOrders = createCourtOrders();
        //When
        double result = new ExpenditureCalculator()
            .calculateTotalExpenditure(expenses, debtsWithCreditCardDebts, courtOrders);
        //Then
        assertThat(result).isEqualTo(expectedResult);
    }

    private List<Element<RecurringExpenseLRspec>> createExpensesList() {
        return wrapElements(new RecurringExpenseLRspec()
                                .setAmount(new BigDecimal(20000))
                                .setFrequency(ONCE_TWO_WEEKS)
                                .setType(MORTGAGE));
    }

    private Respondent1DebtLRspec createDebtsWithoutCreditCard() {
        return new Respondent1DebtLRspec()
            .setDebtDetails(createDebts())
            .setHasLoanCardDebt(YesOrNo.NO)
            ;
    }

    private Respondent1DebtLRspec createDebtWithCreditCard() {
        return new Respondent1DebtLRspec()
            .setDebtDetails(createDebts())
            .setHasLoanCardDebt(YesOrNo.YES)
            .setLoanCardDebtDetails(createLoanCardDents())
            ;
    }

    private List<Element<DebtLRspec>> createDebts() {
        return wrapElements(new DebtLRspec()
                                .setDebtType(DebtTypeLRspec.MORTGAGE)
                                .setPaymentAmount(new BigDecimal(20000))
                                .setPaymentFrequency(ONCE_TWO_WEEKS));
    }

    private List<Element<LoanCardDebtLRspec>> createLoanCardDents() {
        return wrapElements(new LoanCardDebtLRspec().setMonthlyPayment(new BigDecimal(30000))
                                );
    }

    private List<Element<Respondent1CourtOrderDetails>> createCourtOrders() {
        return wrapElements(new Respondent1CourtOrderDetails()
                                .setMonthlyInstalmentAmount(new BigDecimal(30000))
                                );
    }
}
