package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.EmploymentTypeCheckboxFixedListLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadDisclosure;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadExpert;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness;
import uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.dj.HearingMethodTelephoneHearingDJ;
import uk.gov.hmcts.reform.civil.enums.dj.HearingMethodVideoConferenceDJ;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.enums.finalorders.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle;
import uk.gov.hmcts.reform.civil.enums.finalorders.HearingLengthFinalOrderList;
import uk.gov.hmcts.reform.civil.enums.finalorders.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
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
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.caseprogression.HearingOtherComments;
import uk.gov.hmcts.reform.civil.model.caseprogression.RevisedHearingRequirements;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialEmployersLiability;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingNotes;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderCostDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderReasons;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderAppeal;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRecitalsRecorded;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRepresentation;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMade;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetailsOrderWithoutNotice;
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingStandardDisposalOrder;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalOrderWithoutHearing;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackOrderWithoutJudgement;
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
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingHearingNotesDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialOrderMadeWithoutHearingDJ;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.math.BigDecimal.ZERO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

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
    private final Witnesses applicant1DQWitnessesSmallClaim;
    private final Witnesses respondent1DQWitnessesSmallClaim;
    private final Witnesses respondent2DQWitnessesSmallClaim;

    @Deprecated
    private final LocalDateTime addLegalRepDeadline;

    @Builder.Default
    private final List<Value<Document>> caseDocuments = new ArrayList<>();
    private final String caseDocument1Name;
    //TrialReadiness
    private final String hearingDurationTextApplicant;
    private final String hearingDurationTextRespondent1;
    private final String hearingDurationTextRespondent2;
    //workaround for showing cases in unassigned case list
    private final String respondent1OrganisationIDCopy;
    private final String respondent2OrganisationIDCopy;

    @JsonUnwrapped
    private final Mediation mediation;

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
    private DisposalHearingHearingTime disposalHearingHearingTime;
    private DisposalHearingBundle disposalHearingBundle;
    private DisposalHearingBundleDJ disposalHearingBundleDJ;
    private DisposalHearingNotes disposalHearingNotes;
    private String disposalHearingHearingNotes;
    private DisposalOrderWithoutHearing disposalOrderWithoutHearing;
    private final DisposalHearingMethod disposalHearingMethod;
    private final DisposalHearingMethodTelephoneHearing disposalHearingMethodTelephoneHearing;
    private final DisposalHearingMethodVideoConferenceHearing disposalHearingMethodVideoConferenceHearing;
    private final List<Element<DisposalHearingAddNewDirections>> disposalHearingAddNewDirections;

    private final DynamicList disposalHearingMethodInPerson;
    private final DynamicList fastTrackMethodInPerson;
    private final DynamicList hearingMethodValuesFastTrack;
    private final DynamicList hearingMethodValuesDisposalHearing;
    private final DynamicList hearingMethodValuesSmallClaims;
    private final DynamicList smallClaimsMethodInPerson;
    private final DynamicList hearingMethod;
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
    private FastTrackHearingTime fastTrackHearingTime;
    private FastTrackNotes fastTrackNotes;
    private FastTrackHearingNotes fastTrackHearingNotes;
    private FastTrackOrderWithoutJudgement fastTrackOrderWithoutJudgement;
    private final List<FastTrack> fastClaims;
    private final List<FastTrack> trialAdditionalDirectionsForFastTrack;
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
    private SDOHearingNotes sdoHearingNotes;
    private ReasonNotSuitableSDO reasonNotSuitableSDO;
    private final List<SmallTrack> smallClaims;
    private final List<SmallTrack> drawDirectionsOrderSmallClaimsAdditionalDirections;
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
    private List<OrderDetailsPagesSectionsToggle> smallClaimsHearingToggle;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsMethodToggle;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsDocumentsToggle;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsWitnessStatementToggle;
    private List<DateToShowToggle> smallClaimsHearingDateToToggle;
    private List<DateToShowToggle> fastTrackTrialDateToToggle;

    private CaseDocument sdoOrderDocument;

    // sdo ui flags
    private final YesOrNo setSmallClaimsFlag;
    private final YesOrNo setFastTrackFlag;
    private final String eventDescriptionRTJ;
    private final String additionalInformationRTJ;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal partAdmitPaidValuePounds;

    @JsonProperty("CaseAccessCategory")
    private final CaseCategory caseAccessCategory;

    private final ChangeOrganisationRequest changeOrganisationRequestField;
    private final ChangeOfRepresentation changeOfRepresentation;

    /**
     * Adding for PiP to citizen UI.
     */
    private final DefendantPinToPostLRspec respondent1PinToPostLRspec;

    private final ScheduledHearing nextHearingDetails;

    private final String respondent1EmailAddress;
    private final YesOrNo applicant1Represented;

    /**
     * Adding for LR ITP Update.
     */
    private final ResponseOneVOneShowTag showResponseOneVOneFlag;
    private final YesOrNo applicant1AcceptAdmitAmountPaidSpec;
    private final YesOrNo applicant1PartAdmitConfirmAmountPaidSpec;
    private final YesOrNo applicant1PartAdmitIntentionToSettleClaimSpec;
    private final YesOrNo applicant1AcceptFullAdmitPaymentPlanSpec;
    private final YesOrNo applicant1AcceptPartAdmitPaymentPlanSpec;
    private final CaseDocument respondent1ClaimResponseDocumentSpec;
    private final CaseDocument respondent2ClaimResponseDocumentSpec;
    private final String respondent1PaymentDateToStringSpec;
    private final PaymentBySetDate applicant1RequestedPaymentDateForDefendantSpec;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal applicant1SuggestInstalmentsPaymentAmountForDefendantSpec;
    private final PaymentFrequencyClaimantResponseLRspec applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec;
    private final LocalDate applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec;
    private final String currentDateboxDefendantSpec;
    @JsonUnwrapped
    private final CCJPaymentDetails ccjPaymentDetails;
    private final PaymentType applicant1RepaymentOptionForDefendantSpec;

    @JsonUnwrapped
    private final CaseDataLiP caseDataLiP;
    private final YesOrNo applicantDefenceResponseDocumentAndDQFlag;
    private final String migrationId;

    @JsonIgnore
    public boolean isApplicantNotRepresented() {
        return this.applicant1Represented == YesOrNo.NO;
    }

    /**
     * Adding for Certificate of Service.
     */
    private final CertificateOfService cosNotifyClaimDetails1;
    private final CertificateOfService cosNotifyClaimDetails2;
    private final YesOrNo defendant1LIPAtClaimIssued;
    private final YesOrNo defendant2LIPAtClaimIssued;
    private final CertificateOfService cosNotifyClaimDefendant1;
    private final CertificateOfService cosNotifyClaimDefendant2;

    private final List<EvidenceUploadDisclosure> disclosureSelectionEvidence;
    private final List<EvidenceUploadDisclosure> disclosureSelectionEvidenceRes;
    private final List<EvidenceUploadWitness> witnessSelectionEvidence;
    private final List<EvidenceUploadWitness> witnessSelectionEvidenceSmallClaim;
    private final List<EvidenceUploadWitness> witnessSelectionEvidenceRes;
    private final List<EvidenceUploadWitness> witnessSelectionEvidenceSmallClaimRes;
    private final List<EvidenceUploadExpert> expertSelectionEvidenceRes;
    private final List<EvidenceUploadExpert> expertSelectionEvidence;
    private final List<EvidenceUploadExpert> expertSelectionEvidenceSmallClaim;
    private final List<EvidenceUploadExpert> expertSelectionEvidenceSmallClaimRes;
    private final List<EvidenceUploadTrial> trialSelectionEvidence;
    private final List<EvidenceUploadTrial> trialSelectionEvidenceSmallClaim;
    private final List<EvidenceUploadTrial> trialSelectionEvidenceRes;
    private final List<EvidenceUploadTrial> trialSelectionEvidenceSmallClaimRes;
    //applicant
    private final List<Element<UploadEvidenceDocumentType>> documentDisclosureList;
    private final List<Element<UploadEvidenceDocumentType>> documentForDisclosure;
    private final List<Element<UploadEvidenceWitness>> documentWitnessStatement;
    private final List<Element<UploadEvidenceWitness>> documentWitnessSummary;
    private final List<Element<UploadEvidenceWitness>> documentHearsayNotice;
    private final List<Element<UploadEvidenceDocumentType>> documentReferredInStatement;
    private final List<Element<UploadEvidenceExpert>> documentExpertReport;
    private final List<Element<UploadEvidenceExpert>> documentJointStatement;
    private final List<Element<UploadEvidenceExpert>> documentQuestions;
    private final List<Element<UploadEvidenceExpert>> documentAnswers;
    private final List<Element<UploadEvidenceDocumentType>> documentCaseSummary;
    private final List<Element<UploadEvidenceDocumentType>> documentSkeletonArgument;
    private final List<Element<UploadEvidenceDocumentType>> documentAuthorities;
    private final List<Element<UploadEvidenceDocumentType>> documentCosts;
    private final List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial;
    //applicant2
    private final List<Element<UploadEvidenceDocumentType>> documentDisclosureListApp2;
    private final List<Element<UploadEvidenceDocumentType>> documentForDisclosureApp2;
    private final List<Element<UploadEvidenceWitness>> documentWitnessStatementApp2;
    private final List<Element<UploadEvidenceWitness>> documentWitnessSummaryApp2;
    private final List<Element<UploadEvidenceWitness>> documentHearsayNoticeApp2;
    private final List<Element<UploadEvidenceDocumentType>> documentReferredInStatementApp2;
    private final List<Element<UploadEvidenceExpert>> documentExpertReportApp2;
    private final List<Element<UploadEvidenceExpert>> documentJointStatementApp2;
    private final List<Element<UploadEvidenceExpert>> documentQuestionsApp2;
    private final List<Element<UploadEvidenceExpert>> documentAnswersApp2;
    private final List<Element<UploadEvidenceDocumentType>> documentCaseSummaryApp2;
    private final List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentApp2;
    private final List<Element<UploadEvidenceDocumentType>> documentAuthoritiesApp2;
    private final List<Element<UploadEvidenceDocumentType>> documentCostsApp2;
    private final List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialApp2;
    private final LocalDateTime caseDocumentUploadDate;
    //respondent
    private final List<Element<UploadEvidenceDocumentType>> documentDisclosureListRes;
    private final List<Element<UploadEvidenceDocumentType>> documentForDisclosureRes;
    private final List<Element<UploadEvidenceWitness>> documentWitnessStatementRes;
    private final List<Element<UploadEvidenceWitness>> documentWitnessSummaryRes;
    private final List<Element<UploadEvidenceWitness>> documentHearsayNoticeRes;
    private final List<Element<UploadEvidenceDocumentType>> documentReferredInStatementRes;
    private final List<Element<UploadEvidenceExpert>> documentExpertReportRes;
    private final List<Element<UploadEvidenceExpert>> documentJointStatementRes;
    private final List<Element<UploadEvidenceExpert>> documentQuestionsRes;
    private final List<Element<UploadEvidenceExpert>> documentAnswersRes;
    private final List<Element<UploadEvidenceDocumentType>> documentCaseSummaryRes;
    private final List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentRes;
    private final List<Element<UploadEvidenceDocumentType>> documentAuthoritiesRes;
    private final List<Element<UploadEvidenceDocumentType>> documentCostsRes;
    private final List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialRes;
    //these fields are shown if the solicitor is for respondent 2 and respondents have different solicitors
    private final List<Element<UploadEvidenceDocumentType>> documentDisclosureListRes2;
    private final List<Element<UploadEvidenceDocumentType>> documentForDisclosureRes2;
    private final List<Element<UploadEvidenceWitness>> documentWitnessStatementRes2;
    private final List<Element<UploadEvidenceWitness>> documentWitnessSummaryRes2;
    private final List<Element<UploadEvidenceWitness>> documentHearsayNoticeRes2;
    private final List<Element<UploadEvidenceDocumentType>> documentReferredInStatementRes2;
    private final List<Element<UploadEvidenceExpert>> documentExpertReportRes2;
    private final List<Element<UploadEvidenceExpert>> documentJointStatementRes2;
    private final List<Element<UploadEvidenceExpert>> documentQuestionsRes2;
    private final List<Element<UploadEvidenceExpert>> documentAnswersRes2;
    private final List<Element<UploadEvidenceDocumentType>> documentCaseSummaryRes2;
    private final List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentRes2;
    private final List<Element<UploadEvidenceDocumentType>> documentAuthoritiesRes2;
    private final List<Element<UploadEvidenceDocumentType>> documentCostsRes2;
    private final List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialRes2;
    private final LocalDateTime caseDocumentUploadDateRes;
    private final HearingNotes hearingNotes;
    private final List<Element<UploadEvidenceDocumentType>> applicantDocsUploadedAfterBundle;
    private final List<Element<UploadEvidenceDocumentType>> respondentDocsUploadedAfterBundle;
    private final Flags caseFlags;
    private final List<Element<PartyFlagStructure>> applicantExperts;
    private final List<Element<PartyFlagStructure>> respondent1Experts;
    private final List<Element<PartyFlagStructure>> respondent2Experts;
    private final List<Element<PartyFlagStructure>> applicantWitnesses;
    private final List<Element<PartyFlagStructure>> respondent1Witnesses;
    private final List<Element<PartyFlagStructure>> respondent2Witnesses;
    private final List<Element<PartyFlagStructure>> applicantSolOrgIndividuals;
    private final List<Element<PartyFlagStructure>> respondent1SolOrgIndividuals;
    private final List<Element<PartyFlagStructure>> applicant1SolOrgIndividuals;

    private List<DisposalAndTrialHearingDJToggle> disposalHearingDisclosureOfDocumentsDJToggle;
    private List<DisposalAndTrialHearingDJToggle> disposalHearingWitnessOfFactDJToggle;
    private List<DisposalAndTrialHearingDJToggle> disposalHearingMedicalEvidenceDJToggle;
    private List<DisposalAndTrialHearingDJToggle> disposalHearingQuestionsToExpertsDJToggle;
    private List<DisposalAndTrialHearingDJToggle> disposalHearingSchedulesOfLossDJToggle;
    private List<DisposalAndTrialHearingDJToggle> disposalHearingStandardDisposalOrderDJToggle;
    private List<DisposalAndTrialHearingDJToggle> disposalHearingFinalDisposalHearingDJToggle;
    private List<DisposalAndTrialHearingDJToggle> disposalHearingBundleDJToggle;
    private List<DisposalAndTrialHearingDJToggle> disposalHearingClaimSettlingDJToggle;
    private List<DisposalAndTrialHearingDJToggle> disposalHearingCostsDJToggle;

    private List<DisposalAndTrialHearingDJToggle> trialHearingAlternativeDisputeDJToggle;
    private List<DisposalAndTrialHearingDJToggle> trialHearingVariationsDirectionsDJToggle;
    private List<DisposalAndTrialHearingDJToggle> trialHearingSettlementDJToggle;
    private List<DisposalAndTrialHearingDJToggle> trialHearingDisclosureOfDocumentsDJToggle;
    private List<DisposalAndTrialHearingDJToggle> trialHearingWitnessOfFactDJToggle;
    private List<DisposalAndTrialHearingDJToggle> trialHearingSchedulesOfLossDJToggle;
    private List<DisposalAndTrialHearingDJToggle> trialHearingCostsToggle;
    private List<DisposalAndTrialHearingDJToggle> trialHearingTrialDJToggle;

    private List<CaseManagementOrderAdditional> caseManagementOrderAdditional;

    private final List<Element<CaseDocument>> requestForInfoDocStaff;
    private final List<Element<CaseDocument>> requestForInfoDocClaimant;
    private final List<Element<CaseDocument>> requestForInfoDocRespondentSol;
    private final List<Element<CaseDocument>> requestForInfoDocRespondentSolTwo;

    private final List<Element<CaseDocument>> writtenRepSequentialDocument;

    private final List<Element<CaseDocument>> writtenRepSeqDocStaff;
    private final List<Element<CaseDocument>> writtenRepSeqDocClaimant;
    private final List<Element<CaseDocument>> writtenRepSeqDocRespondentSol;
    private final List<Element<CaseDocument>> writtenRepSeqDocRespondentSolTwo;

    private final List<Element<CaseDocument>> writtenRepConcurrentDocument;

    private final List<Element<CaseDocument>> writtenRepConDocStaff;
    private final List<Element<CaseDocument>> writtenRepConDocClaimant;
    private final List<Element<CaseDocument>> writtenRepConDocRespondentSol;
    private final List<Element<CaseDocument>> writtenRepConDocRespondentSolTwo;

    private final List<Element<CaseDocument>> hearingOrderDocument;

    private final List<Element<CaseDocument>> hearingOrderDocStaff;
    private final List<Element<CaseDocument>> hearingOrderDocClaimant;
    private final List<Element<CaseDocument>> hearingOrderDocRespondentSol;
    private final List<Element<CaseDocument>> hearingOrderDocRespondentSolTwo;

    private final List<Element<CaseDocument>> requestForInformationDocument;

    private final List<Element<CaseDocument>> dismissalOrderDocument;
    private final List<Element<CaseDocument>> dismissalOrderDocStaff;
    private final List<Element<CaseDocument>> dismissalOrderDocClaimant;
    private final List<Element<CaseDocument>> dismissalOrderDocRespondentSol;
    private final List<Element<CaseDocument>> dismissalOrderDocRespondentSolTwo;

    private final List<Element<CaseDocument>> directionOrderDocument;
    private final List<Element<CaseDocument>> directionOrderDocStaff;
    private final List<Element<CaseDocument>> directionOrderDocClaimant;
    private final List<Element<CaseDocument>> directionOrderDocRespondentSol;
    private final List<Element<CaseDocument>> directionOrderDocRespondentSolTwo;

    private final List<Element<CaseDocument>> hearingNoticeDocument;
    private final List<Element<CaseDocument>> hearingNoticeDocStaff;
    private final List<Element<CaseDocument>> hearingNoticeDocClaimant;
    private final List<Element<CaseDocument>> hearingNoticeDocRespondentSol;
    private final List<Element<CaseDocument>> hearingNoticeDocRespondentSolTwo;

    private final List<Element<Document>> gaRespDocument;
    private final List<Element<Document>> gaRespDocStaff;
    private final List<Element<Document>> gaRespDocClaimant;
    private final List<Element<Document>> gaRespDocRespondentSol;
    private final List<Element<Document>> gaRespDocRespondentSolTwo;

    private final List<Element<CaseDocument>> gaDraftDocument;
    private final List<Element<CaseDocument>> gaDraftDocStaff;
    private final List<Element<CaseDocument>> gaDraftDocClaimant;
    private final List<Element<CaseDocument>> gaDraftDocRespondentSol;
    private final List<Element<CaseDocument>> gaDraftDocRespondentSolTwo;

    /* Final Orders */

    private YesOrNo finalOrderMadeSelection;
    private OrderMade finalOrderDateHeardComplex;
    private List<FinalOrderToggle> finalOrderJudgeHeardFrom;
    private FinalOrderRepresentation finalOrderRepresentation;
    private List<FinalOrderToggle> finalOrderRecitals;
    private FinalOrderRecitalsRecorded finalOrderRecitalsRecorded;
    private String finalOrderOrderedThatText;
    private AssistedCostTypesList assistedOrderCostList;
    private AssistedOrderCostDetails assistedOrderCostsReserved;
    private AssistedOrderCostDetails assistedOrderCostsBespoke;
    private AssistedOrderCostDetails assistedOrderMakeAnOrderForCosts;
    private YesOrNo publicFundingCostsProtection;
    private List<FinalOrderToggle> finalOrderFurtherHearingToggle;
    private FinalOrderFurtherHearing finalOrderFurtherHearingComplex;
    private HearingLengthFinalOrderList lengthList;
    private List<FinalOrderToggle> finalOrderAppealToggle;
    private FinalOrderAppeal finalOrderAppealComplex;
    private OrderMadeOnTypes orderMadeOnDetailsList;
    private OrderMadeOnDetails orderMadeOnDetailsOrderCourt;
    private OrderMadeOnDetailsOrderWithoutNotice orderMadeOnDetailsOrderWithoutNotice;
    private YesOrNo finalOrderGiveReasonsYesNo;
    private AssistedOrderReasons finalOrderGiveReasonsComplex;

    //Hearing Scheduled
    private DynamicList hearingLocation;
    private LocalDate dateOfApplication;
    private LocalDate hearingDate;
    private LocalDate hearingDueDate;
    private String hearingTimeHourMinute;
    private String hearingReferenceNumber;
    private ListingOrRelisting listingOrRelisting;
    private HearingNoticeList hearingNoticeList;
    private Fee hearingFee;
    private HearingChannel channel;
    private HearingDuration hearingDuration;
    private String information;
    private String hearingNoticeListOther;
    private LocalDateTime caseDismissedHearingFeeDueDate;

    //Trial Readiness
    private YesOrNo trialReadyNotified;
    private YesOrNo trialReadyChecked;

    private YesOrNo trialReadyApplicant;
    private YesOrNo trialReadyRespondent1;
    private YesOrNo trialReadyRespondent2;

    private RevisedHearingRequirements applicantRevisedHearingRequirements;
    private RevisedHearingRequirements respondent1RevisedHearingRequirements;
    private RevisedHearingRequirements respondent2RevisedHearingRequirements;

    private HearingOtherComments applicantHearingOtherComments;
    private HearingOtherComments respondent1HearingOtherComments;
    private HearingOtherComments respondent2HearingOtherComments;

    @Builder.Default
    private final List<Element<CaseDocument>> trialReadyDocuments = new ArrayList<>();

    //default judgement SDO fields for trial/fast track
    private TrialHearingJudgesRecital trialHearingJudgesRecitalDJ;
    private TrialHearingDisclosureOfDocuments trialHearingDisclosureOfDocumentsDJ;
    private TrialHearingWitnessOfFact trialHearingWitnessOfFactDJ;
    private TrialHearingSchedulesOfLoss trialHearingSchedulesOfLossDJ;
    private TrialHearingTrial trialHearingTrialDJ;
    private TrialHearingTimeDJ trialHearingTimeDJ;
    private TrialHearingNotes trialHearingNotesDJ;
    private TrialHearingHearingNotesDJ trialHearingHearingNotesDJ;
    private TrialOrderMadeWithoutHearingDJ trialOrderMadeWithoutHearingDJ;
    private TrialBuildingDispute trialBuildingDispute;
    private TrialClinicalNegligence trialClinicalNegligence;
    private TrialCreditHire trialCreditHire;
    private TrialPersonalInjury trialPersonalInjury;
    private TrialRoadTrafficAccident trialRoadTrafficAccident;
    private TrialEmployersLiability trialEmployersLiability;
    private TrialHousingDisrepair trialHousingDisrepair;
    private DisposalHearingMethodDJ trialHearingMethodDJ;
    private HearingMethodTelephoneHearingDJ trialHearingMethodTelephoneHearingDJ;
    private HearingMethodVideoConferenceDJ trialHearingMethodVideoConferenceHearingDJ;
    private final Address specRespondent2CorrespondenceAddressdetails;
    private final YesOrNo specRespondent2CorrespondenceAddressRequired;

    private List<Element<UnavailableDate>> applicant1UnavailableDatesForTab;
    private List<Element<UnavailableDate>> applicant2UnavailableDatesForTab;
    private List<Element<UnavailableDate>> respondent1UnavailableDatesForTab;
    private List<Element<UnavailableDate>> respondent2UnavailableDatesForTab;

    @JsonUnwrapped
    private final UpdateDetailsForm updateDetailsForm;

    private FastTrackAllocation fastTrackAllocation;

    @JsonIgnore
    public boolean isResponseAcceptedByClaimant() {
        return applicant1AcceptAdmitAmountPaidSpec == YesOrNo.YES
            || applicant1AcceptFullAdmitPaymentPlanSpec == YesOrNo.YES
            || applicant1AcceptPartAdmitPaymentPlanSpec == YesOrNo.YES;
    }

    private final IdamUserDetails claimantUserDetails;
    private final IdamUserDetails defendantUserDetails;

    private final ClaimProceedsInCasemanLR claimProceedsInCasemanLR;

    @JsonIgnore
    public BigDecimal getUpFixedCostAmount(BigDecimal claimAmount) {
        BigDecimal lowerRangeClaimAmount = BigDecimal.valueOf(25);
        BigDecimal upperRangeClaimAmount = BigDecimal.valueOf(5000);
        BigDecimal midCostAmount = BigDecimal.valueOf(40);

        if ((!YES.equals(getCcjPaymentDetails().getCcjJudgmentFixedCostOption())
            || (claimAmount.compareTo(lowerRangeClaimAmount) < 0))) {
            return ZERO;
        }
        if (claimAmount.compareTo(upperRangeClaimAmount) <= 0) {
            return midCostAmount;
        }
        return BigDecimal.valueOf(55);
    }

    @JsonIgnore
    public boolean isRespondentResponseBilingual() {
        return Optional.ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1ResponseLanguage)
            .filter(Language.BOTH.toString()::equals)
            .isPresent();
    }

    @JsonIgnore
    public boolean hasClaimantAgreedToFreeMediation() {
        return Optional.ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1ClaimMediationSpecRequiredLip)
            .filter(ClaimantMediationLip::hasClaimantAgreedToFreeMediation).isPresent();
    }

    @JsonIgnore
    public Optional<TranslatedDocument> getTranslatedDocument() {
        return Optional.ofNullable(getCaseDataLiP()).map(CaseDataLiP::getTranslatedDocument);
    }

    @JsonIgnore
    public boolean hasClaimantNotAgreedToFreeMediation() {
        return Optional.ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1ClaimMediationSpecRequiredLip)
            .filter(ClaimantMediationLip::hasClaimantNotAgreedToFreeMediation).isPresent();
    }

    @JsonIgnore
    public boolean isTranslatedDocumentUploaded() {
        return getCaseDataLiP() != null && getCaseDataLiP().getTranslatedDocument() != null;
    }
}
