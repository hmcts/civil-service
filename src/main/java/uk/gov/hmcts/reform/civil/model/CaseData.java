package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseNoteType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.EmploymentTypeCheckboxFixedListLRspec;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.PersonalInjuryType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.dj.HearingMethodTelephoneHearingDJ;
import uk.gov.hmcts.reform.civil.enums.dj.HearingMethodVideoConferenceDJ;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingAddNewDirectionsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingDisclosureOfDocumentsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingFinalDisposalHearingDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingJudgesRecitalDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingMedicalEvidenceDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingNotesDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingQuestionsToExpertsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingSchedulesOfLossDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingWitnessOfFactDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingAddNewDirectionsDJ;
import uk.gov.hmcts.reform.civil.model.documents.DocumentAndNote;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithName;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.ExpertRequirements;
import uk.gov.hmcts.reform.civil.model.dq.RecurringExpenseLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimUntilType;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaidInFull;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingNotesDJ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.model.sdo.OtherDetails;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.FINISHED;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@Data
public class CaseData extends CaseDataParent implements MappableObject {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final Long ccdCaseReference;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final CaseState ccdState;
    private final GAApplicationType generalAppType;
    private final GARespondentOrderAgreement generalAppRespondentAgreement;
    private final GAPbaDetails generalAppPBADetails;
    private final String generalAppDetailsOfOrder;
    private final String generalAppReasonsOfOrder;
    private final GAInformOtherParty generalAppInformOtherParty;
    private final GAUrgencyRequirement generalAppUrgencyRequirement;
    private final GAStatementOfTruth generalAppStatementOfTruth;
    private final GAHearingDetails generalAppHearingDetails;
    private final GASolicitorDetailsGAspec generalAppApplnSolicitor;
    private final SRPbaDetails hearingFeePBADetails;
    private final SRPbaDetails claimIssuedPBADetails;
    private final String applicantPartyName;

    private final YesOrNo generalAppVaryJudgementType;
    private final YesOrNo generalAppParentClaimantIsApplicant;
    private final GAHearingDateGAspec generalAppHearingDate;
    private final Document generalAppN245FormUpload;

    @Builder.Default
    private final List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors = new ArrayList<>();

    @Builder.Default
    private final List<Element<GeneralApplication>> generalApplications = new ArrayList<>();

    private final List<Element<GeneralApplicationsDetails>> claimantGaAppDetails;
    private final List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection;
    private final List<Element<GADetailsRespondentSol>> respondentSolGaAppDetails;
    private final List<Element<GADetailsRespondentSol>> respondentSolTwoGaAppDetails;
    private final SolicitorReferences solicitorReferences;
    private final SolicitorReferences solicitorReferencesCopy;
    private final String respondentSolicitor2Reference;
    private final CourtLocation courtLocation;
    private final Party applicant1;
    private final Party applicant2;
    private final CorrectEmail applicantSolicitor1CheckEmail;
    private final IdamUserDetails applicantSolicitor1UserDetails;
    private final YesOrNo addApplicant2;
    private final YesOrNo addRespondent2;
    private final YesOrNo respondent2SameLegalRepresentative;
    private final Party respondent1;
    private final Party respondent1Copy;
    private final Party respondent2;
    private final Party respondent2Copy;
    private final Party respondent1DetailsForClaimDetailsTab;
    private final Party respondent2DetailsForClaimDetailsTab;
    private final YesOrNo respondent1Represented;
    private final YesOrNo respondent2Represented;
    private final YesOrNo respondent1OrgRegistered;
    private final YesOrNo respondent2OrgRegistered;
    private final String respondentSolicitor1EmailAddress;
    private final String respondentSolicitor2EmailAddress;
    private final YesOrNo uploadParticularsOfClaim;
    private final String detailsOfClaim;
    private final ClaimValue claimValue;
    private final Fee claimFee;
    private final String paymentReference;
    private final DynamicList applicantSolicitor1PbaAccounts;
    private final ClaimType claimType;
    private final SuperClaimType superClaimType;
    private final String claimTypeOther;
    private final PersonalInjuryType personalInjuryType;
    private final String personalInjuryTypeOther;
    private final StatementOfTruth applicantSolicitor1ClaimStatementOfTruth;
    private final StatementOfTruth uiStatementOfTruth;
    private final String legacyCaseReference;
    private final AllocatedTrack allocatedTrack;
    private final PaymentDetails paymentDetails;
    private final PaymentDetails claimIssuedPaymentDetails;
    private final PaymentDetails hearingFeePaymentDetails;
    private final OrganisationPolicy applicant1OrganisationPolicy;
    private final OrganisationPolicy applicant2OrganisationPolicy;
    private final OrganisationPolicy respondent1OrganisationPolicy;
    private final OrganisationPolicy respondent2OrganisationPolicy;
    private final SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails;
    private final SolicitorOrganisationDetails respondentSolicitor2OrganisationDetails;
    private final YesOrNo applicantSolicitor1ServiceAddressRequired;
    private final Address applicantSolicitor1ServiceAddress;
    private final YesOrNo respondentSolicitor1ServiceAddressRequired;
    private final Address respondentSolicitor1ServiceAddress;
    private final YesOrNo respondentSolicitor2ServiceAddressRequired;
    private final Address respondentSolicitor2ServiceAddress;
    private final StatementOfTruth applicant1ServiceStatementOfTruthToRespondentSolicitor1;

