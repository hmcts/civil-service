package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class Respondent1DebtLRspec {

    private List<Element<DebtLRspec>> debtDetails;
    private YesOrNo hasLoanCardDebt;
    private List<Element<LoanCardDebtLRspec>> loanCardDebtDetails;

}
