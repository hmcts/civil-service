package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.EmploymentTypeCheckboxFixedListLRspec;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingStandardDisposalOrder;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final RespondentResponseTypeSpec respondent1ClaimResponseTestForSpec;
    private final RespondentResponseTypeSpec respondent2ClaimResponseTestForSpec;
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
    private DisposalHearingBundle disposalHearingBundle;
    private DisposalHearingNotes disposalHearingNotes;
    private final DisposalHearingMethod disposalHearingMethod;
    private final DisposalHearingMethodTelephoneHearing disposalHearingMethodTelephoneHearing;
    private final DisposalHearingMethodVideoConferenceHearing disposalHearingMethodVideoConferenceHearing;
    private final List<Element<DisposalHearingAddNewDirections>> disposalHearingAddNewDirections;

    private final DynamicList disposalHearingMethodInPerson;
    private final DynamicList fastTrackMethodInPerson;
    private final DynamicList smallClaimsMethodInPerson;
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
    private final List<FastTrack> fastClaims;
    private final FastTrackMethod fastTrackMethod;
    private final FastTrackMethodTelephoneHearing fastTrackMethodTelephoneHearing;
    private final FastTrackMethodVideoConferenceHearing fastTrackMethodVideoConferenceHearing;
    private final List<Element<FastTrackAddNewDirections>> fastTrackAddNewDirections;
    private SmallClaimsCreditHire smallClaimsCreditHire;
    private SmallClaimsRoadTrafficAccident smallClaimsRoadTrafficAccident;
    private SmallClaimsDocuments smallClaimsDocuments;
    private SmallClaimsHearing smallClaimsHearing;
    private SmallClaimsJudgementDeductionValue smallClaimsJudgementDeductionValue;
    private SmallClaimsJudgesRecital smallClaimsJudgesRecital;
    private SmallClaimsNotes smallClaimsNotes;
    private SmallClaimsWitnessStatement smallClaimsWitnessStatement;
    private ReasonNotSuitableSDO reasonNotSuitableSDO;
    private final List<SmallTrack> smallClaims;
    private final SmallClaimsMethod smallClaimsMethod;
    private final SmallClaimsMethodTelephoneHearing smallClaimsMethodTelephoneHearing;
    private final SmallClaimsMethodVideoConferenceHearing smallClaimsMethodVideoConferenceHearing;
    private final List<Element<SmallClaimsAddNewDirections>> smallClaimsAddNewDirections;
    private List<OrderDetailsPagesSectionsToggle> fastTrackAltDisputeResolutionToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackVariationOfDirectionsToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackSettlementToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackDisclosureOfDocumentsToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackWitnessOfFactToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackSchedulesOfLossToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackCostsToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackTrialToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackMethodToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingDisclosureOfDocumentsToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingWitnessOfFactToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingMedicalEvidenceToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingQuestionsToExpertsToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingSchedulesOfLossToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingFinalDisposalHearingToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingMethodToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingBundleToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingClaimSettlingToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingCostsToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingApplicationsOrderToggle;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsHearingToggle;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsMethodToggle;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsDocumentsToggle;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsWitnessStatementToggle;

    private Document sdoOrderDocument;

    // sdo ui flags
    private final YesOrNo setSmallClaimsFlag;
    private final YesOrNo setFastTrackFlag;

    private final LocalDate nextDeadline;
    private final String allPartyNames;
    private final String caseListDisplayDefendantSolicitorReferences;
    private final String unassignedCaseListDisplayOrganisationReferences;
    private final YesOrNo specAoSRespondent2CorrespondenceAddressRequired;
    private final Address specAoSRespondent2CorrespondenceAddressdetails;
    private final String defenceRouteRequired2;

    private final YesOrNo showHowToAddTimeLinePage;
    private final YesOrNo fullAdmissionAndFullAmountPaid;
    private final YesOrNo specDefenceFullAdmitted2Required;
    private final YesOrNo partAdmittedByEitherRespondents;
    private final YesOrNo specDefenceAdmitted2Required;

    private final String specDefenceRouteAdmittedAmountClaimed2Label;
    private final RespondToClaim respondToAdmittedClaim2;
    private final RespondToClaim respondToClaim2;
    /**
     * money amount in pence.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal respondToAdmittedClaimOwingAmount2;
    private final String detailsOfWhyDoesYouDisputeTheClaim2;
    private final String specDefenceRouteUploadDocumentLabel3;
    private final ResponseSpecDocument respondent2SpecDefenceResponseDocument;
    private final TimelineUploadTypeSpec specClaimResponseTimelineList2;
    private final List<TimelineOfEvents> specResponseTimelineOfEvents2;
    private final String responseClaimMediationSpecLabelRes2;
    private final YesOrNo responseClaimMediationSpec2Required;
    private final YesOrNo responseClaimExpertSpecRequired2;
    private final YesOrNo responseClaimCourtLocation2Required;
    private final String responseClaimWitnesses2;
    private final String smallClaimHearingInterpreterDescription2;
    private final String additionalInformationForJudge2;
    private final RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteRequired2;
    private final RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec2;
    private final YesOrNo defenceAdmitPartEmploymentType2Required;
    private final List<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspec2;
    private final UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec2;
    private final Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer2;
    private final YesOrNo respondent2DQCarerAllowanceCredit;
    /**
     * This field is not used.
     *
     * @deprecated this field is not used and it was in a screen no longer presented to the user.
     *     It is kept here to devote a single jira to its removal, to ensure said removal won't cause
     *     any problem when bringing info from db.
     */
    @Deprecated
    private final YesOrNo respondent2DQCarerAllowanceCreditFullAdmission;
    private final String responseToClaimAdmitPartWhyNotPayLRspec2;
    private final YesOrNo neitherCompanyNorOrganisation;
    private final RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteGeneric;
    private final List<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspecGeneric;
    @Builder.Default
    private final Set<DefendantResponseShowTag> showConditionFlags = new HashSet<>();

    /**
     * money amount in pounds. Waiting here until we address the issue with CaseData having
     * too many fields
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal respondToAdmittedClaimOwingAmountPounds2;

    @JsonProperty("CaseAccessCategory")
    private final CaseCategory caseAccessCategory;

    private final ChangeOrganisationRequest changeOrganisationRequestField;
    private final List<Element<ChangeOfRepresentation>> changeOfRepresentation;

    /**
     * Adding for PiP to citizen UI.
     */
    private final DefendantPinToPostLRspec respondent1PinToPostLRspec;

    private final ScheduledHearing nextHearingDetails;

    private final String respondent1EmailAddress;

}
