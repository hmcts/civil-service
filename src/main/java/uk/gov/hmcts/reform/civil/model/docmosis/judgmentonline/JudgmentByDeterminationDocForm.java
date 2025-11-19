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
@SuppressWarnings("java:S1104") // Suppressing Sonar warnings for Lombok-generated code
public class JudgmentByDeterminationDocForm implements MappableObject {

    // Claim identification
    private String claimReferenceNumber;

    // Party information
    private Party applicant;
    private List<Party> applicants;
    private String applicantReference;

    // Respondent details
    private Party respondent;
    private String respondentReference;
    private String respondent1Name;
    private String respondent2Name;
    private String respondent1Ref;
    private String respondent2Ref;

    // Legal representative
    private Party claimantLR;

    // Financial information
    private String debt;
    private String costs;
    private String totalCost;

    // Payment details
    private String paymentPlan;
    private String payByDate;
    private String repaymentFrequency;
    private String repaymentDate;
    private String paymentStr;
    private String installmentAmount;

    // Form content
    private String formText;

}
