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
@SuppressWarnings({"java:S1104", "java:S1170"}) // Suppressing Sonar warnings for Lombok-generated fields
public class DefaultJudgmentForm implements MappableObject {

    /** Case reference number. */
    private String caseNumber;

    /** Applicant party information. */
    private List<Party> applicant;
    private Party applicantDetails;
    private String applicantReference;

    /** Form content and text. */
    private String formText;

    /** Claimant legal representative. */
    private Party claimantLR;

    /** Respondent information. */
    private Party respondent;
    private String respondentReference;
    private String respondent1Name;
    private String respondent2Name;
    private String respondent1Ref;
    private String respondent2Ref;

    /** Financial amounts. */
    private String debt;
    private String costs;
    private String totalCost;

    /** Payment plan details. */
    private String paymentPlan;
    private String payByDate;
    private String repaymentFrequency;
    private String repaymentDate;
    private String paymentStr;
    private String installmentAmount;

    /** Welsh language translations. */
    private String welshRepaymentFrequency;
    private String welshPaymentStr;
    private String currentDateInWelsh;
    private String welshPayByDate;
    private String welshRepaymentDate;
}
