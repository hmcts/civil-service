package uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline;

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
public class JudgmentByDeterminationDocForm implements MappableObject {

    private final String claimReferenceNumber;
    private final Party applicant;
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
    private final List<Party> applicants;
    private String paymentPlan;
    private final String payByDate;
    private final String repaymentFrequency;
    private final String repaymentDate;
    private final String paymentStr;
    private final String installmentAmount;

}