    @Builder.Default
    private final List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
    private final List<Element<ManageDocument>> manageDocuments;
    private final Document specClaimTemplateDocumentFiles;
    private final Document specClaimDetailsDocumentFiles;
    private final List<Evidence> speclistYourEvidenceList;
    private final YesOrNo specApplicantCorrespondenceAddressRequired;
    private final Address specApplicantCorrespondenceAddressdetails;
    private final YesOrNo specRespondentCorrespondenceAddressRequired;
    private final Address specRespondentCorrespondenceAddressdetails;
    private final YesOrNo specAoSRespondent2HomeAddressRequired;
    private final Address specAoSRespondent2HomeAddressDetails;

    private final LocalDate respondentSolicitor1AgreedDeadlineExtension;
    private final LocalDate respondentSolicitor2AgreedDeadlineExtension;
    private final ResponseIntention respondent1ClaimResponseIntentionType;
    private final ResponseIntention respondent2ClaimResponseIntentionType;
    private final ResponseIntention respondent1ClaimResponseIntentionTypeApplicant2;
    private final ServedDocumentFiles servedDocumentFiles;

    private final YesOrNo respondentResponseIsSame;
    private final YesOrNo defendantSingleResponseToBothClaimants;
    private final RespondentResponseType respondent1ClaimResponseType;
    private final RespondentResponseType respondent2ClaimResponseType;
    private final RespondentResponseType respondent1ClaimResponseTypeToApplicant2;
    private final ResponseDocument respondent1ClaimResponseDocument;
    private final ResponseDocument respondent2ClaimResponseDocument;
    private final ResponseDocument respondentSharedClaimResponseDocument;
    private final CaseDocument respondent1GeneratedResponseDocument;
    private final CaseDocument respondent2GeneratedResponseDocument;

    @Builder.Default
    private final List<Element<CaseDocument>> defendantResponseDocuments = new ArrayList<>();

    private final YesOrNo applicant1ProceedWithClaim;
    private final YesOrNo applicant1ProceedWithClaimMultiParty2v1;
    private final YesOrNo applicant2ProceedWithClaimMultiParty2v1;
    private final YesOrNo applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2;
    private final YesOrNo applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2;
    private final YesOrNo applicant1ProceedWithClaimRespondent2;
    private final ResponseDocument applicant1DefenceResponseDocument;
    private final ResponseDocument claimantDefenceResDocToDefendant2;

    @Builder.Default
    private final List<Element<CaseDocument>> claimantResponseDocuments = new ArrayList<>();

    private final List<ClaimAmountBreakup> claimAmountBreakup;
    private final List<TimelineOfEvents> timelineOfEvents;
    /**
     * money amount in pounds.
     */
    private BigDecimal totalClaimAmount;
    private BigDecimal totalInterest;
    private final YesOrNo claimInterest;
    private final InterestClaimOptions interestClaimOptions;
    private final SameRateInterestSelection sameRateInterestSelection;
    private final BigDecimal breakDownInterestTotal;
    private final String breakDownInterestDescription;
    private final InterestClaimFromType interestClaimFrom;
    private final InterestClaimUntilType interestClaimUntil;
    private final LocalDate interestFromSpecificDate;
    private final String interestFromSpecificDateDescription;
    private final String calculatedInterest;
    private final String specRespondentSolicitor1EmailAddress;
    private final YesOrNo specAoSApplicantCorrespondenceAddressRequired;
    private final Address specAoSApplicantCorrespondenceAddressdetails;
    private final YesOrNo specAoSRespondentCorrespondenceAddressRequired;
    private final Address specAoSRespondentCorrespondenceAddressdetails;
    private final YesOrNo specRespondent1Represented;
    private final YesOrNo specRespondent2Represented;
    private final List<TimelineOfEvents> specResponseTimelineOfEvents;
    private final TimelineUploadTypeSpec specClaimResponseTimelineList;
    private final ResponseDocument specResponseTimelineDocumentFiles;
    private final List<Evidence> specResponselistYourEvidenceList;

    private final String detailsOfWhyDoesYouDisputeTheClaim;

    private final ResponseDocument respondent1SpecDefenceResponseDocument;
    private final ResponseDocument respondent2SpecDefenceResponseDocument;

    public RespondentResponseTypeSpec getRespondent1ClaimResponseTypeForSpec() {

        if (respondent1ClaimResponseTypeForSpec == null) {
            return getRespondent1ClaimResponseTestForSpec();
        } else {
            return respondent1ClaimResponseTypeForSpec;
        }
    }

