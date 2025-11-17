package uk.gov.hmcts.reform.civil.model.docmosis.dj;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;

import java.util.List;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DefaultJudgmentForm implements MappableObject {

    private String caseNumber;
    private List<Party> applicant;
    private String applicantReference;
    private String respondentReference;
    private Party respondent;
    private String formText;
    private Party claimantLR;
    private String debt;
    private String costs;
    private String totalCost;
    private String respondent1Name;
    private String respondent2Name;
    private String respondent1Ref;
    private String respondent2Ref;
    private Party applicantDetails;
    private String paymentPlan;
    private String payByDate;
    private String repaymentFrequency;
    private String repaymentDate;
    private String paymentStr;
    private String installmentAmount;
    private String welshRepaymentFrequency;
    private String welshPaymentStr;
    private String currentDateInWelsh;
    private String welshPayByDate;
    private String welshRepaymentDate;
}
