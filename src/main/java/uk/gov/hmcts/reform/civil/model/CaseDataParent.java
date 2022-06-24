package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackDisclosureOfDocumentsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingPreferredEmail;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingPreferredTelephone;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingStandardDisposalOrder;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPreferredEmail;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPreferredTelephone;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private final LocalDateTime addLegalRepDeadline;

    @Builder.Default
    private final List<Value<Document>> caseDocuments = new ArrayList<>();
    private final String caseDocument1Name;

    //workaround for showing cases in unassigned case list
    private final String respondent1OrganisationIDCopy;
    private final String respondent2OrganisationIDCopy;

    // sdo fields
    private final JudgementSum drawDirectionsOrder;
    private DisposalHearingJudgesRecital disposalHearingJudgesRecital;
    private DisposalHearingJudgementDeductionValue disposalHearingJudgementDeductionValue;
    private DisposalHearingDisclosureOfDocuments disposalHearingDisclosureOfDocuments;
    private DisposalHearingWitnessOfFact disposalHearingWitnessOfFact;
    private DisposalHearingMedicalEvidence disposalHearingMedicalEvidence;
    private DisposalHearingQuestionsToExperts disposalHearingQuestionsToExperts;
    private DisposalHearingSchedulesOfLoss disposalHearingSchedulesOfLoss;
    private DisposalHearingStandardDisposalOrder disposalHearingStandardDisposalOrder;
    private DisposalHearingFinalDisposalHearing disposalHearingFinalDisposalHearing;
    private DisposalHearingPreferredTelephone disposalHearingPreferredTelephone;
    private DisposalHearingPreferredEmail disposalHearingPreferredEmail;
    private DisposalHearingBundle disposalHearingBundle;
    private DisposalHearingNotes disposalHearingNotes;
    private final DynamicList disposalHearingMethodInPerson;
    private final DynamicList fastTrackMethodInPerson;
    private final YesOrNo drawDirectionsOrderRequired;
    private final YesOrNo drawDirectionsOrderSmallClaims;
    private final ClaimsTrack claimsTrack;
    private final OrderType orderType;
    private FastTrackBuildingDispute fastTrackBuildingDispute;
    private FastTrackClinicalNegligence fastTrackClinicalNegligence;
    private FastTrackCreditHire fastTrackCreditHire;
    private FastTrackHousingDisrepair fastTrackHousingDisrepair;
    private FastTrackPersonalInjury fastTrackPersonalInjury;
    private FastTrackRoadTrafficAccident fastTrackRoadTrafficAccident;
    private FastTrackJudgesRecital fastTrackJudgesRecital;
    private FastTrackJudgementDeductionValue fastTrackJudgementDeductionValue;
    private FastTrackDisclosureOfDocuments fastTrackDisclosureOfDocuments;
    private FastTrackWitnessOfFact fastTrackWitnessOfFact;
    private FastTrackSchedulesOfLoss fastTrackSchedulesOfLoss;
    private FastTrackTrial fastTrackTrial;
    private FastTrackNotes fastTrackNotes;
    private FastTrackPreferredTelephone fastTrackPreferredTelephone;
    private FastTrackPreferredEmail fastTrackPreferredEmail;

    @Builder.Default
    private List<FastTrackDisclosureOfDocumentsToggle> fastTrackDisclosureOfDocumentsToggle = new ArrayList<>();

    // sdo ui flags
    private final YesOrNo setSmallClaimsFlag;
    private final YesOrNo setFastTrackFlag;

    private final LocalDate nextDeadline;
    private final String allPartyNames;
    private final String caseListDisplayDefendantSolicitorReferences;
    private final String unassignedCaseListDisplayOrganisationReferences;
}
