package uk.gov.hmcts.reform.civil.model.docmosis.dj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class DefaultJudgmentForm implements MappableObject {

    private final String caseNumber;
    private final List<Party> applicant;
    private final String applicantReference;
    private final String respondentReference;
    private final Party respondent;
    private final String formText;
    private final Party claimantLR;
    private final String debt;
    private final String costs;
    private final String totalCost;
    private final String respondent1Name;
    private final String respondent2Name;
    private final String respondent1Ref;
    private final String respondent2Ref;
    private final Party applicantDetails;
    private String paymentPlan;
    private final String payByDate;
    private final String repaymentFrequency;
    private final String repaymentDate;
    private final String paymentStr;
    private final String installmentAmount;

    @Override
    public String toString() {
        return "DefaultJudgmentForm{" +
            "caseNumber='" + caseNumber + '\'' +
            ", applicant=" + applicant +
            ", applicantReference='" + applicantReference + '\'' +
            ", respondentReference='" + respondentReference + '\'' +
            ", respondent=" + respondent +
            ", formText='" + formText + '\'' +
            ", claimantLR=" + claimantLR +
            ", debt='" + debt + '\'' +
            ", costs='" + costs + '\'' +
            ", totalCost='" + totalCost + '\'' +
            ", respondent1Name='" + respondent1Name + '\'' +
            ", respondent2Name='" + respondent2Name + '\'' +
            ", respondent1Ref='" + respondent1Ref + '\'' +
            ", respondent2Ref='" + respondent2Ref + '\'' +
            ", applicantDetails=" + applicantDetails +
            ", paymentPlan='" + paymentPlan + '\'' +
            ", payByDate='" + payByDate + '\'' +
            ", repaymentFrequency='" + repaymentFrequency + '\'' +
            ", repaymentDate='" + repaymentDate + '\'' +
            ", paymentStr='" + paymentStr + '\'' +
            ", installmentAmount='" + installmentAmount + '\'' +
            '}';
    }
}
