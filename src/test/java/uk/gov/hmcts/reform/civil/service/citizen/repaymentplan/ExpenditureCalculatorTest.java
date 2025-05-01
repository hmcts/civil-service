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
        return wrapElements(RecurringExpenseLRspec.builder()
                                .amount(new BigDecimal(20000))
                                .frequency(ONCE_TWO_WEEKS)
                                .type(MORTGAGE)
                                .build());
    }

    private Respondent1DebtLRspec createDebtsWithoutCreditCard() {
        return Respondent1DebtLRspec.builder()
            .debtDetails(createDebts())
            .hasLoanCardDebt(YesOrNo.NO)
            .build();
    }

    private Respondent1DebtLRspec createDebtWithCreditCard() {
        return Respondent1DebtLRspec.builder()
            .debtDetails(createDebts())
            .hasLoanCardDebt(YesOrNo.YES)
            .loanCardDebtDetails(createLoanCardDents())
            .build();
    }

    private List<Element<DebtLRspec>> createDebts() {
        return wrapElements(DebtLRspec.builder()
                                .debtType(DebtTypeLRspec.MORTGAGE)
                                .paymentAmount(new BigDecimal(20000))
                                .paymentFrequency(ONCE_TWO_WEEKS)
                                .build());
    }

    private List<Element<LoanCardDebtLRspec>> createLoanCardDents() {
        return wrapElements(LoanCardDebtLRspec.builder()
                                .monthlyPayment(new BigDecimal(30000))
                                .build());
    }

    private List<Element<Respondent1CourtOrderDetails>> createCourtOrders() {
        return wrapElements(Respondent1CourtOrderDetails.builder()
                                .monthlyInstalmentAmount(new BigDecimal(30000))
                                .build());
    }
}
