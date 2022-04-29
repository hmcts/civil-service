package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.civil.enums.*;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.math.BigDecimal;
import java.util.List;

@Jacksonized
@SuperBuilder(toBuilder = true)
@Data
public class CaseDataParent implements MappableObject {

    private final SmallClaimMedicalLRspec applicantMPClaimMediationSpecRequired;
    private final YesOrNo applicantMPClaimExpertSpecRequired;
    private final PartnerAndDependentsLRspec respondent1PartnerAndDependent;
    private final PartnerAndDependentsLRspec respondent2PartnerAndDependent;

    private final PaymentUponCourtOrder respondent2CourtOrderPayment;
    private final RepaymentPlanLRspec respondent2RepaymentPlan;
    private final Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployerRespondent2;
    private final Respondent1DebtLRspec specDefendant2Debts;
    private final Respondent1SelfEmploymentLRspec specDefendant2SelfEmploymentDetails;

    private final YesOrNo specAoSRespondent2CorrespondenceAddressRequired;
    private final Address specAoSRespondent2CorrespondenceAddressdetails;
    private final String defenceRouteRequired2;
    private final RespondentResponseTypeSpecPaidStatus respondent2ClaimResponsePaymentAdmissionForSpec;
    private final RespondentResponseTypeSpecPaidStatus claimResponsePaymentAdmissionForSpecGeneric;

    private final YesOrNo showHowToAddTimeLinePage;
    private final YesOrNo fullAdmissionAndFullAmountPaid;
    private final YesOrNo specDefenceFullAdmitted2Required;
    private final YesOrNo partAdmittedByEitherRespondents;
    private final YesOrNo specDefenceAdmitted2Required;
    private final YesOrNo specDefenceAdmittedGeneric;

    private final String specDefenceRouteAmountClaimed2Label;
    private final String specDefenceRouteAdmittedAmountClaimed2Label;
    private final RespondToClaim respondToAdmittedClaim2;
    private final RespondToClaim respondToClaim2;
    /**
     * money amount in pence.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal respondToAdmittedClaimOwingAmount2;
    private final String specDefenceRouteUploadDocument2Label;
    private final String detailsOfWhyDoesYouDisputeTheClaim2;
    private final String specDefenceRouteUploadDocumentLabel3;
    private final ResponseSpecDocument respondent2SpecDefenceResponseDocument;
    private final String specClaimResponseTimelineList2;
    private final List<TimelineOfEvents> specResponseTimelineOfEvents2;
    private final String responseClaimMediationSpecLabelRes2;
    private final YesOrNo responseClaimMediationSpec2Required;
    private final YesOrNo responseClaimExpertSpecRequired2;
    private final YesOrNo responseClaimCourtLocation2Required;
    private final String responseClaimWitnesses2;
    private final String smallClaimHearingInterpreterDescription2;
    private final String additionalInformationForJudge2;
    private final String responseClaimAdmitPartOfClaimWhenToPaySpecLabel2;
    private final RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteRequired2;
    private final RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec2;
    private final String responseClaimAdmitPartOfClaimEmploymentDeclarationLabel2;
    private final YesOrNo defenceAdmitPartEmploymentType2Required;
    private final List<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspec2;
    private final UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec2;
    private final String respondent2CourtOrderPaymentLabel;
    private final String minusBeforeOverdrawnAmountRes2;
    private final String specFinancialDetailsPurpose2;
    private final String specDebtPageLabel2;
    private final String responseClaimAdmitPartOfClaimEmploymentSpecLabel2;
    private final Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer2;
    private final String specSelfEmployedPageLabel2;
    private final String homeOptionsTitleLRspec2;
    private final String respondent2DQIncomeAndExpenseLabel;
    private final String respondent2DQIncomeAndExpenseLabelFullAdmission;
    private final YesOrNo respondent2DQCarerAllowanceCredit;
    private final YesOrNo respondent2DQCarerAllowanceCreditFullAdmission;
    private final String responseToClaimAdmitPartWhyNotPayLRspecLabel2;
    private final String responseToClaimAdmitPartWhyNotPayLRspec2;
    private final YesOrNo neitherCompanyNorOrganisation;
    private final RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteGeneric;
    private final List<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspecGeneric;
    private final RespondentResponseTypeSpec respondentClaimResponseTypeForSpecGeneric;
}