    public RespondentResponseTypeSpec getRespondent2ClaimResponseTypeForSpec() {

        if (respondent2ClaimResponseTypeForSpec == null) {
            return getRespondent2ClaimResponseTestForSpec();
        } else {
            return respondent2ClaimResponseTypeForSpec;
        }
    }

    private final RespondentResponseTypeSpec respondent1ClaimResponseTypeForSpec;
    private final RespondentResponseTypeSpec respondent2ClaimResponseTypeForSpec;
    private final RespondentResponseTypeSpec claimant1ClaimResponseTypeForSpec;
    private final RespondentResponseTypeSpec claimant2ClaimResponseTypeForSpec;
    private final RespondentResponseTypeSpecPaidStatus respondent1ClaimResponsePaymentAdmissionForSpec;
    private final RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteRequired;
    private final String defenceRouteRequired;
    private final String responseClaimTrack;
    private final RespondToClaim respondToClaim;
    private final RespondToClaim respondToAdmittedClaim;
    /**
     * money amount in pence.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal respondToAdmittedClaimOwingAmount;
    /**
     * money amount in pounds.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal respondToAdmittedClaimOwingAmountPounds;
    private final YesOrNo specDefenceFullAdmittedRequired;
    private final PaymentUponCourtOrder respondent1CourtOrderPayment;
    private final RepaymentPlanLRspec respondent1RepaymentPlan;
    private final RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec;
    private final UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec;
    private final Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer;
    private final String responseToClaimAdmitPartWhyNotPayLRspec;
    // Fields related to ROC-9453 & ROC-9455
    private final YesOrNo responseClaimMediationSpecRequired;
    private final SmallClaimMedicalLRspec applicant1ClaimMediationSpecRequired;
    private final YesOrNo defenceAdmitPartEmploymentTypeRequired;
    private final YesOrNo responseClaimExpertSpecRequired;
    private final YesOrNo applicant1ClaimExpertSpecRequired;
    private final String responseClaimWitnesses;
    private final String applicant1ClaimWitnesses;
    private final YesOrNo smallClaimHearingInterpreterRequired;
    private final String smallClaimHearingInterpreterDescription;
    private final List<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspec;
    private final YesOrNo specDefenceAdmittedRequired;

    private final String additionalInformationForJudge;
    private final String applicantAdditionalInformationForJudge;
    @JsonUnwrapped
    private final ExpertRequirements respondToClaimExperts;

    private final String caseNote;
    private final List<Element<CaseNote>> caseNotes;

    @Valid
    private final CloseClaim withdrawClaim;

    @Valid
    private final CloseClaim discontinueClaim;

    private final BusinessProcess businessProcess;

    @JsonUnwrapped
    private final Respondent1DQ respondent1DQ;

    @JsonUnwrapped
    private final Respondent2DQ respondent2DQ;

    @JsonUnwrapped
    private final Applicant1DQ applicant1DQ;

    @JsonUnwrapped
    private final Applicant2DQ applicant2DQ;

    public boolean hasNoOngoingBusinessProcess() {
        return businessProcess == null
            || businessProcess.getStatus() == null
            || businessProcess.getStatus() == FINISHED;
    }

    private final LitigationFriend genericLitigationFriend;
    private final LitigationFriend respondent1LitigationFriend;
    private final LitigationFriend respondent2LitigationFriend;

    private final YesOrNo applicant1LitigationFriendRequired;
    private final LitigationFriend applicant1LitigationFriend;

    private final YesOrNo applicant2LitigationFriendRequired;
    private final LitigationFriend applicant2LitigationFriend;

    private final DynamicList defendantSolicitorNotifyClaimOptions;
    private final DynamicList defendantSolicitorNotifyClaimDetailsOptions;
    private final DynamicList selectLitigationFriend;
    private final String litigantFriendSelection;
    @Valid
    private final ClaimProceedsInCaseman claimProceedsInCaseman;
    @Valid
    private final ClaimProceedsInCasemanLR claimProceedsInCasemanLR;

    //CCD UI flag
    private final YesOrNo applicantSolicitor1PbaAccountsIsEmpty;
    private MultiPartyResponseTypeFlags multiPartyResponseTypeFlags;
    private YesOrNo applicantsProceedIntention;
    private final MultiPartyScenario claimantResponseScenarioFlag;
    private YesOrNo claimantResponseDocumentToDefendant2Flag;
    private YesOrNo claimant2ResponseFlag;
    private RespondentResponseTypeSpec atLeastOneClaimResponseTypeForSpecIsFullDefence;
    // used only in 2v1
    private YesOrNo specFullAdmissionOrPartAdmission;
    private YesOrNo sameSolicitorSameResponse;
    private YesOrNo specPaidLessAmountOrDisputesOrPartAdmission;
    private YesOrNo specFullDefenceOrPartAdmission1V1;
    private YesOrNo specFullDefenceOrPartAdmission;
    private YesOrNo specDisputesOrPartAdmission;
    private YesOrNo specPartAdmitPaid;
    private YesOrNo specFullAdmitPaid;

    // dates
    private final LocalDateTime submittedDate;
    private final LocalDateTime paymentSuccessfulDate;
    private final LocalDate issueDate;
    private final LocalDateTime claimNotificationDeadline;
    private final LocalDateTime claimNotificationDate;
    private final LocalDateTime claimDetailsNotificationDeadline;
    private final LocalDateTime claimDetailsNotificationDate;
    private final LocalDateTime respondent1ResponseDeadline;
    private final LocalDateTime respondent2ResponseDeadline;
    private final LocalDateTime claimDismissedDeadline;
    private final LocalDateTime respondent1TimeExtensionDate;
    private final LocalDateTime respondent2TimeExtensionDate;
    private final LocalDateTime respondent1AcknowledgeNotificationDate;
    private final LocalDateTime respondent2AcknowledgeNotificationDate;
    private final LocalDateTime respondent1ResponseDate;
    private final LocalDateTime respondent2ResponseDate;
    private final LocalDateTime applicant1ResponseDeadline;
    private final LocalDateTime applicant1ResponseDate;
    private final LocalDateTime applicant2ResponseDate;
    private final LocalDateTime takenOfflineDate;
    private final LocalDateTime takenOfflineByStaffDate;
    private final LocalDateTime unsuitableSDODate;
    private final OtherDetails  otherDetails;
    private final LocalDateTime claimDismissedDate;
    private final String claimAmountBreakupSummaryObject;
    private final LocalDateTime respondent1LitigationFriendDate;
    private final LocalDateTime respondent2LitigationFriendDate;
    private final String paymentTypePBA;
    private final String paymentTypePBASpec;
    private final String whenToBePaidText;

    private final LocalDateTime respondent1LitigationFriendCreatedDate;
    private final LocalDateTime respondent2LitigationFriendCreatedDate;

    @Builder.Default
    private final List<IdValue<Bundle>> caseBundles = new ArrayList<>();

    private final Respondent1DebtLRspec specDefendant1Debts;
    private final Respondent1SelfEmploymentLRspec specDefendant1SelfEmploymentDetails;
    private final String detailsOfDirection;

    private final HearingSupportRequirementsDJ hearingSupportRequirementsDJ;
    private final CaseLocationCivil caseManagementLocation;
    private final CaseManagementCategory caseManagementCategory;
    private final String locationName;
    private final DynamicList defendantDetailsSpec;
    private final DynamicList defendantDetails;
    private final String bothDefendants;
    private final String bothDefendantsSpec;
    private final String partialPaymentAmount;
    private final YesOrNo partialPayment;
    private final LocalDate paymentSetDate;
    private final String repaymentSummaryObject;
    private final YesOrNo paymentConfirmationDecisionSpec;
    private final String repaymentDue;
    private final String repaymentSuggestion;
    private final String currentDatebox;
    private final LocalDate repaymentDate;
    private final String caseNameHmctsInternal;
    private final String caseNamePublic;

    @Builder.Default
    private final List<Element<CaseDocument>> defaultJudgmentDocuments = new ArrayList<>();

    private final String hearingSelection;

    private final YesOrNo isRespondent1;
    private final YesOrNo isRespondent2;
    private final YesOrNo isApplicant1;
    private final YesOrNo disabilityPremiumPayments;
    private final YesOrNo severeDisabilityPremiumPayments;

    private final String currentDefendant;
    private final YesOrNo claimStarted;
    private final String currentDefendantName;

    @JsonUnwrapped
    private final BreathingSpaceInfo breathing;
    private final String applicantVRespondentText;

    //default judgement SDO fields for disposal
    private DisposalHearingJudgesRecitalDJ disposalHearingJudgesRecitalDJ;
    private DisposalHearingDisclosureOfDocumentsDJ disposalHearingDisclosureOfDocumentsDJ;
    private DisposalHearingWitnessOfFactDJ disposalHearingWitnessOfFactDJ;
    private DisposalHearingMedicalEvidenceDJ disposalHearingMedicalEvidenceDJ;
    private DisposalHearingQuestionsToExpertsDJ disposalHearingQuestionsToExpertsDJ;
    private DisposalHearingSchedulesOfLossDJ disposalHearingSchedulesOfLossDJ;
    private DisposalHearingFinalDisposalHearingDJ disposalHearingFinalDisposalHearingDJ;
    private DisposalHearingFinalDisposalHearingTimeDJ disposalHearingFinalDisposalHearingTimeDJ;
    private DisposalHearingNotesDJ disposalHearingNotesDJ;
    private DisposalHearingHearingNotesDJ disposalHearingHearingNotesDJ;
    private DisposalHearingOrderMadeWithoutHearingDJ disposalHearingOrderMadeWithoutHearingDJ;
    private DisposalHearingMethodDJ disposalHearingMethodDJ;
    private DynamicList trialHearingMethodInPersonDJ;
    private DynamicList disposalHearingMethodInPersonDJ;
    private final DynamicList hearingMethodValuesDisposalHearingDJ;
    private final DynamicList hearingMethodValuesTrialHearingDJ;
    private List<Element<DisposalHearingAddNewDirectionsDJ>> disposalHearingAddNewDirectionsDJ;
    private List<Element<TrialHearingAddNewDirectionsDJ>> trialHearingAddNewDirectionsDJ;
    private HearingMethodTelephoneHearingDJ disposalHearingMethodTelephoneHearingDJ;
    private HearingMethodVideoConferenceDJ disposalHearingMethodVideoConferenceHearingDJ;

    private String caseManagementOrderSelection;
    private Document orderSDODocumentDJ;

    @Builder.Default
    private final List<Element<CaseDocument>> orderSDODocumentDJCollection = new ArrayList<>();

    /**
     * RTJ = Refer To Judge.
     */
    private final String eventDescriptionRTJ;
    /**
     * RTJ = Refer To Judge.
     */
    private final String additionalInformationRTJ;

