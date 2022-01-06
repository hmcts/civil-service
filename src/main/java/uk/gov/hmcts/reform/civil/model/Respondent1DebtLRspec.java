package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class Respondent1DebtLRspec {

    private final List<Element<DebtLRspec>> debtDetails;
    private final YesOrNo hasLoanCardDebt;
    private final List<Element<LoanCardDebtLRspec>> loanCardDebtDetails;

}
