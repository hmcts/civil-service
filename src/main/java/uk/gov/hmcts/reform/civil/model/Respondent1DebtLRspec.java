package uk.gov.hmcts.reform.civil.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Respondent1DebtLRspec {

    private List<Element<DebtLRspec>> debtDetails;
    private YesOrNo hasLoanCardDebt;
    private List<Element<LoanCardDebtLRspec>> loanCardDebtDetails;

}
