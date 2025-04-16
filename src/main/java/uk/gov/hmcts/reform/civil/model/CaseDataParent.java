package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
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
import uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesForTab;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsReferredInStatement;
import uk.gov.hmcts.reform.civil.model.mediation.MediationNonAttendanceStatement;
import uk.gov.hmcts.reform.civil.model.mediation.UploadMediationDocumentsForm;
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddNewDirection;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2EvidenceAcousticEngineer;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ExpertEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2PermissionToRelyOnExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsToEntExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Settlement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsAddNewDirection;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsImpNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsPPI;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsUploadDoc;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2UploadOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2VariationOfDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;
import uk.gov.hmcts.reform.civil.model.transferonlinecase.NotSuitableSdoOptions;
import uk.gov.hmcts.reform.civil.model.transferonlinecase.TocTransferCaseReason;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.math.BigDecimal.ZERO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Data
public class CaseDataParent extends CaseDataCaseProgression implements MappableObject {

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

    /**
     * SNI-5142 made mandatory SHOW.
     */
    private List<OrderDetailsPagesSectionsToggle> smallClaimsMethodToggle;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsDocumentsToggle;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsWitnessStatementToggle;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsFlightDelayToggle;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsMediationSectionToggle;
    private List<DateToShowToggle> smallClaimsHearingDateToToggle;
    private List<DateToShowToggle> fastTrackTrialDateToToggle;