    //general application order documents
    private final List<Element<CaseDocument>> generalOrderDocument;
    private final List<Element<CaseDocument>> generalOrderDocStaff;
    private final List<Element<CaseDocument>> generalOrderDocClaimant;
    private final List<Element<CaseDocument>> generalOrderDocRespondentSol;
    private final List<Element<CaseDocument>> generalOrderDocRespondentSolTwo;

    private final List<Element<CaseDocument>> consentOrderDocument;
    private final List<Element<CaseDocument>> consentOrderDocStaff;
    private final List<Element<CaseDocument>> consentOrderDocClaimant;
    private final List<Element<CaseDocument>> consentOrderDocRespondentSol;
    private final List<Element<CaseDocument>> consentOrderDocRespondentSolTwo;

    private final List<Element<Document>> generalAppEvidenceDocument;

    private final List<Element<Document>> gaEvidenceDocStaff;
    private final List<Element<Document>> gaEvidenceDocClaimant;
    private final List<Element<Document>> gaEvidenceDocRespondentSol;
    private final List<Element<Document>> gaEvidenceDocRespondentSolTwo;

    private final List<Element<CaseDocument>> gaRespondDoc;

    @Builder.Default
    private final List<Element<CaseDocument>> hearingDocuments = new ArrayList<>();

