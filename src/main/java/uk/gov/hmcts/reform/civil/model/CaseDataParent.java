package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.dq.Witness;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Jacksonized
@SuperBuilder(toBuilder = true)
@Data
public class CaseDataParent implements MappableObject {

    private final SmallClaimMedicalLRspec applicantMPClaimMediationSpecRequired;
    private final YesOrNo applicantMPClaimExpertSpecRequired;
    private final PartnerAndDependentsLRspec respondent1PartnerAndDependent;
    private final PartnerAndDependentsLRspec respondent2PartnerAndDependent;
    private final YesOrNo applicant1ProceedWithClaimSpec2v1;

    private final PaymentUponCourtOrder respondent2CourtOrderPayment;
    private final RepaymentPlanLRspec respondent2RepaymentPlan;
    private final Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployerRespondent2;
    private final Respondent1DebtLRspec specDefendant2Debts;
    private final Respondent1SelfEmploymentLRspec specDefendant2SelfEmploymentDetails;
    private final RespondentResponseTypeSpec respondentClaimResponseTypeForSpecGeneric;
    private final YesOrNo respondent1CourtOrderPaymentOption;
    private final List<Element<Respondent1CourtOrderDetails>> respondent1CourtOrderDetails;
    private final YesOrNo respondent2CourtOrderPaymentOption;
    private final List<Element<Respondent2CourtOrderDetails>> respondent2CourtOrderDetails;
    private final YesOrNo respondent1LoanCreditOption;
    private final List<Element<Respondent1LoanCreditDetails>> respondent1LoanCreditDetails;
    private final YesOrNo respondent2LoanCreditOption;
    private final List<Element<Respondent2LoanCreditDetails>> respondent2LoanCreditDetails;
    // for default judgment specified tab
    private final DJPaymentTypeSelection paymentTypeSelection;
    private final RepaymentFrequencyDJ repaymentFrequency;
    // for default judgment specified tab
    // for witness
    private final YesOrNo respondent1DQWitnessesRequiredSpec;
    private final List<Element<Witness>> respondent1DQWitnessesDetailsSpec;

    @Builder.Default
    private final List<Value<Document>> caseDocuments = new ArrayList<>();
    private final String caseDocument1Name;

    private final LocalDate nextDeadline;
    private final String allPartyNames;
    private final String caseListDisplayDefendantSolicitorReferences;
    private final String unassignedCaseListDisplayOrganisationReferences;
}