    //SDOR2
    private YesOrNo isSdoR2NewScreen;
    private FastTrackJudgesRecital sdoFastTrackJudgesRecital;
    private SdoR2FastTrackAltDisputeResolution sdoAltDisputeResolution;
    private SdoR2VariationOfDirections sdoVariationOfDirections;
    private SdoR2Settlement sdoR2Settlement;
    private List<IncludeInOrderToggle> sdoR2DisclosureOfDocumentsToggle;
    private SdoR2DisclosureOfDocuments sdoR2DisclosureOfDocuments;
    private List<IncludeInOrderToggle> sdoR2SeparatorWitnessesOfFactToggle;
    private SdoR2WitnessOfFact sdoR2WitnessesOfFact;
    private SdoR2WitnessOfFact sdoR2FastTrackWitnessOfFact;
    private List<IncludeInOrderToggle> sdoR2ScheduleOfLossToggle;
    private SdoR2ScheduleOfLoss sdoR2ScheduleOfLoss;
    private List<Element<SdoR2AddNewDirection>> sdoR2AddNewDirection;
    private List<IncludeInOrderToggle> sdoR2TrialToggle;
    private SdoR2Trial sdoR2Trial;
    private String sdoR2ImportantNotesTxt;
    private LocalDate sdoR2ImportantNotesDate;
    private List<IncludeInOrderToggle> sdoR2SeparatorExpertEvidenceToggle;
    private SdoR2ExpertEvidence sdoR2ExpertEvidence;
    private List<IncludeInOrderToggle> sdoR2SeparatorAddendumReportToggle;
    private SdoR2AddendumReport sdoR2AddendumReport;
    private List<IncludeInOrderToggle> sdoR2SeparatorFurtherAudiogramToggle;
    private SdoR2FurtherAudiogram sdoR2FurtherAudiogram;
    private List<IncludeInOrderToggle> sdoR2SeparatorQuestionsClaimantExpertToggle;
    private SdoR2QuestionsClaimantExpert sdoR2QuestionsClaimantExpert;
    private List<IncludeInOrderToggle> sdoR2SeparatorPermissionToRelyOnExpertToggle;
    private SdoR2PermissionToRelyOnExpert sdoR2PermissionToRelyOnExpert;
    private List<IncludeInOrderToggle> sdoR2SeparatorEvidenceAcousticEngineerToggle;
    private SdoR2EvidenceAcousticEngineer sdoR2EvidenceAcousticEngineer;
    private List<IncludeInOrderToggle> sdoR2SeparatorQuestionsToEntExpertToggle;
    private SdoR2QuestionsToEntExpert sdoR2QuestionsToEntExpert;
    private List<IncludeInOrderToggle> sdoR2SeparatorUploadOfDocumentsToggle;
    private SdoR2UploadOfDocuments sdoR2UploadOfDocuments;
    private SdoR2SmallClaimsJudgesRecital sdoR2SmallClaimsJudgesRecital;
    private List<IncludeInOrderToggle> sdoR2SmallClaimsPPIToggle;
    private SdoR2SmallClaimsPPI sdoR2SmallClaimsPPI;
    private List<IncludeInOrderToggle> sdoR2SmallClaimsWitnessStatementsToggle;
    private SdoR2SmallClaimsWitnessStatements sdoR2SmallClaimsWitnessStatements;
    private SdoR2SmallClaimsWitnessStatements sdoR2SmallClaimsWitnessStatementOther;
    private List<IncludeInOrderToggle> sdoR2SmallClaimsUploadDocToggle;
    private SdoR2SmallClaimsUploadDoc sdoR2SmallClaimsUploadDoc;
    private List<IncludeInOrderToggle> sdoR2SmallClaimsHearingToggle;
    private SdoR2SmallClaimsHearing sdoR2SmallClaimsHearing;
    private SdoR2SmallClaimsMediation sdoR2SmallClaimsMediationSectionStatement;
    private List<IncludeInOrderToggle> sdoR2SmallClaimsMediationSectionToggle;
    private SdoR2SmallClaimsImpNotes sdoR2SmallClaimsImpNotes;
    private List<Element<SdoR2SmallClaimsAddNewDirection>> sdoR2SmallClaimsAddNewDirection;
    private List<OrderDetailsPagesSectionsToggle> sdoR2FastTrackUseOfWelshToggle;
    private SdoR2WelshLanguageUsage sdoR2FastTrackUseOfWelshLanguage;
    private List<OrderDetailsPagesSectionsToggle> sdoR2SmallClaimsUseOfWelshToggle;
    private SdoR2WelshLanguageUsage sdoR2SmallClaimsUseOfWelshLanguage;
    private List<IncludeInOrderToggle> sdoR2NihlUseOfWelshIncludeInOrderToggle;
    private SdoR2WelshLanguageUsage sdoR2NihlUseOfWelshLanguage;
    private List<IncludeInOrderToggle> sdoR2DrhUseOfWelshIncludeInOrderToggle;
    private SdoR2WelshLanguageUsage sdoR2DrhUseOfWelshLanguage;
    private List<OrderDetailsPagesSectionsToggle> sdoR2DisposalHearingUseOfWelshToggle;
    private SdoR2WelshLanguageUsage sdoR2DisposalHearingUseOfWelshLanguage;
    private SdoR2WelshLanguageUsage sdoR2DisposalHearingWelshLanguageDJ;
    private List<DisposalAndTrialHearingDJToggle> sdoR2DisposalHearingUseOfWelshLangToggleDJ;
    private SdoR2WelshLanguageUsage sdoR2TrialWelshLanguageDJ;
    private List<DisposalAndTrialHearingDJToggle> sdoR2TrialUseOfWelshLangToggleDJ;
    private SdoR2FastTrackCreditHire sdoR2FastTrackCreditHire;

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

    @Deprecated
    private final YesOrNo respondent2DQCarerAllowanceCreditFullAdmission;
    @Deprecated
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

    private final NextHearingDetails nextHearingDetails;

    private final String respondent1EmailAddress;
    private final YesOrNo applicant1Represented;
    private final YesOrNo anyRepresented;

    /**
     * Adding for LR ITP Update.
     */
    private final ResponseOneVOneShowTag showResponseOneVOneFlag;
    private final YesOrNo applicant1AcceptAdmitAmountPaidSpec;
    private final YesOrNo applicant1FullDefenceConfirmAmountPaidSpec;
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
    private final LocalDate applicant1SuggestPayImmediatelyPaymentDateForDefendantSpec;
    private final String currentDateboxDefendantSpec;
    @JsonUnwrapped
    private final CCJPaymentDetails ccjPaymentDetails;
    private final PaymentType applicant1RepaymentOptionForDefendantSpec;