    //case progression
    private final List<Element<DocumentWithName>> documentAndName;
    private final List<Element<DocumentAndNote>> documentAndNote;
    private final CaseNoteType caseNoteType;
    private final String caseNoteTypeNoteTA;
    private final LocalDateTime noteAdditionDateTime;
    private final String caseTypeFlag;
    private final String witnessStatementFlag;
    private final String witnessSummaryFlag;
    private final String witnessReferredStatementFlag;
    private final String expertReportFlag;
    private final String expertJointFlag;
    private final String trialAuthorityFlag;
    private final String trialCostsFlag;
    private final String trialDocumentaryFlag;

    private final YesOrNo urgentFlag;
    private final String caseProgAllocatedTrack;
    private final DynamicList evidenceUploadOptions;

    private final List<Element<RegistrationInformation>> registrationTypeRespondentOne;
    private final List<Element<RegistrationInformation>> registrationTypeRespondentTwo;

    private final String respondent1DocumentURL;
    private final String respondent2DocumentURL;
    private final String respondent2DocumentGeneration;

    // judge final orders
    private final FinalOrderSelection finalOrderSelection;
    private final String freeFormRecordedTextArea;
    private final String freeFormOrderedTextArea;
    private final FreeFormOrderValues orderOnCourtInitiative;
    private final FreeFormOrderValues orderWithoutNotice;
    private final OrderOnCourtsList orderOnCourtsList;

    private Document finalOrderDocument;
    @Builder.Default
    private final List<Element<CaseDocument>> finalOrderDocumentCollection = new ArrayList<>();

    // bulk claims
    private final String bulkCustomerId;
    private final String sdtRequestIdFromSdt;
    private final List<Element<String>> sdtRequestId;

    //Judgments Online
    private JudgmentRecordedReason joJudgmentRecordReason;
    private JudgmentStatusDetails joJudgmentStatusDetails;
    private LocalDate joOrderMadeDate;
    private String joAmountOrdered;
    private String joAmountCostOrdered;
    private YesOrNo joIsRegisteredWithRTL;
    private PaymentPlanSelection joPaymentPlanSelection;
    private JudgmentInstalmentDetails joJudgmentInstalmentDetails;
    private LocalDate joPaymentToBeMadeByDate;
    private YesOrNo joIsLiveJudgmentExists;
    private LocalDate joSetAsideDate;
    private JudgmentPaidInFull joJudgmentPaidInFull;

