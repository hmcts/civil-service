package uk.gov.hmcts.reform.civil.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "Debts", generate = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Respondent1DebtLRspec {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Collection, typeParameterOverride = "Debt")
    private List<Element<DebtLRspec>> debtDetails;
    @CCD(ignore = true)
    private YesOrNo hasLoanCardDebt;
    @CCD(ignore = true)
    private List<Element<LoanCardDebtLRspec>> loanCardDebtDetails;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "## Add details of any debts owed by your client.\n", searchable = false, typeOverride = FieldType.Label)
  private String debtAddLabel;
  // ==== end synthesised definition-only fields ====
}