    @JsonUnwrapped
    private final CaseDataLiP caseDataLiP;
    private final HelpWithFeesMoreInformation helpWithFeesMoreInformationClaimIssue;
    private final HelpWithFeesMoreInformation helpWithFeesMoreInformationHearing;
    private final HelpWithFeesForTab claimIssuedHwfForTab;
    private final HelpWithFeesForTab hearingHwfForTab;
    private final YesOrNo applicantDefenceResponseDocumentAndDQFlag;
    private final String migrationId;

    @JsonIgnore
    public boolean isApplicantNotRepresented() {
        return this.applicant1Represented == NO;
    }

    @JsonIgnore
    public boolean isApplicantRepresented() {
        return this.applicant1Represented == YES;
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

    //Top level structure objects used for Hearings + Case Flags
    private final Flags caseFlags;
    private final List<Element<PartyFlagStructure>> applicantExperts;
    private final List<Element<PartyFlagStructure>> respondent1Experts;
    private final List<Element<PartyFlagStructure>> respondent2Experts;
    private final List<Element<PartyFlagStructure>> applicantWitnesses;
    private final List<Element<PartyFlagStructure>> respondent1Witnesses;
    private final List<Element<PartyFlagStructure>> respondent2Witnesses;
    //Individuals attending from parties that are Org/Company
    private final List<Element<PartyFlagStructure>> applicant1OrgIndividuals;
    private final List<Element<PartyFlagStructure>> applicant2OrgIndividuals;
    private final List<Element<PartyFlagStructure>> respondent1OrgIndividuals;
    private final List<Element<PartyFlagStructure>> respondent2OrgIndividuals;
    //Individuals attending from Legal Representative Firms
    private final List<Element<PartyFlagStructure>> applicant1LRIndividuals;
    private final List<Element<PartyFlagStructure>> respondent1LRIndividuals;
    private final List<Element<PartyFlagStructure>> respondent2LRIndividuals;

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

    private final Address specRespondent2CorrespondenceAddressdetails;
    private final YesOrNo specRespondent2CorrespondenceAddressRequired;

    private List<Element<UnavailableDate>> applicant1UnavailableDatesForTab;
    private List<Element<UnavailableDate>> applicant2UnavailableDatesForTab;
    private List<Element<UnavailableDate>> respondent1UnavailableDatesForTab;
    private List<Element<UnavailableDate>> respondent2UnavailableDatesForTab;
    private String pcqId;
    private String respondentResponsePcqId;

    // Transfer a Case Online
    private String reasonForTransfer;
    private DynamicList transferCourtLocationList;
    private NotSuitableSdoOptions notSuitableSdoOptions;
    private TocTransferCaseReason tocTransferCaseReason;
    private String claimantBilingualLanguagePreference;

    @JsonUnwrapped
    private final UpdateDetailsForm updateDetailsForm;

    private FastTrackAllocation fastTrackAllocation;

    private YesOrNo showCarmFields;

    @JsonUnwrapped
    private UploadMediationDocumentsForm uploadMediationDocumentsForm;

    private List<Element<MediationNonAttendanceStatement>> app1MediationNonAttendanceDocs;
    private List<Element<MediationDocumentsReferredInStatement>> app1MediationDocumentsReferred;

    private List<Element<MediationNonAttendanceStatement>> app2MediationNonAttendanceDocs;
    private List<Element<MediationDocumentsReferredInStatement>> app2MediationDocumentsReferred;

    private List<Element<MediationNonAttendanceStatement>> res1MediationNonAttendanceDocs;
    private List<Element<MediationDocumentsReferredInStatement>> res1MediationDocumentsReferred;

    private List<Element<MediationNonAttendanceStatement>> res2MediationNonAttendanceDocs;
    private List<Element<MediationDocumentsReferredInStatement>> res2MediationDocumentsReferred;

    private SmallClaimsMediation smallClaimsMediationSectionStatement;

    private FixedCosts fixedCosts;
    private YesOrNo showDJFixedCostsScreen;
    private YesOrNo showOldDJFixedCostsScreen;
    private YesOrNo claimFixedCostsOnEntryDJ;

    private YesOrNo mediationFileSentToMmt;

    @JsonIgnore
    public boolean isResponseAcceptedByClaimant() {
        return applicant1AcceptAdmitAmountPaidSpec == YesOrNo.YES
            || applicant1AcceptFullAdmitPaymentPlanSpec == YesOrNo.YES
            || applicant1AcceptPartAdmitPaymentPlanSpec == YesOrNo.YES;
    }

    private final IdamUserDetails claimantUserDetails;
    private final IdamUserDetails defendantUserDetails;

    private final ClaimProceedsInCasemanLR claimProceedsInCasemanLR;
    private final ResponseDocument applicant1DefenceResponseDocumentSpec;

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
        Optional<CaseDataLiP> caseDataLiP1 = Optional.ofNullable(getCaseDataLiP());
        return caseDataLiP1.map(CaseDataLiP::getApplicant1ClaimMediationSpecRequiredLip)
            .filter(ClaimantMediationLip::hasClaimantAgreedToFreeMediation).isPresent()
            || isCorrectEmailPresent(caseDataLiP1)
            || isCorrectPhonePresent(caseDataLiP1);
    }

