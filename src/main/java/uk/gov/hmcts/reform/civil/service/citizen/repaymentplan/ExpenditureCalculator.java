package uk.gov.hmcts.reform.civil.service.citizen.repaymentplan;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.DebtLRspec;
import uk.gov.hmcts.reform.civil.model.LoanCardDebtLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1DebtLRspec;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElementsNullSafe;
import static uk.gov.hmcts.reform.civil.utils.PaymentFrequencyCalculator.calculatePaymentPerMonth;

@Component
public class ExpenditureCalculator {



    private int calculateTotalMonthlyDebt(Respondent1DebtLRspec respondent1DebtLRspec) {
       if(respondent1DebtLRspec.getHasLoanCardDebt() == YesOrNo.YES) {
           return calculateCreditCardDebts(respondent1DebtLRspec.getLoanCardDebtDetails())
               + calculateDepts(respondent1DebtLRspec.getDebtDetails());
       }
       return calculateDepts(respondent1DebtLRspec.getDebtDetails());
    }

    private int calculateCreditCardDebts(List<Element<LoanCardDebtLRspec>> loanCardDebtDetails) {
        List<LoanCardDebtLRspec> cardDebtList = unwrapElementsNullSafe(loanCardDebtDetails);
        return cardDebtList.stream()
            .map(LoanCardDebtLRspec::getMonthlyPayment)
            .collect(Collectors.summingInt(BigDecimal::intValue));
    }

    private int calculateDepts(List<Element<DebtLRspec>> debtDetails) {
        List<DebtLRspec> debts = unwrapElementsNullSafe(debtDetails);
        return debts.stream()
            .map(debt -> calculatePaymentPerMonth(debt.getPaymentAmount().intValue(), debt.getPaymentFrequency()))
            .collect(Collectors.summingInt(Integer::intValue));
    }

}