    /**
     * There are several fields that can hold the I2P of applicant1 depending
     * on multiparty scenario, which complicates all conditions depending on it.
     * This method tries to simplify those conditions since only one field will be
     * meaningful for that.
     *
     * @return value set among the fields that hold the I2P of applicant1
     */
    @JsonIgnore
    public YesOrNo getApplicant1ProceedsWithClaimSpec() {
        return Stream.of(
                applicant1ProceedWithClaim,
                getApplicant1ProceedWithClaimSpec2v1()
            )
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    public YesOrNo getRespondent1Represented() {
        return Stream.of(
                respondent1Represented,
                specRespondent1Represented
            )
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    @JsonIgnore
    public boolean isRespondent1LiP() {
        return YesOrNo.NO == getRespondent1Represented();
    }

    public YesOrNo getRespondent2Represented() {
        return Stream.of(
                respondent2Represented,
                specRespondent2Represented
            )
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    @JsonIgnore
    public boolean isRespondent2LiP() {
        return YesOrNo.NO == getRespondent2Represented();
    }

    @JsonIgnore
    public boolean respondent1PaidInFull() {
        return respondent1ClaimResponsePaymentAdmissionForSpec
            == RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT;
    }

    @JsonIgnore
    public boolean isPayBySetDate() {
        return defenceAdmitPartPaymentTimeRouteRequired != null
            && defenceAdmitPartPaymentTimeRouteRequired == RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
    }

    @JsonIgnore
    public boolean isPayByInstallment() {
        return defenceAdmitPartPaymentTimeRouteRequired != null
            && defenceAdmitPartPaymentTimeRouteRequired
            == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
    }

    @JsonIgnore
    public boolean isPayImmediately() {
        return RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY.equals(getDefenceAdmitPartPaymentTimeRouteRequired());
    }

    @JsonIgnore
    public boolean hasDefendantPayedTheAmountClaimed() {
        return SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED
            .equals(getDefenceRouteRequired());
    }

    @JsonIgnore
    public boolean isClaimBeingDisputed() {
        return SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM
            .equals(getDefenceRouteRequired());
    }

    @JsonIgnore
    public LocalDate getDateForRepayment() {
        return Optional.ofNullable(respondToClaimAdmitPartLRspec)
            .map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid).orElse(null);
    }

    @JsonIgnore
    public boolean hasBreathingSpace() {
        return getBreathing() != null
            && getBreathing().getEnter() != null
            && getBreathing().getLift() == null;
    }

    @JsonIgnore
    public boolean isDefendantPaymentPlanYes() {
        Set<RespondentResponsePartAdmissionPaymentTimeLRspec> paymentPlan = EnumSet.of(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE
        );

        return hasApplicantAcceptedRepaymentPlan()
            || !paymentPlan.contains(getDefenceAdmitPartPaymentTimeRouteRequired())
            || isClaimantNotSettlePartAdmitClaim();
    }

    @JsonIgnore
    public boolean isDefendantPaymentPlanNo() {
        Set<RespondentResponsePartAdmissionPaymentTimeLRspec> paymentPlan = EnumSet.of(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE
        );

        return hasApplicantRejectedRepaymentPlan()
            || !paymentPlan.contains(getDefenceAdmitPartPaymentTimeRouteRequired())
            || isClaimantNotSettlePartAdmitClaim();
    }

    @JsonIgnore
    public boolean isPartAdmitClaimSettled() {
        return (
            getApplicant1ProceedsWithClaimSpec() == null
                && isPartAdmitClaimSpec()
                && isClaimantIntentionSettlePartAdmit()
                && isClaimantConfirmAmountPaidPartAdmit());
    }

    @JsonIgnore
    public boolean isPartAdmitClaimNotSettled() {
        return (
            getApplicant1ProceedsWithClaimSpec() != null
                || getApplicant1AcceptAdmitAmountPaidSpec() != null
                || isClaimantIntentionNotSettlePartAdmit()
                || isClaimantConfirmAmountNotPaidPartAdmit());
    }

    @JsonIgnore
    public boolean hasDefendantNotPaid() {
        return NO.equals(getApplicant1PartAdmitConfirmAmountPaidSpec());
    }

    @JsonIgnore
    public boolean isSettlementDeclinedByClaimant() {
        return NO.equals(getApplicant1PartAdmitIntentionToSettleClaimSpec());
    }

    @JsonIgnore
    public boolean isClaimantRejectsClaimAmount() {
        return NO.equals(getApplicant1AcceptAdmitAmountPaidSpec());
    }

    @JsonIgnore
    public boolean isFullDefence() {
        return YES.equals(getApplicant1ProceedWithClaim());
    }

    @JsonIgnore
    public boolean hasDefendantAgreedToFreeMediation() {
        return YES.equals(getResponseClaimMediationSpecRequired());
    }

    @JsonIgnore
    public boolean isMultiPartyDefendant() {
        return !YES.equals(getDefendantSingleResponseToBothClaimants())
            && YES.equals(getApplicant1ProceedWithClaim());
    }

    @JsonIgnore
    public boolean isMultiPartyClaimant(MultiPartyScenario multiPartyScenario) {
        return multiPartyScenario.equals(TWO_V_ONE)
            && YES.equals(getDefendantSingleResponseToBothClaimants())
            && YES.equals(getApplicant1ProceedWithClaimSpec2v1());
    }

    @JsonIgnore
    public boolean isPaidSomeAmountMoreThanClaimAmount() {
        return getCcjPaymentDetails().getCcjPaymentPaidSomeAmount() != null
            && getCcjPaymentDetails().getCcjPaymentPaidSomeAmount()
            .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(getTotalClaimAmount()))) > 0;
    }