    private static boolean isCorrectPhonePresent(Optional<CaseDataLiP> caseDataLiP1) {
        return (caseDataLiP1.map(CaseDataLiP::getApplicant1LiPResponseCarm)
            .filter(carm -> carm.getIsMediationPhoneCorrect() == YES).isPresent()
            || caseDataLiP1.map(CaseDataLiP::getApplicant1LiPResponseCarm)
            .filter(carm -> carm.getAlternativeMediationTelephone() != null).isPresent());
    }

    private static boolean isCorrectEmailPresent(Optional<CaseDataLiP> caseDataLiP1) {
        return (caseDataLiP1.map(CaseDataLiP::getApplicant1LiPResponseCarm)
            .filter(carm -> carm.getIsMediationEmailCorrect() == YES).isPresent()
            || caseDataLiP1.map(CaseDataLiP::getApplicant1LiPResponseCarm)
            .filter(carm -> carm.getAlternativeMediationEmail() != null).isPresent());
    }

    @JsonIgnore
    public List<Element<TranslatedDocument>> getTranslatedDocuments() {
        return Optional.ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getTranslatedDocuments)
            .orElse(Collections.emptyList());
    }

    @JsonIgnore
    public boolean hasClaimantNotAgreedToFreeMediation() {
        return Optional.ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1ClaimMediationSpecRequiredLip)
            .filter(ClaimantMediationLip::hasClaimantNotAgreedToFreeMediation).isPresent();
    }

    @JsonIgnore
    public boolean isClaimantBilingual() {
        return null != claimantBilingualLanguagePreference
            && !claimantBilingualLanguagePreference.equalsIgnoreCase(Language.ENGLISH.toString());
    }

    @JsonIgnore
    public boolean isFullDefenceNotPaid() {
        return NO.equals(getApplicant1FullDefenceConfirmAmountPaidSpec());
    }

    @JsonIgnore
    public boolean applicant1SuggestedPayImmediately() {
        return applicant1RepaymentOptionForDefendantSpec == PaymentType.IMMEDIATELY;
    }

    @JsonIgnore
    public boolean applicant1SuggestedPayBySetDate() {
        return applicant1RepaymentOptionForDefendantSpec == PaymentType.SET_DATE;
    }

    @JsonIgnore
    public boolean hasClaimantAgreedClaimSettled() {
        return Optional.ofNullable(getCaseDataLiP())
            .filter(CaseDataLiP::hasClaimantAgreedClaimSettled).isPresent();
    }
}
