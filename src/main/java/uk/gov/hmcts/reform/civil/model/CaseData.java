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
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.ClaimTypeUnspec;
import uk.gov.hmcts.reform.civil.enums.ConfirmationToggle;
import uk.gov.hmcts.reform.civil.enums.EmploymentTypeCheckboxFixedListLRspec;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.PersonalInjuryType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeepCollection;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.ExpertRequirements;
import uk.gov.hmcts.reform.civil.model.dq.RecurringExpenseLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationTypeLR;
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
import uk.gov.hmcts.reform.civil.model.mediation.MediationAvailability;
import uk.gov.hmcts.reform.civil.model.mediation.MediationContactInformation;
import uk.gov.hmcts.reform.civil.model.sdo.OtherDetails;
import uk.gov.hmcts.reform.civil.model.taskmanagement.ClientContext;
import uk.gov.hmcts.reform.civil.model.welshenhancements.ChangeLanguagePreference;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreTranslationDocumentType;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.FINISHED;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
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
    private  Long ccdCaseReference;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private  CaseState ccdState;
    private  CaseState previousCCDState;
    private  String preStayState;
    private  String manageStayOption;
    private  LocalDate manageStayUpdateRequestDate;
    private  GAApplicationType generalAppType;
    private  GAApplicationTypeLR generalAppTypeLR;
    private  GARespondentOrderAgreement generalAppRespondentAgreement;
    private  GAPbaDetails generalAppPBADetails;
    private  String generalAppDetailsOfOrder;
    private  List<Element<String>> generalAppDetailsOfOrderColl;
    private  String generalAppReasonsOfOrder;
    private  List<Element<String>> generalAppReasonsOfOrderColl;
    private  YesOrNo generalAppAskForCosts;
    private  GAInformOtherParty generalAppInformOtherParty;
    private  GAUrgencyRequirement generalAppUrgencyRequirement;
    private  GAStatementOfTruth generalAppStatementOfTruth;
    private  GAHearingDetails generalAppHearingDetails;
    private  GASolicitorDetailsGAspec generalAppApplnSolicitor;
    private  SRPbaDetails hearingFeePBADetails;
    private  SRPbaDetails claimIssuedPBADetails;
    private  String applicantPartyName;
    private  CertOfSC certOfSC;
    private  String gaWaTrackLabel;
    private  String nextState;

    private  YesOrNo generalAppVaryJudgementType;
    private  YesOrNo generalAppParentClaimantIsApplicant;
    private  YesOrNo parentClaimantIsApplicant;
    private  GAHearingDateGAspec generalAppHearingDate;
    private  Document generalAppN245FormUpload;
    private  YesOrNo gaEaCourtLocation;

    @Builder.Default
    private  List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors = new ArrayList<>();

    @Builder.Default
    private  List<Element<GeneralApplication>> generalApplications = new ArrayList<>();

    private  List<Element<GeneralApplicationsDetails>> claimantGaAppDetails;
    private  List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection;
    private  List<Element<GeneralApplicationsDetails>> gaDetailsTranslationCollection;
    private  List<Element<GADetailsRespondentSol>> respondentSolGaAppDetails;
    private  List<Element<GADetailsRespondentSol>> respondentSolTwoGaAppDetails;
    private  SolicitorReferences solicitorReferences;
    private  SolicitorReferences solicitorReferencesCopy;
    private  String respondentSolicitor2Reference;
    private  CourtLocation courtLocation;
    private  Party applicant1;
    private  Party applicant2;
    private  CorrectEmail applicantSolicitor1CheckEmail;
    private  IdamUserDetails applicantSolicitor1UserDetails;
    private  YesOrNo addApplicant2;
    private  YesOrNo addRespondent2;
    private  YesOrNo respondent2SameLegalRepresentative;
    private  Party respondent1;
    private  Party respondent1Copy;
    private  Party respondent2;
    private  Party respondent2Copy;
    private  Party respondent1DetailsForClaimDetailsTab;
    private  Party respondent2DetailsForClaimDetailsTab;
    private  YesOrNo respondent1Represented;
    private  YesOrNo respondent2Represented;
    private  YesOrNo respondent1OrgRegistered;
    private  YesOrNo respondent2OrgRegistered;
    private  String respondentSolicitor1EmailAddress;
    private  String respondentSolicitor2EmailAddress;
    private  YesOrNo uploadParticularsOfClaim;
    private  String detailsOfClaim;
    private  ClaimValue claimValue;
    private  Fee claimFee;
    private  String serviceRequestReference;
    private  String paymentReference;
    private  DynamicList applicantSolicitor1PbaAccounts;
    private  ClaimTypeUnspec claimTypeUnSpec;
    private  ClaimType claimType;
    private HelpWithFees generalAppHelpWithFees;
    private  HelpWithFeesDetails claimIssuedHwfDetails;
    private  HelpWithFeesDetails hearingHwfDetails;
    private  FeeType hwfFeeType;
    private  SuperClaimType superClaimType;
    private  String claimTypeOther;
    private  PersonalInjuryType personalInjuryType;
    private  String personalInjuryTypeOther;
    private  StatementOfTruth applicantSolicitor1ClaimStatementOfTruth;
    private  StatementOfTruth uiStatementOfTruth;
    private  StatementOfTruth respondent1LiPStatementOfTruth;
    private  String legacyCaseReference;
    private  AllocatedTrack allocatedTrack;
    private  PaymentDetails paymentDetails;
    private  PaymentDetails claimIssuedPaymentDetails;
    private  PaymentDetails hearingFeePaymentDetails;
    private  OrganisationPolicy applicant1OrganisationPolicy;
    private  OrganisationPolicy applicant2OrganisationPolicy;
    private  OrganisationPolicy respondent1OrganisationPolicy;
    private  OrganisationPolicy respondent2OrganisationPolicy;
    private  SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails;
    private  SolicitorOrganisationDetails respondentSolicitor2OrganisationDetails;
    private  YesOrNo applicantSolicitor1ServiceAddressRequired;
    private  Address applicantSolicitor1ServiceAddress;
    private  YesOrNo respondentSolicitor1ServiceAddressRequired;
    private  Address respondentSolicitor1ServiceAddress;
    private  YesOrNo respondentSolicitor2ServiceAddressRequired;
    private  Address respondentSolicitor2ServiceAddress;
    private  StatementOfTruth applicant1ServiceStatementOfTruthToRespondentSolicitor1;
    private  RespondentSolicitorDetails respondentSolicitorDetails;

    @Builder.Default
    private  List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();

    @Builder.Default
    private  List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
    private  List<Element<ManageDocument>> manageDocuments;
    private  Document specClaimTemplateDocumentFiles;
    private  Document specClaimDetailsDocumentFiles;
    private  List<Evidence> speclistYourEvidenceList;
    private  YesOrNo specApplicantCorrespondenceAddressRequired;
    private  Address specApplicantCorrespondenceAddressdetails;
    private  YesOrNo specRespondentCorrespondenceAddressRequired;
    private  Address specRespondentCorrespondenceAddressdetails;
    private  YesOrNo specAoSRespondent2HomeAddressRequired;
    private  Address specAoSRespondent2HomeAddressDetails;

    private  LocalDate respondentSolicitor1AgreedDeadlineExtension;
    private  LocalDate respondentSolicitor2AgreedDeadlineExtension;
    private  ResponseIntention respondent1ClaimResponseIntentionType;
    private  ResponseIntention respondent2ClaimResponseIntentionType;
    private  ResponseIntention respondent1ClaimResponseIntentionTypeApplicant2;
    private  ServedDocumentFiles servedDocumentFiles;

    private  YesOrNo respondentResponseIsSame;
    private  YesOrNo defendantSingleResponseToBothClaimants;
    private  RespondentResponseType respondent1ClaimResponseType;
    private  RespondentResponseType respondent2ClaimResponseType;
    private  RespondentResponseType respondent1ClaimResponseTypeToApplicant2;
    private  ResponseDocument respondent1ClaimResponseDocument;
    private  ResponseDocument respondent2ClaimResponseDocument;
    private  ResponseDocument respondentSharedClaimResponseDocument;
    private  CaseDocument respondent1GeneratedResponseDocument;
    private  CaseDocument respondent2GeneratedResponseDocument;
    private  LocalDate claimMovedToMediationOn;

    @Builder.Default
    private  List<Element<CaseDocument>> defendantResponseDocuments = new ArrayList<>();

    private  YesOrNo applicant1ProceedWithClaim;
    private  YesOrNo applicant1ProceedWithClaimMultiParty2v1;
    private  YesOrNo applicant2ProceedWithClaimMultiParty2v1;
    private  YesOrNo applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2;
    private  YesOrNo applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2;
    private  YesOrNo applicant1ProceedWithClaimRespondent2;
    private  ResponseDocument applicant1DefenceResponseDocument;
    private  ResponseDocument claimantDefenceResDocToDefendant2;

    @Builder.Default
    private  List<Element<CaseDocument>> claimantResponseDocuments = new ArrayList<>();

    @Builder.Default
    private  List<Element<CaseDocument>> duplicateSystemGeneratedCaseDocs = new ArrayList<>();

    @Builder.Default
    @JsonProperty("duplicateClaimantDefResponseDocs")
    private  List<Element<CaseDocument>> duplicateClaimantDefendantResponseDocs = new ArrayList<>();

    private  List<ClaimAmountBreakup> claimAmountBreakup;
    private  List<TimelineOfEvents> timelineOfEvents;
    /**
     * money amount in pounds.
     */
    private BigDecimal totalClaimAmount;
    private BigDecimal totalInterest;
    private BigDecimal totalClaimAmountPlusInterestAdmitPart;
    private BigDecimal totalClaimAmountPlusInterest;
    private BigDecimal defaultJudgementOverallTotal;
    private String totalClaimAmountPlusInterestAdmitPartString;
    private String totalClaimAmountPlusInterestString;
    private  YesOrNo claimInterest;
    private  InterestClaimOptions interestClaimOptions;
    private  SameRateInterestSelection sameRateInterestSelection;
    private  BigDecimal breakDownInterestTotal;
    private  String breakDownInterestDescription;
    private  InterestClaimFromType interestClaimFrom;
    private  InterestClaimUntilType interestClaimUntil;
    private  LocalDate interestFromSpecificDate;
    private  String interestFromSpecificDateDescription;
    private  String calculatedInterest;
    private  String specRespondentSolicitor1EmailAddress;
    private  YesOrNo specAoSApplicantCorrespondenceAddressRequired;
    private  Address specAoSApplicantCorrespondenceAddressdetails;
    private  YesOrNo specAoSRespondentCorrespondenceAddressRequired;
    private  Address specAoSRespondentCorrespondenceAddressdetails;
    private  YesOrNo specRespondent1Represented;
    private  YesOrNo specRespondent2Represented;
    private  List<TimelineOfEvents> specResponseTimelineOfEvents;
    private  List<TimelineOfEvents> specResponseTimelineOfEvents2;
    private  TimelineUploadTypeSpec specClaimResponseTimelineList;
    private  Document specResponseTimelineDocumentFiles;
    private  List<Evidence> specResponselistYourEvidenceList;
    private  List<Evidence> specResponselistYourEvidenceList2;

    private  String detailsOfWhyDoesYouDisputeTheClaim;
    private  String detailsOfWhyDoesYouDisputeTheClaim2;

    private  ResponseDocument respondent1SpecDefenceResponseDocument;
    private  ResponseDocument respondent2SpecDefenceResponseDocument;

    private  YesOrNo bundleError;
    private  String bundleEvent;
    private  YesOrNo fullAdmitNoPaymentSchedulerProcessed;

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

    private  RespondentResponseTypeSpec respondent1ClaimResponseTypeForSpec;
    private  RespondentResponseTypeSpec respondent2ClaimResponseTypeForSpec;
    private  RespondentResponseTypeSpec claimant1ClaimResponseTypeForSpec;
    private  RespondentResponseTypeSpec claimant2ClaimResponseTypeForSpec;
    private  RespondentResponseTypeSpecPaidStatus respondent1ClaimResponsePaymentAdmissionForSpec;
    private  RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteRequired;
    private  String defenceRouteRequired;
    private  String responseClaimTrack;
    private  RespondToClaim respondToClaim;
    private  RespondToClaim respondToAdmittedClaim;
    /**
     * money amount in pence.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private  BigDecimal respondToAdmittedClaimOwingAmount;
    /**
     * money amount in pounds.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private  BigDecimal respondToAdmittedClaimOwingAmountPounds;
    private  YesOrNo specDefenceFullAdmittedRequired;
    private  PaymentUponCourtOrder respondent1CourtOrderPayment;
    private  RepaymentPlanLRspec respondent1RepaymentPlan;
    private  RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec;
    private  UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec;
    private  Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer;
    private  String responseToClaimAdmitPartWhyNotPayLRspec;
    // Fields related to ROC-9453 & ROC-9455
    private  YesOrNo responseClaimMediationSpecRequired;
    private  SmallClaimMedicalLRspec applicant1ClaimMediationSpecRequired;
    private  YesOrNo defenceAdmitPartEmploymentTypeRequired;
    private  YesOrNo responseClaimExpertSpecRequired;
    private  YesOrNo applicant1ClaimExpertSpecRequired;
    private  String responseClaimWitnesses;
    private  String applicant1ClaimWitnesses;
    private  YesOrNo smallClaimHearingInterpreterRequired;
    private  String smallClaimHearingInterpreterDescription;
    private  List<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspec;
    private  YesOrNo specDefenceAdmittedRequired;

    private  MediationContactInformation app1MediationContactInfo;
    private  MediationAvailability app1MediationAvailability;
    private  MediationContactInformation resp1MediationContactInfo;
    private  MediationContactInformation resp2MediationContactInfo;
    private  MediationAvailability resp1MediationAvailability;
    private  MediationAvailability resp2MediationAvailability;

    private  String additionalInformationForJudge;
    private  String applicantAdditionalInformationForJudge;
    @JsonUnwrapped
    private  ExpertRequirements respondToClaimExperts;

    private  String caseNote;
    private  List<Element<CaseNote>> caseNotes;

    @Valid
    private  CloseClaim withdrawClaim;

    @Valid
    private  CloseClaim discontinueClaim;

    private  BusinessProcess businessProcess;

    @JsonUnwrapped
    private  Respondent1DQ respondent1DQ;

    @JsonUnwrapped
    private  Respondent2DQ respondent2DQ;

    @JsonUnwrapped
    private  Applicant1DQ applicant1DQ;

    @JsonUnwrapped
    private  Applicant2DQ applicant2DQ;

    public boolean hasNoOngoingBusinessProcess() {
        return businessProcess == null
            || businessProcess.getStatus() == null
            || businessProcess.getStatus() == FINISHED;
    }

    private  LitigationFriend genericLitigationFriend;
    private  LitigationFriend respondent1LitigationFriend;
    private  LitigationFriend respondent2LitigationFriend;

    private  YesOrNo applicant1LitigationFriendRequired;
    private  LitigationFriend applicant1LitigationFriend;

    private  YesOrNo applicant2LitigationFriendRequired;
    private  LitigationFriend applicant2LitigationFriend;

    private  DynamicList defendantSolicitorNotifyClaimOptions;
    private  DynamicList defendantSolicitorNotifyClaimDetailsOptions;
    private  DynamicList selectLitigationFriend;
    private  String litigantFriendSelection;
    @Valid
    private  ClaimProceedsInCaseman claimProceedsInCaseman;
    @Valid
    private  ClaimProceedsInCasemanLR claimProceedsInCasemanLR;

    //CCD UI flag
    private  YesOrNo applicantSolicitor1PbaAccountsIsEmpty;
    private MultiPartyResponseTypeFlags multiPartyResponseTypeFlags;
    private YesOrNo applicantsProceedIntention;
    private  MultiPartyScenario claimantResponseScenarioFlag;
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
    private  LocalDateTime submittedDate;
    private  LocalDateTime paymentSuccessfulDate;
    private  LocalDate issueDate;
    private  LocalDateTime claimNotificationDeadline;
    private  LocalDateTime claimNotificationDate;
    private  LocalDateTime claimDetailsNotificationDeadline;
    private  LocalDateTime claimDetailsNotificationDate;
    private  LocalDateTime respondent1ResponseDeadline;
    private  LocalDateTime respondent2ResponseDeadline;
    private  LocalDateTime addLegalRepDeadlineRes1;
    private  LocalDateTime addLegalRepDeadlineRes2;
    private  LocalDateTime claimDismissedDeadline;
    private  LocalDateTime respondent1TimeExtensionDate;
    private  LocalDateTime respondent2TimeExtensionDate;
    private  LocalDateTime respondent1AcknowledgeNotificationDate;
    private  LocalDateTime respondent2AcknowledgeNotificationDate;
    private  LocalDateTime respondent1ResponseDate;
    private  LocalDateTime respondent2ResponseDate;
    private  LocalDateTime applicant1ResponseDeadline;
    private  LocalDateTime applicant1ResponseDate;
    private  LocalDateTime applicant2ResponseDate;
    private  LocalDateTime takenOfflineDate;
    private  LocalDateTime takenOfflineByStaffDate;
    private  LocalDateTime unsuitableSDODate;
    private  OtherDetails otherDetails;
    private  LocalDateTime claimDismissedDate;
    private  String claimAmountBreakupSummaryObject;
    private  LocalDateTime respondent1LitigationFriendDate;
    private  LocalDateTime respondent2LitigationFriendDate;
    private  LocalDateTime respondent1RespondToSettlementAgreementDeadline;
    private  YesOrNo respondent1ResponseDeadlineChecked;
    private  String paymentTypePBA;
    private  String paymentTypePBASpec;
    private  String whenToBePaidText;

    private  LocalDateTime respondent1LitigationFriendCreatedDate;
    private  LocalDateTime respondent2LitigationFriendCreatedDate;

    @Builder.Default
    private  List<IdValue<Bundle>> caseBundles = new ArrayList<>();

    private  Respondent1DebtLRspec specDefendant1Debts;
    private  Respondent1SelfEmploymentLRspec specDefendant1SelfEmploymentDetails;
    private  String detailsOfDirection;

    private  HearingSupportRequirementsDJ hearingSupportRequirementsDJ;
    private  CaseLocationCivil caseManagementLocation;
    private  CaseManagementCategory caseManagementCategory;
    private  String locationName;
    private  DynamicList defendantDetailsSpec;
    private  DynamicList defendantDetails;
    private  String bothDefendants;
    private  String bothDefendantsSpec;
    private  String partialPaymentAmount;
    private  YesOrNo partialPayment;
    private  LocalDate paymentSetDate;
    private  String repaymentSummaryObject;
    private  YesOrNo paymentConfirmationDecisionSpec;
    private  String repaymentDue;
    private  String repaymentSuggestion;
    private  String currentDatebox;
    private  LocalDate repaymentDate;
    private  String caseNameHmctsInternal;
    private  String caseNamePublic;
    private  YesOrNo ccjJudgmentAmountShowInterest;
    private  YesOrNo claimFixedCostsExist;
    private  YesOrNo partAdmit1v1Defendant;

    @Builder.Default
    private  List<Element<CaseDocument>> defaultJudgmentDocuments = new ArrayList<>();

    private  String hearingSelection;

    private  YesOrNo isRespondent1;
    private  YesOrNo isRespondent2;
    private  YesOrNo isApplicant1;
    private  YesOrNo disabilityPremiumPayments;
    private  YesOrNo severeDisabilityPremiumPayments;

    private  String currentDefendant;
    private  YesOrNo claimStarted;
    private  String currentDefendantName;

    @JsonUnwrapped
    private  BreathingSpaceInfo breathing;
    private  String applicantVRespondentText;

    private YesOrNo setRequestDJDamagesFlagForWA;
    private String featureToggleWA;

    private ContactDetailsUpdatedEvent contactDetailsUpdatedEvent;

    /**
     * RTJ = Refer To Judge.
     */
    private  String eventDescriptionRTJ;
    /**
     * RTJ = Refer To Judge.
     */
    private  String additionalInformationRTJ;
    /**
     * Refer To Judge(Defence received in time).
     */
    private List<ConfirmationToggle> confirmReferToJudgeDefenceReceived;

    //general application order documents
    private  List<Element<CaseDocument>> generalOrderDocument;
    private  List<Element<CaseDocument>> generalOrderDocStaff;
    private  List<Element<CaseDocument>> generalOrderDocClaimant;
    private  List<Element<CaseDocument>> generalOrderDocRespondentSol;
    private  List<Element<CaseDocument>> generalOrderDocRespondentSolTwo;

    private  List<Element<CaseDocument>> consentOrderDocument;
    private  List<Element<CaseDocument>> consentOrderDocStaff;
    private  List<Element<CaseDocument>> consentOrderDocClaimant;
    private  List<Element<CaseDocument>> consentOrderDocRespondentSol;
    private  List<Element<CaseDocument>> consentOrderDocRespondentSolTwo;

    private  List<Element<Document>> generalAppEvidenceDocument;

    private  List<Element<Document>> gaEvidenceDocStaff;
    private  List<Element<Document>> gaEvidenceDocClaimant;
    private  List<Element<Document>> gaEvidenceDocRespondentSol;
    private  List<Element<Document>> gaEvidenceDocRespondentSolTwo;
    private  List<Element<CaseDocument>> gaAddlDoc;
    private  List<Element<CaseDocument>> gaAddlDocStaff;
    private  List<Element<CaseDocument>> gaAddlDocClaimant;
    private  List<Element<CaseDocument>> gaAddlDocRespondentSol;
    private  List<Element<CaseDocument>> gaAddlDocRespondentSolTwo;
    private  List<Element<CaseDocument>> gaAddlDocBundle;
    private  List<Element<CaseDocument>> gaDraftDocument;
    private  List<Element<CaseDocument>> gaDraftDocStaff;
    private  List<Element<CaseDocument>> gaDraftDocClaimant;
    private  List<Element<CaseDocument>> gaDraftDocRespondentSol;
    private  List<Element<CaseDocument>> gaDraftDocRespondentSolTwo;

    private  List<Element<CaseDocument>> gaRespondDoc;
    private  List<Element<CaseDocument>> preTranslationGaDocsApplicant;
    private  List<Element<CaseDocument>> preTranslationGaDocsRespondent;
    @Builder.Default
    private  List<Element<CaseDocument>> hearingDocuments = new ArrayList<>();

    @Builder.Default
    private  List<Element<CaseDocument>> hearingDocumentsWelsh = new ArrayList<>();

    // GA for LIP
    private  YesOrNo isGaApplicantLip;
    private  YesOrNo isGaRespondentOneLip;
    private  YesOrNo isGaRespondentTwoLip;

    private List<DocumentToKeepCollection> documentToKeepCollection;

    private RequestedCourtForTabDetails requestedCourtForTabDetailsApp;
    private RequestedCourtForTabDetails requestedCourtForTabDetailsRes1;
    private RequestedCourtForTabDetails requestedCourtForTabDetailsRes2;

    private  ChangeLanguagePreference changeLanguagePreference;
    private  PreferredLanguage claimantLanguagePreferenceDisplay;
    private  PreferredLanguage defendantLanguagePreferenceDisplay;

    @Builder.Default
    private  List<Element<CaseDocument>> queryDocuments = new ArrayList<>();

    private  PreTranslationDocumentType preTranslationDocumentType;
    private  YesOrNo bilingualHint;
    private  CaseDocument respondent1OriginalDqDoc;

    private  YesOrNo isMintiLipCase;

    @Builder.Default
    private  List<Element<CaseDocument>> courtOfficersOrders = new ArrayList<>();
    private  YesOrNo isReferToJudgeClaim;

    private  ClientContext clientContext;

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
        return YesOrNo.NO.equals(getRespondent1Represented());
    }

    @JsonIgnore
    public boolean isApplicantLiP() {
        return YesOrNo.NO == getApplicant1Represented();
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
    public boolean hasDefendantPaidTheAmountClaimed() {
        return SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED
            .equals(getDefenceRouteRequired());
    }

    @JsonIgnore
    public boolean isPaidFullAmount() {
        RespondToClaim localRespondToClaim = null;
        if (getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE) {
            localRespondToClaim = getRespondToClaim();
        } else if (getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION) {
            localRespondToClaim = getRespondToAdmittedClaim();
        }

        return ofNullable(localRespondToClaim)
            .map(RespondToClaim::getHowMuchWasPaid)
            .map(amount -> MonetaryConversions.penniesToPounds(amount).compareTo(totalClaimAmount) >= 0)
            .orElse(false);
    }

    @JsonIgnore
    public boolean isClaimBeingDisputed() {
        return SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM
            .equals(getDefenceRouteRequired());
    }

    @JsonIgnore
    public LocalDate getDateForRepayment() {
        return ofNullable(respondToClaimAdmitPartLRspec)
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
    public boolean isClaimantAcceptedClaimAmount() {
        return YES.equals(getApplicant1AcceptAdmitAmountPaidSpec());
    }

    @JsonIgnore
    public boolean isClaimantRejectsClaimAmount() {
        return NO.equals(getApplicant1AcceptAdmitAmountPaidSpec());
    }

    @JsonIgnore
    public boolean isPartAdmitAlreadyPaid() {
        return YES.equals(getSpecDefenceAdmittedRequired());
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
    public boolean hasDefendant2AgreedToFreeMediation() {
        return YES.equals(getResponseClaimMediationSpec2Required());
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
            || YES == getApplicant1ProceedWithClaimSpec2v1()
            || NO.equals(getApplicant1AcceptAdmitAmountPaidSpec())
            || NO.equals(getApplicant1PartAdmitConfirmAmountPaidSpec())
            || NO.equals(getApplicant1PartAdmitIntentionToSettleClaimSpec());
    }

    @JsonIgnore
    public boolean hasApplicantNotProceededWithClaim() {
        return Objects.nonNull(getApplicant1ProceedWithClaim()) && NO == getApplicant1ProceedWithClaim();
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
    public boolean isRespondent2NotRepresented() {
        return NO.equals(Stream.of(
                respondent2Represented,
                specRespondent2Represented
            )
            .filter(Objects::nonNull)
            .findFirst().orElse(null));
    }

    @JsonIgnore
    public boolean isApplicant1NotRepresented() {
        return NO.equals(getApplicant1Represented());
    }

    @JsonIgnore
    public boolean isLRvLipOneVOne() {
        return isRespondent1LiP()
            && !isApplicant1NotRepresented()
            && isOneVOne(this);
    }

    @JsonIgnore
    public boolean isLipvLipOneVOne() {
        return isRespondent1LiP()
            && isApplicant1NotRepresented()
            && isOneVOne(this);
    }

    @JsonIgnore
    public boolean isApplicantLipOneVOne() {
        return isApplicant1NotRepresented() && isOneVOne(this);
    }

    @JsonIgnore
    public boolean isJudgementDateNotPermitted() {
        LocalDate whenWillThisAmountBePaid = null;
        LocalDate firstRepaymentDate;
        if (hasApplicant1CourtDecisionInFavourOfClaimant()) {
            if (applicant1SuggestedPayImmediately()) {
                whenWillThisAmountBePaid = getApplicant1SuggestPayImmediatelyPaymentDateForDefendantSpec();
            } else if (applicant1SuggestedPayBySetDate()) {
                whenWillThisAmountBePaid = ofNullable(getApplicant1RequestedPaymentDateForDefendantSpec()).map(
                    PaymentBySetDate::getPaymentSetDate).orElse(null);
            }
            firstRepaymentDate = getApplicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec();
        } else {
            whenWillThisAmountBePaid =
                ofNullable(getRespondToClaimAdmitPartLRspec()).map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid).orElse(
                    null);
            firstRepaymentDate = ofNullable(getRespondent1RepaymentPlan()).map(RepaymentPlanLRspec::getFirstRepaymentDate).orElse(
                null);
        }
        LocalDate respondentSettlementAgreementDeadline = ofNullable(
            getRespondent1RespondToSettlementAgreementDeadline()).map(LocalDateTime::toLocalDate).orElse(null);
        Optional<CaseDataLiP> optionalCaseDataLiP = ofNullable(getCaseDataLiP());
        YesOrNo hasDoneSettlementAgreement = optionalCaseDataLiP.map(CaseDataLiP::getRespondentSignSettlementAgreement).orElse(
            null);
        boolean hasDoneSettlementAgreementInTime = (nonNull(hasDoneSettlementAgreement) && hasDoneSettlementAgreement == YesOrNo.YES)
            || (isNull(hasDoneSettlementAgreement) && isDateAfterToday(respondentSettlementAgreementDeadline));

        return (isNull(whenWillThisAmountBePaid) && isNull(firstRepaymentDate))
            || (isDateAfterToday(whenWillThisAmountBePaid) && hasDoneSettlementAgreementInTime)
            || (isDateAfterToday(firstRepaymentDate) && hasDoneSettlementAgreementInTime)
            || (isDateAfterToday(whenWillThisAmountBePaid) && isFullAdmitPayImmediatelyClaimSpec());

    }

    @JsonIgnore
    public String getFormattedJudgementPermittedDate(LocalDate extendedRespondent1ResponseDate) {
        return formatLocalDateTime(
            extendedRespondent1ResponseDate.atTime(DeadlinesCalculator.END_OF_BUSINESS_DAY),
            DATE_TIME_AT
        );
    }

    @JsonIgnore
    public boolean isPartAdmitClaimSpec() {
        return PART_ADMISSION.equals(getRespondent1ClaimResponseTypeForSpec());
    }

    @JsonIgnore
    public boolean isPartAdmitImmediatePaymentClaimSettled() {
        return (isPartAdmitClaimSpec()
            && (Objects.nonNull(getApplicant1AcceptAdmitAmountPaidSpec())
            && YesOrNo.YES.equals(getApplicant1AcceptAdmitAmountPaidSpec()))
            && (Objects.isNull(getApplicant1AcceptPartAdmitPaymentPlanSpec())));
    }

    @JsonIgnore
    public boolean isFullAdmitClaimSpec() {
        return FULL_ADMISSION.equals(getRespondent1ClaimResponseTypeForSpec());
    }

    @JsonIgnore
    public boolean isFullAdmitPayImmediatelyClaimSpec() {
        return isFullAdmitClaimSpec() && isPayImmediately();
    }

    @JsonIgnore
    public boolean isPartAdmitPayImmediatelyClaimSpec() {
        return isPartAdmitClaimSpec() && isPayImmediately();
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
            && isMediationRejectedOrFastTrack();
    }

    @JsonIgnore
    public boolean isMediationRejectedOrFastTrack() {
        return ((hasClaimantNotAgreedToFreeMediation()
            || hasDefendantNotAgreedToFreeMediation())
            || isFastTrackClaim());
    }

    @JsonIgnore
    public String getApplicantOrganisationId() {
        return getOrganisationId(ofNullable(getApplicant1OrganisationPolicy()));
    }

    @JsonIgnore
    public String getRespondent1OrganisationId() {
        return getOrganisationId(ofNullable(getRespondent1OrganisationPolicy()));
    }

    @JsonIgnore
    public String getRespondent2OrganisationId() {
        return getOrganisationId(ofNullable(getRespondent2OrganisationPolicy()));
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
        return SPEC_CLAIM.equals(getCaseAccessCategory())
            && YES.equals(getApplicant1AcceptAdmitAmountPaidSpec())
            && (getShowResponseOneVOneFlag() != null
            && getShowResponseOneVOneFlag().equals(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY));
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getSDODocument() {
        return getLatestDocumentOfType(DocumentType.SDO_ORDER);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getHiddenSDODocument() {
        return getLatestHiddenDocumentOfType(DocumentType.SDO_ORDER);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getTranslatedSDODocument() {
        return getLatestDocumentOfType(DocumentType.SDO_TRANSLATED_DOCUMENT);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getDecisionReconsiderationDocument() {
        return getLatestDocumentOfType(DocumentType.DECISION_MADE_ON_APPLICATIONS);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getTranslatedDecisionReconsiderationDocument() {
        return getLatestDocumentOfType(DocumentType.DECISION_MADE_ON_APPLICATIONS_TRANSLATED);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getLatestDocumentOfType(DocumentType documentType) {
        return ofNullable(systemGeneratedCaseDocuments)
            .flatMap(docs -> docs.stream()
                .filter(doc -> doc.getValue().getDocumentType().equals(documentType))
                .max(Comparator.comparing(doc -> doc.getValue().getCreatedDatetime())));
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getLatestHiddenDocumentOfType(DocumentType documentType) {
        return ofNullable(preTranslationDocuments)
            .flatMap(docs -> docs.stream()
                .filter(doc -> doc.getValue().getDocumentType().equals(documentType))
                .max(Comparator.comparing(doc -> doc.getValue().getCreatedDatetime())));
    }

    @JsonIgnore
    public Optional<List<CaseDocument>> getDocumentListByType(List<Element<CaseDocument>> documentCollection, DocumentType documentType) {
        List<CaseDocument> documents = documentCollection.stream()
            .map(Element::getValue)
            .filter(doc -> doc.getDocumentType().equals(documentType))
            .toList();
        return ofNullable(documents.isEmpty() ? null : documents);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getDecisionOnReconsiderationDocumentFromList() {
        if (getSystemGeneratedCaseDocuments() != null) {
            return getSystemGeneratedCaseDocuments().stream()
                .filter(systemGeneratedCaseDocument -> systemGeneratedCaseDocument.getValue()
                    .getDocumentType().equals(DocumentType.DECISION_MADE_ON_APPLICATIONS)).findAny();
        }
        return empty();
    }

    @JsonIgnore
    public boolean isCcjRequestJudgmentByAdmission() {
        return getCcjPaymentDetails() != null
            && getCcjPaymentDetails().getCcjPaymentPaidSomeOption() != null;
    }

    @JsonIgnore
    public boolean hasCoscCert() {
        if (getSystemGeneratedCaseDocuments() != null) {
            return getSystemGeneratedCaseDocuments().stream()
                .filter(systemGeneratedCaseDocument -> systemGeneratedCaseDocument.getValue()
                    .getDocumentType().equals(DocumentType.CERTIFICATE_OF_DEBT_PAYMENT)).findAny().isPresent();
        }
        return false;
    }

    @JsonIgnore
    public Address getRespondent1CorrespondanceAddress() {
        return ofNullable(getCaseDataLiP())
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
            return ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringIncomeFA).orElse(
                null);
        }
        return ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringIncome).orElse(null);
    }

    @JsonIgnore
    public List<Element<RecurringExpenseLRspec>> getRecurringExpensesForRespondent1() {
        if (isFullAdmitClaimSpec()) {
            return ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringExpensesFA)
                .orElse(
                    null);
        }
        return ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringExpenses).orElse(
            null);
    }

    @JsonIgnore
    public List<Element<ManageDocument>> getManageDocumentsList() {
        return ofNullable(getManageDocuments()).orElse(new ArrayList<>());
    }

    @JsonIgnore
    public boolean getApplicant1ResponseDeadlinePassed() {
        return getApplicant1ResponseDeadline() != null
            && getApplicant1ResponseDeadline().isBefore(LocalDateTime.now())
            && getApplicant1ProceedWithClaim() == null;
    }

    @JsonIgnore
    public String getApplicant1Email() {
        return ofNullable(getApplicant1().getPartyEmail())
            .or(() -> ofNullable(getClaimantUserDetails())
                .map(IdamUserDetails::getEmail))
            .or(() -> ofNullable(getApplicantSolicitor1UserDetails())
                .map(IdamUserDetails::getEmail))
            .orElse(null);
    }

    @JsonIgnore
    public String getHelpWithFeesReferenceNumber() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getHelpWithFees)
            .map(HelpWithFees::getHelpWithFeesReferenceNumber).orElse(null);
    }

    @JsonIgnore
    public boolean isHelpWithFees() {
        return getCaseDataLiP() != null && getCaseDataLiP().getHelpWithFees() != null
            && YES.equals(getCaseDataLiP().getHelpWithFees().getHelpWithFee());
    }

    @JsonIgnore
    public Address getRespondent1CorrespondenceAddress() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1LiPCorrespondenceAddress)
            .orElse(null);
    }

    @JsonIgnore
    public String getCurrentCamundaBusinessProcessName() {
        return ofNullable(getBusinessProcess())
            .map(BusinessProcess::getCamundaEvent)
            .orElse(null);
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

    @JsonIgnore
    public boolean isRespondentSolicitorRegistered() {
        return YesOrNo.YES.equals(getRespondent1OrgRegistered());
    }

    @JsonIgnore
    public boolean isRespondentTwoSolicitorRegistered() {
        return YesOrNo.YES.equals(getRespondent2OrgRegistered());
    }

    @JsonIgnore
    public String getRespondent1Email() {
        if (isRespondent1NotRepresented()) {
            return getRespondent1().getPartyEmail();
        }
        if (isRespondentSolicitorRegistered()) {
            return getRespondentSolicitor1EmailAddress();
        }
        return null;
    }

    @JsonIgnore
    public boolean isRespondentRespondedToSettlementAgreement() {
        return getCaseDataLiP() != null && getCaseDataLiP().getRespondentSignSettlementAgreement() != null;
    }

    @JsonIgnore
    public boolean isRespondentSignedSettlementAgreement() {
        return getCaseDataLiP() != null && YesOrNo.YES.equals(getCaseDataLiP().getRespondentSignSettlementAgreement());
    }

    @JsonIgnore
    public boolean isRespondentRejectedSettlementAgreement() {
        return getCaseDataLiP() != null && YesOrNo.NO.equals(getCaseDataLiP().getRespondentSignSettlementAgreement());
    }

    @JsonIgnore
    public List<ClaimAmountBreakupDetails> getClaimAmountBreakupDetails() {
        return ofNullable(getClaimAmountBreakup())
            .map(Collection::stream)
            .map(claimAmountBreakupStream -> claimAmountBreakupStream
                .map(item -> new ClaimAmountBreakupDetails(
                    MonetaryConversions.penniesToPounds(item.getValue().getClaimAmount()),
                    item.getValue().getClaimReason()
                ))
                .toList())
            .orElse(Collections.emptyList());

    }

    @JsonIgnore
    public BigDecimal getCalculatedClaimFeeInPence() {
        return ofNullable(getClaimFee())
            .map(Fee::getCalculatedAmountInPence)
            .orElse(BigDecimal.ZERO);
    }

    @JsonIgnore
    public BigDecimal getCalculatedHearingFeeInPence() {
        return ofNullable(getHearingFee())
            .map(Fee::getCalculatedAmountInPence)
            .orElse(BigDecimal.ZERO);
    }

    @JsonIgnore
    public BigDecimal getClaimAmountInPounds() {
        if (nonNull(getClaimValue())) {
            return getClaimValue().toPounds();
        } else if (nonNull(getTotalInterest())) {
            return getTotalClaimAmount()
                .add(getTotalInterest())
                .setScale(2, RoundingMode.UNNECESSARY);
        } else if (nonNull(getTotalClaimAmount())) {
            return getTotalClaimAmount().setScale(2, RoundingMode.UNNECESSARY);
        }
        return null;
    }

    @JsonIgnore
    public BigDecimal getClaimIssueRemissionAmount() {
        return ofNullable(getClaimIssuedHwfDetails())
            .map(HelpWithFeesDetails::getRemissionAmount)
            .orElse(BigDecimal.ZERO);
    }

    @JsonIgnore
    public BigDecimal getHearingRemissionAmount() {
        return ofNullable(getHearingHwfDetails())
            .map(HelpWithFeesDetails::getRemissionAmount)
            .orElse(BigDecimal.ZERO);
    }

    public boolean hasApplicant1SignedSettlementAgreement() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasApplicant1SignedSettlementAgreement).isPresent();

    }

    @JsonIgnore
    public boolean isHWFTypeHearing() {
        return getHwfFeeType() == FeeType.HEARING;
    }

    @JsonIgnore
    public boolean isHWFTypeClaimIssued() {
        return getHwfFeeType() == FeeType.CLAIMISSUED;
    }

    @JsonIgnore
    public CaseEvent getHwFEvent() {
        if (this.isHWFTypeHearing() && this.getHearingHwfDetails() != null) {
            return this.getHearingHwfDetails().getHwfCaseEvent();
        }
        if (this.isHWFTypeClaimIssued() && this.getClaimIssuedHwfDetails() != null) {
            return this.getClaimIssuedHwfDetails().getHwfCaseEvent();
        }
        return null;
    }

    @JsonIgnore
    public boolean isHWFOutcomeReady() {
        return (this.getCcdState() == CaseState.PENDING_CASE_ISSUED && this.isHWFTypeClaimIssued())
            || (this.getCcdState() == CaseState.HEARING_READINESS && this.isHWFTypeHearing());
    }

    @JsonIgnore
    public String getHwFReferenceNumber() {
        if (this.isHWFTypeHearing()) {
            return this.getHearingHelpFeesReferenceNumber();
        }
        if (this.isHWFTypeClaimIssued()) {
            return this.getHelpWithFeesReferenceNumber();
        }
        return null;
    }

    @JsonIgnore
    public BigDecimal getHwFFeeAmount() {
        if (this.isHWFTypeHearing()) {
            return MonetaryConversions.penniesToPounds(this.getCalculatedHearingFeeInPence());
        }
        if (this.isHWFTypeClaimIssued()) {
            return MonetaryConversions.penniesToPounds(this.getCalculatedClaimFeeInPence());
        }
        return null;
    }

    @JsonIgnore
    public BigDecimal getRemissionAmount() {
        if (this.isHWFTypeHearing()) {
            return MonetaryConversions.penniesToPounds(this.getHearingRemissionAmount());
        }
        if (this.isHWFTypeClaimIssued()) {
            return MonetaryConversions.penniesToPounds(this.getClaimIssueRemissionAmount());
        }
        return null;
    }

    @JsonIgnore
    public BigDecimal getOutstandingFeeInPounds() {
        if (this.isHWFTypeHearing() && this.getHearingHwfDetails() != null) {
            return this.getHearingHwfDetails().getOutstandingFeeInPounds();
        }
        if (this.isHWFTypeClaimIssued() && this.getClaimIssuedHwfDetails() != null) {
            return this.getClaimIssuedHwfDetails().getOutstandingFeeInPounds();
        }
        return null;
    }

    @JsonIgnore
    public boolean isSettlementAgreementDeadlineExpired() {
        return nonNull(respondent1RespondToSettlementAgreementDeadline)
            && LocalDateTime.now().isAfter(respondent1RespondToSettlementAgreementDeadline);
    }

    @JsonIgnore
    public boolean hasApplicant1AcceptedCcj() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasApplicant1RequestedCcj).isPresent();
    }

    @JsonIgnore
    public boolean isDateAfterToday(LocalDate date) {
        return nonNull(date)
            && date.atTime(DeadlinesCalculator.END_OF_BUSINESS_DAY).isAfter(LocalDateTime.now());
    }

    @JsonIgnore
    public boolean hearingFeePaymentDoneWithHWF() {
        return isLipvLipOneVOne()
            && Objects.nonNull(getHearingHelpFeesReferenceNumber())
            && Objects.nonNull(getFeePaymentOutcomeDetails())
            && Objects.nonNull(getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForHearingFee());
    }

    @JsonIgnore
    public String getAssignedTrack() {
        return nonNull(getAllocatedTrack()) ? getAllocatedTrack().name() : getResponseClaimTrack();
    }

    @JsonIgnore
    public AllocatedTrack getAssignedTrackType() {
        return nonNull(getAssignedTrack()) ? AllocatedTrack.valueOf(getAssignedTrack()) : null;
    }

    @JsonIgnore
    public boolean hasApplicant1AcceptedCourtDecision() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasClaimantAcceptedCourtDecision).isPresent();
    }

    @JsonIgnore
    public boolean hasApplicant1CourtDecisionInFavourOfClaimant() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasCourtDecisionInFavourOfClaimant).isPresent();
    }

    public boolean hasApplicant1CourtDecisionInFavourOfDefendant() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasCourtDecisionInFavourOfDefendant).isPresent();
    }

    @JsonIgnore
    public boolean claimIssueFeePaymentDoneWithHWF() {
        return Objects.nonNull(getHelpWithFeesReferenceNumber())
            && Objects.nonNull(getFeePaymentOutcomeDetails())
            && Objects.nonNull(getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForClaimIssue());
    }

    @JsonIgnore
    public boolean claimIssueFullRemissionNotGrantedHWF() {
        return Objects.nonNull(getFeePaymentOutcomeDetails())
            && Objects.nonNull(getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForClaimIssue())
            && getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForClaimIssue() == NO;
    }

    @JsonIgnore
    public boolean hearingFeeFullRemissionNotGrantedHWF() {
        return Objects.nonNull(getFeePaymentOutcomeDetails())
            && Objects.nonNull(getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForHearingFee())
            && getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForHearingFee() == NO;
    }

    @JsonIgnore
    public boolean isLipvLROneVOne() {
        return !isRespondent1LiP()
            && isApplicant1NotRepresented()
            && isOneVOne(this);
    }

    @JsonIgnore
    public boolean nocApplyForLiPClaimant() {
        return isLRvLipOneVOne()
            && (getClaimIssuedPaymentDetails() == null
            && (claimIssueFeePaymentDoneWithHWF()
            || getChangeOfRepresentation() != null));
    }

    @JsonIgnore
    public LocalDate getApplicant1ClaimSettleDate() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1ClaimSettledDate).orElse(null);
    }

    @JsonIgnore
    public boolean isPaidLessThanClaimAmount() {
        RespondToClaim localRespondToClaim = null;
        if (getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE) {
            localRespondToClaim = getRespondToClaim();
        } else if (getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION) {
            localRespondToClaim = getRespondToAdmittedClaim();
        }
        return ofNullable(localRespondToClaim).map(RespondToClaim::getHowMuchWasPaid)
            .map(paid -> MonetaryConversions.penniesToPounds(paid).compareTo(totalClaimAmount) < 0).orElse(false);
    }

    @JsonIgnore
    public boolean isCourtDecisionInClaimantFavourImmediateRePayment() {
        return hasApplicant1CourtDecisionInFavourOfClaimant()
            && getApplicant1RepaymentOptionForDefendantSpec() == PaymentType.IMMEDIATELY;
    }

    @JsonIgnore
    public boolean nocApplyForLiPDefendantBeforeOffline() {
        return isLipvLROneVOne() && getChangeOfRepresentation() != null;
    }

    @JsonIgnore
    public boolean nocApplyForLiPDefendant() {
        return isLipvLROneVOne() && getChangeOfRepresentation() != null && this.getCcdState() == CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
    }

    @JsonIgnore
    public boolean isJudgeOrderVerificationRequired() {
        return (this.getCourtPermissionNeeded() != null && SettleDiscontinueYesOrNoList.YES.equals(this.getCourtPermissionNeeded()));
    }

    @JsonIgnore
    public boolean isClaimantDontWantToProceedWithFulLDefenceFD() {
        return this.isClaimBeingDisputed()
            && this.hasApplicantNotProceededWithClaim();
    }

    @JsonIgnore
    public boolean isLipCase() {
        return this.isApplicant1NotRepresented() || this.isRespondent1LiP();
    }

    @JsonIgnore
    public boolean isHearingFeePaid() {
        return nonNull(this.getHearingFeePaymentDetails())
            && SUCCESS.equals(this.getHearingFeePaymentDetails().getStatus()) || this.hearingFeePaymentDoneWithHWF();
    }

    @JsonIgnore
    public boolean isCcjRequestJudgmentByAdmissionDefendantNotPaid() {
        return getCcjPaymentDetails() != null
            && getCcjPaymentDetails().getCcjPaymentPaidSomeOption() != null
            && NO.equals(getCcjPaymentDetails().getCcjPaymentPaidSomeOption());
    }

    @JsonIgnore
    public String getApplicantSolicitor1UserDetailsEmail() {
        return applicantSolicitor1UserDetails == null ? null : applicantSolicitor1UserDetails.getEmail();
    }

    @JsonIgnore
    public String getClaimantUserDetailsEmail() {
        final IdamUserDetails claimantUserDetails = getClaimantUserDetails();
        return claimantUserDetails == null ? null : claimantUserDetails.getEmail();
    }

    @JsonIgnore
    public String getRespondent1PartyEmail() {
        final Party party = getRespondent1();
        return party == null ? null : party.getPartyEmail();
    }

    @JsonIgnore
    public String getRespondent2PartyEmail() {
        final Party party = getRespondent2();
        return party == null ? null : party.getPartyEmail();
    }

    @JsonIgnore
    public boolean isClaimUnderTranslationAfterDefResponse() {
        return this.getRespondent1ClaimResponseTypeForSpec() != null
            && this.getCcdState() == CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
    }

    @JsonIgnore
    public boolean isClaimUnderTranslationAfterClaimantResponse() {
        return this.getApplicant1ResponseDate() != null
            && this.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION;
    }
}