    @JsonIgnore
    public boolean hasApplicantProceededWithClaim() {
        return YES == getApplicant1ProceedWithClaim()
            || YES == getApplicant1ProceedWithClaimSpec2v1();
    }

    @JsonIgnore
    public boolean isRespondentResponseFullDefence() {
        return (RespondentResponseTypeSpec.FULL_DEFENCE.equals(getRespondent1ClaimResponseTypeForSpec())
            && !isOneVTwoTwoLegalRep(this))
            || (RespondentResponseTypeSpec.FULL_DEFENCE.equals(getRespondent1ClaimResponseTypeForSpec())
            && RespondentResponseTypeSpec.FULL_DEFENCE.equals(getRespondent2ClaimResponseTypeForSpec()));
    }

    @JsonIgnore
    public boolean hasApplicantAcceptedRepaymentPlan() {
        return YES.equals(getApplicant1AcceptFullAdmitPaymentPlanSpec())
            || YES.equals(getApplicant1AcceptPartAdmitPaymentPlanSpec());
    }

    @JsonIgnore
    public boolean hasApplicantRejectedRepaymentPlan() {
        return NO.equals(getApplicant1AcceptFullAdmitPaymentPlanSpec())
            || NO.equals(getApplicant1AcceptPartAdmitPaymentPlanSpec());
    }

    @JsonIgnore
    public boolean isAcceptDefendantPaymentPlanForPartAdmitYes() {
        return YesOrNo.YES.equals(getApplicant1AcceptPartAdmitPaymentPlanSpec());
    }

    @JsonIgnore
    public boolean isRespondent1NotRepresented() {
        return NO.equals(getRespondent1Represented());
    }

    @JsonIgnore
    public boolean isApplicant1NotRepresented() {
        return NO.equals(getApplicant1Represented());
    }

    @JsonIgnore
    public boolean isLRvLipOneVOne() {
        return isRespondent1NotRepresented()
            && !isApplicant1NotRepresented()
            && isOneVOne(this);
    }

    @JsonIgnore
    public boolean isLipvLipOneVOne() {
        return isRespondent1NotRepresented()
            && isApplicant1NotRepresented()
            && isOneVOne(this);
    }

    @JsonIgnore
    public boolean isJudgementDateNotPermitted() {
        return nonNull(getRespondent1ResponseDate())
            && getRespondent1ResponseDate()
            .toLocalDate().plusDays(5).atTime(DeadlinesCalculator.END_OF_BUSINESS_DAY).isAfter(LocalDateTime.now());
    }

    @JsonIgnore
    public String setUpJudgementFormattedPermittedDate() {
        if (isJudgementDateNotPermitted()) {
            return formatLocalDateTime(getRespondent1ResponseDate()
                                           .toLocalDate().plusDays(5).atTime(DeadlinesCalculator.END_OF_BUSINESS_DAY), DATE_TIME_AT);
        }
        return null;
    }

    @JsonIgnore
    public boolean isPartAdmitClaimSpec() {
        return PART_ADMISSION.equals(getRespondent1ClaimResponseTypeForSpec());
    }

    @JsonIgnore
    public boolean isFullAdmitClaimSpec() {
        return FULL_ADMISSION.equals(getRespondent1ClaimResponseTypeForSpec());
    }

    @JsonIgnore
    public boolean isClaimantIntentionSettlePartAdmit() {
        return YesOrNo.YES.equals(getApplicant1PartAdmitIntentionToSettleClaimSpec());
    }

    @JsonIgnore
    public boolean isClaimantIntentionNotSettlePartAdmit() {
        return YesOrNo.NO.equals(getApplicant1PartAdmitIntentionToSettleClaimSpec());
    }

    @JsonIgnore
    public boolean isClaimantConfirmAmountPaidPartAdmit() {
        return YesOrNo.YES.equals(getApplicant1PartAdmitConfirmAmountPaidSpec());
    }

    @JsonIgnore
    public boolean isClaimantConfirmAmountNotPaidPartAdmit() {
        return YesOrNo.NO.equals(getApplicant1PartAdmitConfirmAmountPaidSpec());
    }

    @JsonIgnore
    public YesOrNo doesPartPaymentRejectedOrItsFullDefenceResponse() {
        if (isClaimantNotSettlePartAdmitClaim()
            || (RespondentResponseTypeSpec.FULL_DEFENCE.equals(getRespondent1ClaimResponseTypeForSpec())
            && !(NO.equals(getApplicant1ProceedWithClaim()))
            && !(NO.equals(getApplicant1ProceedWithClaimSpec2v1())))) {
            return YES;
        }
        return NO;
    }

