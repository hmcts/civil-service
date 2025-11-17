package uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class JudgmentByDeterminationDocForm implements MappableObject {

    private String claimReferenceNumber;
    private Party applicant;
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
    private List<Party> applicants;
    private String paymentPlan;
    private String payByDate;
    private String repaymentFrequency;
    private String repaymentDate;
    private String paymentStr;
    private String installmentAmount;

}
