package uk.gov.hmcts.reform.civil.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Jacksonized
@SuperBuilder(toBuilder = true)
@Data
public class CaseDataParent implements MappableObject {

    private final SmallClaimMedicalLRspec applicantMPClaimMediationSpecRequired;
    private final YesOrNo applicantMPClaimExpertSpecRequired;
    private final String respondent1PartnerAndDependent; //marked as string (Kenneth will change them to object type)
    private final String respondent2PartnerAndDependent; //marked as string (Kenneth will change them to object type)

    private final PaymentUponCourtOrder respondent2CourtOrderPayment;
    private final RepaymentPlanLRspec respondent2RepaymentPlan;
    private final Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployerRespondent2;
    private final Respondent1DebtLRspec specDefendant2Debts;
    private final Respondent1SelfEmploymentLRspec specDefendant2SelfEmploymentDetails;

}