    @JsonIgnore
    public boolean isClaimantNotSettlePartAdmitClaim() {
        return hasDefendantNotPaid()
            || isSettlementDeclinedByClaimant()
            || isClaimantRejectsClaimAmount();
    }

    @JsonIgnore
    public boolean hasDefendantNotAgreedToFreeMediation() {
        return NO.equals(getResponseClaimMediationSpecRequired());
    }

    @JsonIgnore
    public boolean isFastTrackClaim() {
        return FAST_CLAIM.name().equals(getResponseClaimTrack());
    }

    @JsonIgnore
    public boolean isSmallClaim() {
        return SMALL_CLAIM.name().equals(getResponseClaimTrack());
    }

    @JsonIgnore
    public boolean isRejectWithNoMediation() {
        return isClaimantNotSettlePartAdmitClaim()
            && ((hasClaimantNotAgreedToFreeMediation()
            || hasDefendantNotAgreedToFreeMediation())
            || isFastTrackClaim());
    }

    @JsonIgnore
    public String getApplicantOrganisationId() {
        return getOrganisationId(Optional.ofNullable(getApplicant1OrganisationPolicy()));
    }

    @JsonIgnore
    public String getRespondent1OrganisationId() {
        return getOrganisationId(Optional.ofNullable(getRespondent1OrganisationPolicy()));
    }

    @JsonIgnore
    private String getOrganisationId(Optional<OrganisationPolicy> policy) {
        return policy
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .orElse("");
    }

    @JsonIgnore
    public boolean isPartAdmitPayImmediatelyAccepted() {
        return  SPEC_CLAIM.equals(getCaseAccessCategory())
            && YES.equals(getApplicant1AcceptAdmitAmountPaidSpec())
            && getShowResponseOneVOneFlag().equals(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getSDODocument() {
        if (getSystemGeneratedCaseDocuments() != null) {
            return getSystemGeneratedCaseDocuments().stream()
                   .filter(systemGeneratedCaseDocument -> systemGeneratedCaseDocument.getValue()
                   .getDocumentType().equals(DocumentType.SDO_ORDER)).findAny();
        }
        return Optional.empty();
    }

    @JsonIgnore
    public boolean isCcjRequestJudgmentByAdmission() {
        return getCcjPaymentDetails() != null
            && getCcjPaymentDetails().getCcjPaymentPaidSomeOption() != null;
    }

    @JsonIgnore
    public Address getRespondent1CorrespondanceAddress() {
        return Optional.ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1LiPCorrespondenceAddress)
            .orElse(null);
    }

    @JsonIgnore
    public RespondToClaim getResponseToClaim() {
        return getRespondToAdmittedClaim() != null ? getRespondToAdmittedClaim() : getRespondToClaim();
    }

    @JsonIgnore
    public List<Element<RecurringIncomeLRspec>> getRecurringIncomeForRespondent1() {
        if (isFullAdmitClaimSpec()) {
            return Optional.ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringIncomeFA).orElse(
                null);
        }
        return Optional.ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringIncome).orElse(null);
    }

    @JsonIgnore
    public List<Element<RecurringExpenseLRspec>> getRecurringExpensesForRespondent1() {
        if (isFullAdmitClaimSpec()) {
            return Optional.ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringExpensesFA)
                .orElse(
                    null);
        }
        return Optional.ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringExpenses).orElse(
            null);
    }

    @JsonIgnore
    public List<Element<ManageDocument>> getManageDocumentsList() {
        return Optional.ofNullable(getManageDocuments()).orElse(new ArrayList<>());
    }

    @JsonIgnore
    public boolean getApplicant1ResponseDeadlinePassed() {
        return getApplicant1ResponseDeadline() != null
            && getApplicant1ResponseDeadline().isBefore(LocalDateTime.now())
            && getApplicant1ProceedWithClaim() == null;
    }

    @JsonIgnore
    public String getApplicant1Email() {
        return getApplicant1().getPartyEmail() != null ? getApplicant1().getPartyEmail() : getClaimantUserDetails().getEmail();
    }

    @JsonIgnore
    public String getHelpWithFeesReferenceNumber() {
        return Optional.ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getHelpWithFees)
            .map(HelpWithFees::getHelpWithFeesReferenceNumber).orElse(null);
    }

    @JsonIgnore
    public boolean isTranslatedDocumentUploaded() {
        if (getSystemGeneratedCaseDocuments() != null) {
            return getSystemGeneratedCaseDocuments().stream()
                   .filter(systemGeneratedCaseDocument -> systemGeneratedCaseDocument.getValue()
                   .getDocumentType().equals(DocumentType.DEFENCE_TRANSLATED_DOCUMENT)).findAny().isPresent();
        }
        return false;
    }
}
