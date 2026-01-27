package uk.gov.hmcts.reform.civil.model.docmosis.dj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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

    public DefaultJudgmentForm copy() {
        return new DefaultJudgmentForm()
            .setCaseNumber(this.caseNumber)
            .setApplicant(this.applicant)
            .setApplicantDetails(this.applicantDetails)
            .setApplicantReference(this.applicantReference)
            .setFormText(this.formText)
            .setClaimantLR(this.claimantLR)
            .setRespondent(this.respondent)
            .setRespondentReference(this.respondentReference)
            .setRespondent1Name(this.respondent1Name)
            .setRespondent2Name(this.respondent2Name)
            .setRespondent1Ref(this.respondent1Ref)
            .setRespondent2Ref(this.respondent2Ref)
            .setDebt(this.debt)
            .setCosts(this.costs)
            .setTotalCost(this.totalCost)
            .setPaymentPlan(this.paymentPlan)
            .setPayByDate(this.payByDate)
            .setRepaymentFrequency(this.repaymentFrequency)
            .setRepaymentDate(this.repaymentDate)
            .setPaymentStr(this.paymentStr)
            .setInstallmentAmount(this.installmentAmount)
            .setWelshRepaymentFrequency(this.welshRepaymentFrequency)
            .setWelshPaymentStr(this.welshPaymentStr)
            .setCurrentDateInWelsh(this.currentDateInWelsh)
            .setWelshPayByDate(this.welshPayByDate)
            .setWelshRepaymentDate(this.welshRepaymentDate);
    }
}
