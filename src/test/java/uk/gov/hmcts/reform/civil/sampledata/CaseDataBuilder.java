package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.PersonalInjuryType;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialHearingTimeEstimateDJ;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.model.FlightDelayDetails;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCasemanLR;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.CloseClaim;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.HearingDates;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.LengthOfUnemploymentComplexTypeLRspec;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.PartnerAndDependentsLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.UnemployedComplexTypeLRspec;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.caseprogression.RevisedHearingRequirements;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingFinalDisposalHearingDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingJudgesRecitalDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfNonElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts;
import uk.gov.hmcts.reform.civil.model.dq.FurtherInformation;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.VulnerabilityQuestions;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimUntilType;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaidInFull;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusDetails;
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalOrderWithoutHearing;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackOrderWithoutJudgement;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonForReconsideration;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.model.transferonlinecase.NotSuitableSdoOptions;
import uk.gov.hmcts.reform.civil.model.transferonlinecase.TocTransferCaseReason;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.ComplexityBand.BAND_1;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.PersonalInjuryType.ROAD_ACCIDENT;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.HearingLength.MORE_THAN_DAY;
import static uk.gov.hmcts.reform.civil.enums.dq.HearingLength.ONE_DAY;
import static uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration.MINUTES_120;
import static uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate.FIFTEEN_MINUTES;
import static uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentOrderFormGenerator.DISPOSAL_HEARING;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class CaseDataBuilder {

    public static final String LEGACY_CASE_REFERENCE = "000DC001";
    public static final Long CASE_ID = 1594901956117591L;
    public static final LocalDateTime SUBMITTED_DATE_TIME = LocalDateTime.now();
    public static final LocalDateTime RESPONSE_DEADLINE = SUBMITTED_DATE_TIME.toLocalDate().plusDays(14)
        .atTime(23, 59, 59);
    public static final LocalDateTime APPLICANT_RESPONSE_DEADLINE = SUBMITTED_DATE_TIME.plusDays(120);
    public static final LocalDate CLAIM_ISSUED_DATE = now();
    public static final LocalDateTime DEADLINE = LocalDate.now().atStartOfDay().plusDays(14);
    public static final LocalDate PAST_DATE = now().minusDays(1);
    public static final LocalDateTime NOTIFICATION_DEADLINE = LocalDate.now().atStartOfDay().plusDays(14);
    public static final BigDecimal FAST_TRACK_CLAIM_AMOUNT = BigDecimal.valueOf(10000);
    public static final LocalDate FUTURE_DATE = LocalDate.now().plusYears(1);
    public static final String CUSTOMER_REFERENCE = "12345";

    // Create Claim
    protected String caseNameHmctsInternal;
    protected Long ccdCaseReference;
    protected SolicitorReferences solicitorReferences;
    protected String respondentSolicitor2Reference;
    protected CourtLocation courtLocation;
    protected LocationRefData locationRefData;
    protected Party applicant1;
    protected Party applicant2;
    protected YesOrNo applicant1Represented;
    protected YesOrNo applicant1LitigationFriendRequired;
    protected YesOrNo applicant1AcceptFullAdmitPaymentPlanSpec;
    protected YesOrNo applicant2LitigationFriendRequired;
    protected Party respondent1;
    protected Party respondent2;
    protected YesOrNo respondent1Represented;
    protected YesOrNo respondent2Represented;
    protected YesOrNo defendant1LIPAtClaimIssued;
    protected YesOrNo defendant2LIPAtClaimIssued;
    protected String respondentSolicitor1EmailAddress;
    protected String respondentSolicitor2EmailAddress;
    protected ClaimValue claimValue;
    protected YesOrNo uploadParticularsOfClaim;
    protected ClaimType claimType;
    protected String claimTypeOther;
    protected PersonalInjuryType personalInjuryType;
    protected String personalInjuryTypeOther;
    protected DynamicList applicantSolicitor1PbaAccounts;
    protected Fee claimFee;
    protected StatementOfTruth applicantSolicitor1ClaimStatementOfTruth;
    protected StatementOfTruth uiStatementOfTruth;
    protected String paymentReference;
    protected String legacyCaseReference;
    protected AllocatedTrack allocatedTrack;
    protected String responseClaimTrack;
    protected CaseState ccdState;
    protected List<Element<CaseDocument>> systemGeneratedCaseDocuments;
    protected PaymentDetails claimIssuedPaymentDetails;
    protected PaymentDetails paymentDetails;
    protected PaymentDetails hearingFeePaymentDetails;
    protected CorrectEmail applicantSolicitor1CheckEmail;
    protected IdamUserDetails applicantSolicitor1UserDetails;
    //Deadline extension
    protected LocalDate respondentSolicitor1AgreedDeadlineExtension;
    protected LocalDate respondentSolicitor2AgreedDeadlineExtension;
    //Acknowledge Claim
    protected ResponseIntention respondent1ClaimResponseIntentionType;
    protected ResponseIntention respondent2ClaimResponseIntentionType;
    // Defendant Response Defendant 1
    protected RespondentResponseType respondent1ClaimResponseType;
    protected ResponseDocument respondent1ClaimResponseDocument;
    protected ResponseDocument respondentSharedClaimResponseDocument;
    protected Respondent1DQ respondent1DQ;
    protected Respondent2DQ respondent2DQ;
    protected Applicant1DQ applicant1DQ;
    protected Applicant2DQ applicant2DQ;
    // Defendant Response Defendant 2
    protected RespondentResponseType respondent2ClaimResponseType;
    protected ResponseDocument respondent2ClaimResponseDocument;
    protected YesOrNo respondentResponseIsSame;
    protected DynamicList defendantDetails;
    // Defendant Response 2 Applicants
    protected RespondentResponseType respondent1ClaimResponseTypeToApplicant2;
    protected RespondentResponseTypeSpec claimant1ClaimResponseTypeForSpec;
    protected RespondentResponseTypeSpec claimant2ClaimResponseTypeForSpec;
    // Claimant Response
    protected YesOrNo applicant1ProceedWithClaim;
    protected YesOrNo applicant1ProceedWithClaimSpec2v1;
    protected YesOrNo applicant2ProceedWithClaimMultiParty2v1;
    protected YesOrNo applicant1ProceedWithClaimMultiParty2v1;
    protected YesOrNo applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2;
    protected YesOrNo applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2;
    protected ResponseDocument applicant1DefenceResponseDocument;
    protected ResponseDocument applicant2DefenceResponseDocument;
    protected BusinessProcess businessProcess;

    //Case proceeds in caseman
    protected ClaimProceedsInCaseman claimProceedsInCaseman;
    protected ClaimProceedsInCasemanLR claimProceedsInCasemanLR;

    protected CloseClaim withdrawClaim;
    protected CloseClaim discontinueClaim;
    protected YesOrNo respondent1OrgRegistered;
    protected YesOrNo respondent2OrgRegistered;
    protected OrganisationPolicy applicant1OrganisationPolicy;
    protected OrganisationPolicy respondent1OrganisationPolicy;
    protected OrganisationPolicy respondent2OrganisationPolicy;
    protected YesOrNo addApplicant2;
    protected YesOrNo addRespondent2;
    protected CaseCategory caseAccessCategory;

    protected YesOrNo specRespondent1Represented;
    protected YesOrNo specRespondent2Represented;

    protected YesOrNo respondent2SameLegalRepresentative;
    protected LitigationFriend respondent1LitigationFriend;
    protected LitigationFriend respondent2LitigationFriend;
    protected LitigationFriend applicant1LitigationFriend;
    protected LitigationFriend applicant2LitigationFriend;
    protected LitigationFriend genericLitigationFriend;
    protected BreathingSpaceInfo breathing;
    protected BreathingSpaceEnterInfo enter;
    protected BreathingSpaceLiftInfo lift;

    protected List<Element<CaseNote>> caseNotes;

    //dates
    protected LocalDateTime submittedDate;
    protected LocalDateTime paymentSuccessfulDate;
    protected LocalDate issueDate;
    protected LocalDateTime claimNotificationDeadline;
    protected LocalDateTime claimNotificationDate;
    protected LocalDateTime claimDetailsNotificationDeadline;
    protected ServedDocumentFiles servedDocumentFiles;
    protected LocalDateTime claimDetailsNotificationDate;
    protected LocalDateTime respondent1ResponseDeadline;
    protected LocalDateTime respondent2ResponseDeadline;
    protected LocalDateTime claimDismissedDeadline;
    protected LocalDateTime respondent1TimeExtensionDate;
    protected LocalDateTime respondent2TimeExtensionDate;
    protected LocalDateTime respondent1AcknowledgeNotificationDate;
    protected LocalDateTime respondent2AcknowledgeNotificationDate;
    protected LocalDateTime respondent1ResponseDate;
    protected LocalDateTime respondent2ResponseDate;
    protected LocalDateTime applicant1ResponseDeadline;
    protected LocalDateTime applicant1ResponseDate;
    protected LocalDateTime applicant2ResponseDate;
    protected LocalDateTime takenOfflineDate;
    protected LocalDateTime takenOfflineByStaffDate;
    protected LocalDateTime unsuitableSDODate;
    protected LocalDateTime claimDismissedDate;
    protected LocalDateTime caseDismissedHearingFeeDueDate;
    protected LocalDate hearingDate;
    private InterestClaimOptions interestClaimOptions;
    private YesOrNo claimInterest;
    private SameRateInterestSelection sameRateInterestSelection;
    private InterestClaimFromType interestClaimFrom;
    private InterestClaimUntilType interestClaimUntil;
    private BigDecimal totalClaimAmount;
    private LocalDate interestFromSpecificDate;
    private BigDecimal breakDownInterestTotal;
    protected LocalDateTime respondent1LitigationFriendDate;
    protected LocalDateTime respondent2LitigationFriendDate;
    protected DynamicList defendantSolicitorNotifyClaimOptions;
    protected DynamicList defendantSolicitorNotifyClaimDetailsOptions;
    protected DynamicList selectLitigationFriend;
    protected LocalDateTime respondent1LitigationFriendCreatedDate;
    protected LocalDateTime respondent2LitigationFriendCreatedDate;

    public SRPbaDetails srPbaDetails;

    protected SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails;
    protected SolicitorOrganisationDetails respondentSolicitor2OrganisationDetails;
    protected Address applicantSolicitor1ServiceAddress;
    protected Address respondentSolicitor1ServiceAddress;
    protected Address respondentSolicitor2ServiceAddress;
    protected YesOrNo respondentSolicitor1ServiceAddressRequired;
    protected YesOrNo respondentSolicitor2ServiceAddressRequired;
    protected YesOrNo isRespondent1;
    protected YesOrNo isRespondent2;
    private List<IdValue<Bundle>> caseBundles;
    private RespondToClaim respondToClaim;
    private RespondentResponseTypeSpec respondent1ClaimResponseTypeForSpec;
    private YesOrNo defendantSingleResponseToBothClaimants;
    private RespondentResponseTypeSpec respondent2ClaimResponseTypeForSpec;
    private UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec;
    private RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec;
    private Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer;
    private YesOrNo respondent1MediationRequired;
    private YesOrNo respondent2MediationRequired;
    private PartnerAndDependentsLRspec respondent1PartnerAndDependent;
    private PartnerAndDependentsLRspec respondent2PartnerAndDependent;
    private ReasonNotSuitableSDO reasonNotSuitableSDO;
    private RepaymentPlanLRspec respondent1RepaymentPlan;
    private RepaymentPlanLRspec respondent2RepaymentPlan;
    private YesOrNo applicantsProceedIntention;
    private SmallClaimMedicalLRspec applicant1ClaimMediationSpecRequired;
    private SmallClaimMedicalLRspec applicantMPClaimMediationSpecRequired;
    private Mediation mediation;
    private YesOrNo specAoSApplicantCorrespondenceAddressRequired;
    private Address specAoSApplicantCorrespondenceAddressDetails;
    private YesOrNo specAoSRespondent2HomeAddressRequired;
    private YesOrNo specAoSRespondentCorrespondenceAddressRequired;
    private Address specAoSRespondentCorrespondenceAddressDetails;
    private Address specAoSRespondent2HomeAddressDetails;
    private YesOrNo respondent1DQWitnessesRequiredSpec;
    private List<Element<Witness>> respondent1DQWitnessesDetailsSpec;

    private String respondent1OrganisationIDCopy;
    private String respondent2OrganisationIDCopy;
    private String caseManagementOrderSelection;
    private LocalDateTime addLegalRepDeadline;
    private DefendantPinToPostLRspec respondent1PinToPostLRspec;
    private DisposalHearingMethodDJ trialHearingMethodDJ;
    private DynamicList hearingMethodValuesDisposalHearingDJ;
    private DynamicList hearingMethodValuesTrialHearingDJ;
    private DisposalHearingMethodDJ disposalHearingMethodDJ;
    private DynamicList trialHearingMethodInPersonDJ;
    private DisposalHearingBundleDJ disposalHearingBundleDJ;
    private DisposalHearingFinalDisposalHearingDJ disposalHearingFinalDisposalHearingDJ;
    private TrialHearingTrial trialHearingTrialDJ;
    private LocalDate hearingDueDate;
    private DisposalHearingJudgesRecitalDJ disposalHearingJudgesRecitalDJ;
    private TrialHearingJudgesRecital trialHearingJudgesRecitalDJ;
    private CaseLocationCivil caseManagementLocation;
    private DisposalHearingOrderMadeWithoutHearingDJ disposalHearingOrderMadeWithoutHearingDJ;
    private DisposalHearingFinalDisposalHearingTimeDJ disposalHearingFinalDisposalHearingTimeDJ;

    private YesOrNo generalAppVaryJudgementType;
    private Document generalAppN245FormUpload;
    private GAApplicationType generalAppType;
    private GAHearingDateGAspec generalAppHearingDate;

    private ChangeOfRepresentation changeOfRepresentation;
    private ChangeOrganisationRequest changeOrganisationRequest;

    private String unassignedCaseListDisplayOrganisationReferences;
    private String caseListDisplayDefendantSolicitorReferences;
    private  CertificateOfService cosNotifyClaimDefendant1;
    private  CertificateOfService cosNotifyClaimDefendant2;
    private CertificateOfService cosNotifyClaimDetails1;
    private CertificateOfService cosNotifyClaimDetails2;

    private FastTrackHearingTime fastTrackHearingTime;
    private List<DateToShowToggle> fastTrackTrialDateToToggle;
    private FastTrackOrderWithoutJudgement fastTrackOrderWithoutJudgement;

    private DisposalOrderWithoutHearing disposalOrderWithoutHearing;
    private DisposalHearingHearingTime disposalHearingHearingTime;

    private TrialHearingTimeDJ trialHearingTimeDJ;
    private TrialOrderMadeWithoutHearingDJ trialOrderMadeWithoutHearingDJ;

    private BigDecimal totalInterest;
    private YesOrNo applicant1AcceptAdmitAmountPaidSpec;

    private YesOrNo applicant1AcceptPartAdmitPaymentPlanSpec;

    private BigDecimal respondToAdmittedClaimOwingAmountPounds;
    //Trial Readiness
    private HearingDuration hearingDuration;
    private YesOrNo trialReadyApplicant;
    private YesOrNo trialReadyRespondent1;
    private YesOrNo trialReadyRespondent2;

    private RevisedHearingRequirements applicantRevisedHearingRequirements;
    private RevisedHearingRequirements respondent1RevisedHearingRequirements;
    private RevisedHearingRequirements respondent2RevisedHearingRequirements;

    private YesOrNo applicant1PartAdmitIntentionToSettleClaimSpec;
    private YesOrNo applicant1PartAdmitConfirmAmountPaidSpec;

    private CCJPaymentDetails ccjPaymentDetails;
    private DisposalHearingMethod disposalHearingMethod;
    private DynamicList hearingMethodValuesDisposalHearing;
    private DynamicList hearingMethodValuesFastTrack;
    private DynamicList hearingMethodValuesSmallClaims;

    private List<Element<PartyFlagStructure>> applicantExperts;
    private List<Element<PartyFlagStructure>> applicantWitnesses;
    private List<Element<PartyFlagStructure>> respondent1Experts;
    private List<Element<PartyFlagStructure>> respondent1Witnesses;
    private List<Element<PartyFlagStructure>> respondent2Experts;
    private List<Element<PartyFlagStructure>> respondent2Witnesses;
    private CaseDataLiP caseDataLiP;
    private YesOrNo claimant2ResponseFlag;
    private TimelineUploadTypeSpec specClaimResponseTimelineList;
    private TimelineUploadTypeSpec specClaimResponseTimelineList2;
    private YesOrNo defenceAdmitPartEmploymentTypeRequired;
    private YesOrNo specDefenceFullAdmitted2Required;
    private RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteRequired;
    private ResponseOneVOneShowTag showResponseOneVOneFlag;
    private SmallClaimsWitnessStatement smallClaimsWitnessStatement;
    private FastTrackWitnessOfFact fastTrackWitnessOfFact;
    private TrialHearingWitnessOfFact trialHearingWitnessOfFactDJ;

    private HearingSupportRequirementsDJ hearingSupportRequirementsDJ;
    private List<Element<CaseDocument>> defaultJudgmentDocuments = new ArrayList<>();
    private IdamUserDetails claimantUserDetails;

    private UpdateDetailsForm updateDetailsForm;

    private TocTransferCaseReason tocTransferCaseReason;

    private NotSuitableSdoOptions notSuitableSdoOptions;

    private List<Element<PartyFlagStructure>> applicant1LRIndividuals;
    private List<Element<PartyFlagStructure>> respondent1LRIndividuals;
    private List<Element<PartyFlagStructure>> respondent2LRIndividuals;

    private List<Element<PartyFlagStructure>> applicant1OrgIndividuals;
    private List<Element<PartyFlagStructure>> applicant2OrgIndividuals;
    private List<Element<PartyFlagStructure>> respondent1OrgIndividuals;
    private List<Element<PartyFlagStructure>> respondent2OrgIndividuals;

    protected String hearingReference;
    protected ListingOrRelisting listingOrRelisting;

    private YesOrNo drawDirectionsOrderRequired;

    private DynamicList transferCourtLocationList;
    private String reasonForTransfer;

    private YesOrNo isFlightDelayClaim;
    private FlightDelayDetails flightDelayDetails;
    private ReasonForReconsideration reasonForReconsideration;

    private YesOrNo responseClaimExpertSpecRequired;
    private YesOrNo responseClaimExpertSpecRequired2;
    private YesOrNo applicantMPClaimExpertSpecRequired;
    private YesOrNo applicant1ClaimExpertSpecRequired;

    public CaseDataBuilder applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo applicant1AcceptFullAdmitPaymentPlanSpec) {
        this.applicant1AcceptFullAdmitPaymentPlanSpec = applicant1AcceptFullAdmitPaymentPlanSpec;
        return this;
    }

    public CaseDataBuilder sameRateInterestSelection(SameRateInterestSelection sameRateInterestSelection) {
        this.sameRateInterestSelection = sameRateInterestSelection;
        return this;
    }

    public CaseDataBuilder responseClaimExpertSpecRequired(YesOrNo responseClaimExpertSpecRequired) {
        this.responseClaimExpertSpecRequired = responseClaimExpertSpecRequired;
        return this;
    }

    public CaseDataBuilder responseClaimExpertSpecRequired2(YesOrNo responseClaimExpertSpecRequired2) {
        this.responseClaimExpertSpecRequired2 = responseClaimExpertSpecRequired2;
        return this;
    }

    public CaseDataBuilder applicant1ClaimExpertSpecRequired(YesOrNo applicant1ClaimExpertSpecRequired) {
        this.applicant1ClaimExpertSpecRequired = applicant1ClaimExpertSpecRequired;
        return this;
    }

    public CaseDataBuilder applicantMPClaimExpertSpecRequired(YesOrNo applicantMPClaimExpertSpecRequired) {
        this.applicantMPClaimExpertSpecRequired = applicantMPClaimExpertSpecRequired;
        return this;
    }

    public CaseDataBuilder generalAppVaryJudgementType(YesOrNo generalAppVaryJudgementType) {
        this.generalAppVaryJudgementType = generalAppVaryJudgementType;
        return this;
    }

    public CaseDataBuilder generalAppType(GAApplicationType generalAppType) {
        this.generalAppType = generalAppType;
        return this;
    }

    public CaseDataBuilder generalAppHearingDate(GAHearingDateGAspec generalAppHearingDate) {
        this.generalAppHearingDate = generalAppHearingDate;
        return this;
    }

    public CaseDataBuilder generalAppN245FormUpload(Document generalAppN245FormUpload) {
        this.generalAppN245FormUpload = generalAppN245FormUpload;
        return this;
    }

    public CaseDataBuilder breakDownInterestTotal(BigDecimal breakDownInterestTotal) {
        this.breakDownInterestTotal = breakDownInterestTotal;
        return this;
    }

    public CaseDataBuilder interestFromSpecificDate(LocalDate interestFromSpecificDate) {
        this.interestFromSpecificDate = interestFromSpecificDate;
        return this;
    }

    public CaseDataBuilder totalClaimAmount(BigDecimal totalClaimAmount) {
        this.totalClaimAmount = totalClaimAmount;
        return this;
    }

    public CaseDataBuilder interestClaimOptions(InterestClaimOptions interestClaimOptions) {
        this.interestClaimOptions = interestClaimOptions;
        return this;
    }

    public CaseDataBuilder interestClaimFrom(InterestClaimFromType interestClaimFrom) {
        this.interestClaimFrom = interestClaimFrom;
        return this;
    }

    public CaseDataBuilder interestClaimUntil(InterestClaimUntilType interestClaimUntil) {
        this.interestClaimUntil = interestClaimUntil;
        return this;
    }

    public CaseDataBuilder claimInterest(YesOrNo claimInterest) {
        this.claimInterest = claimInterest;
        return this;
    }

    //workaround fields
    protected Party respondent1Copy;
    protected Party respondent2Copy;

    public CaseDataBuilder respondent1ResponseDeadline(LocalDateTime deadline) {
        this.respondent1ResponseDeadline = deadline;
        return this;
    }

    public CaseDataBuilder respondent2ResponseDeadline(LocalDateTime deadline) {
        this.respondent2ResponseDeadline = deadline;
        return this;
    }

    public CaseDataBuilder respondent1AcknowledgeNotificationDate(LocalDateTime dateTime) {
        this.respondent1AcknowledgeNotificationDate = dateTime;
        return this;
    }

    public CaseDataBuilder respondent2AcknowledgeNotificationDate(LocalDateTime dateTime) {
        this.respondent2AcknowledgeNotificationDate = dateTime;
        return this;
    }

    public CaseDataBuilder applicantSolicitor1ServiceAddress(Address applicantSolicitor1ServiceAddress) {
        this.applicantSolicitor1ServiceAddress = applicantSolicitor1ServiceAddress;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1ServiceAddress(Address respondentSolicitor1ServiceAddress) {
        this.respondentSolicitor1ServiceAddress = respondentSolicitor1ServiceAddress;
        return this;
    }

    public CaseDataBuilder respondentSolicitor2ServiceAddress(Address respondentSolicitor2ServiceAddress) {
        this.respondentSolicitor2ServiceAddress = respondentSolicitor2ServiceAddress;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1ServiceAddressRequired(YesOrNo respondentSolicitor1ServiceAddressRequired) {
        this.respondentSolicitor1ServiceAddressRequired = respondentSolicitor1ServiceAddressRequired;
        return this;
    }

    public CaseDataBuilder respondentSolicitor2ServiceAddressRequired(YesOrNo respondentSolicitor2ServiceAddressRequired) {
        this.respondentSolicitor2ServiceAddressRequired = respondentSolicitor2ServiceAddressRequired;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1EmailAddress(String respondentSolicitor1EmailAddress) {
        this.respondentSolicitor1EmailAddress = respondentSolicitor1EmailAddress;
        return this;
    }

    public CaseDataBuilder respondentSolicitor2EmailAddress(String respondentSolicitor2EmailAddress) {
        this.respondentSolicitor2EmailAddress = respondentSolicitor2EmailAddress;
        return this;
    }

    public CaseDataBuilder isRespondent1(YesOrNo isRespondent1) {
        this.isRespondent1 = isRespondent1;
        return this;
    }

    public CaseDataBuilder isRespondent2(YesOrNo isRespondent2) {
        this.isRespondent2 = isRespondent2;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1AgreedDeadlineExtension(LocalDate extensionDate) {
        this.respondentSolicitor1AgreedDeadlineExtension = extensionDate;
        return this;
    }

    public CaseDataBuilder respondentSolicitor2AgreedDeadlineExtension(LocalDate extensionDate) {
        this.respondentSolicitor2AgreedDeadlineExtension = extensionDate;
        return this;
    }

    public CaseDataBuilder respondent1TimeExtensionDate(LocalDateTime extensionDate) {
        this.respondent1TimeExtensionDate = extensionDate;
        return this;
    }

    public CaseDataBuilder respondent2TimeExtensionDate(LocalDateTime extensionDate) {
        this.respondent2TimeExtensionDate = extensionDate;
        return this;
    }

    public CaseDataBuilder respondent2(Party party) {
        this.respondent2 = party;
        return this;
    }

    public CaseDataBuilder caseNotes(CaseNote caseNote) {
        this.caseNotes = wrapElements(caseNote);
        return this;
    }

    public CaseDataBuilder respondent1OrganisationIDCopy(String id) {
        this.respondent1OrganisationIDCopy = id;
        return this;
    }

    public CaseDataBuilder respondent2OrganisationIDCopy(String id) {
        this.respondent2OrganisationIDCopy = id;
        return this;
    }

    public CaseDataBuilder cosNotifyClaimDefendant1(CertificateOfService cosNotifyClaimDefendant) {
        this.cosNotifyClaimDefendant1 = cosNotifyClaimDefendant;
        return this;
    }

    public CaseDataBuilder cosNotifyClaimDefendant2(CertificateOfService cosNotifyClaimDefendant) {
        this.cosNotifyClaimDefendant2 = cosNotifyClaimDefendant;
        return this;
    }

    public CaseDataBuilder respondent1DQWithFixedRecoverableCosts() {
        respondent1DQ = respondent1DQ.toBuilder()
            .respondent1DQFixedRecoverableCosts(FixedRecoverableCosts.builder()
                                                   .isSubjectToFixedRecoverableCostRegime(YES)
                                                   .band(BAND_1)
                                                   .complexityBandingAgreed(YES)
                                                   .reasons("Good reason")
                                                   .build()).build();
        return this;
    }

    public CaseDataBuilder respondent1DQ() {
        respondent1DQ = Respondent1DQ.builder()
            .respondent1DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                          .explainedToClient(List.of("CONFIRM"))
                                                          .oneMonthStayRequested(YES)
                                                          .reactionProtocolCompliedWith(YES)
                                                          .build())
            .respondent1DQDisclosureOfElectronicDocuments(DisclosureOfElectronicDocuments.builder()
                                                              .reachedAgreement(YES)
                                                              .build())
            .respondent1DQDisclosureOfNonElectronicDocuments(DisclosureOfNonElectronicDocuments.builder()
                                                                 .directionsForDisclosureProposed(NO)
                                                                 .build())
            .respondent1DQExperts(Experts.builder().expertRequired(NO).build())
            .respondent1DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
            .respondent1DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(NO).build())
            .respondent1DQHearingSupport(HearingSupport.builder()
                                             .supportRequirements(YES)
                                             .supportRequirementsAdditional("Additional support needed")
                                             .requirements(List.of()).build())
            .respondent1DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .respondent1DQLanguage(WelshLanguageRequirements.builder().build())
            .respondent1DQVulnerabilityQuestions(VulnerabilityQuestions.builder()
                                                     .vulnerabilityAdjustmentsRequired(NO).build())
            .respondent1DQStatementOfTruth(StatementOfTruth.builder().name("John Doe").role("Solicitor").build())
            .respondent1DQDraftDirections(DocumentBuilder.builder().documentName("defendant1-directions.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilder respondent1DQ(Respondent1DQ respondent1DQ) {
        this.respondent1DQ = respondent1DQ;
        return this;
    }

    public CaseDataBuilder respondent1DQWithoutSotAndExperts() {
        respondent1DQ = Respondent1DQ.builder()
            .respondent1DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                          .explainedToClient(List.of("CONFIRM"))
                                                          .oneMonthStayRequested(YES)
                                                          .reactionProtocolCompliedWith(YES)
                                                          .build())
            .respondent1DQDisclosureOfElectronicDocuments(DisclosureOfElectronicDocuments.builder()
                                                              .reachedAgreement(YES)
                                                              .build())
            .respondent1DQDisclosureOfNonElectronicDocuments(DisclosureOfNonElectronicDocuments.builder()
                                                                 .directionsForDisclosureProposed(NO)
                                                                 .build())
            .respondent1DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
            .respondent1DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(NO).build())
            .respondent1DQHearingSupport(HearingSupport.builder().requirements(List.of()).build())
            .respondent1DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .respondent1DQLanguage(WelshLanguageRequirements.builder().build())
            .respondent1DQVulnerabilityQuestions(VulnerabilityQuestions.builder()
                                                     .vulnerabilityAdjustmentsRequired(NO).build())
            .respondent1DQDraftDirections(DocumentBuilder.builder().documentName("defendant1-directions.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilder respondent1DQWithLocation() {
        respondent1DQ = Respondent1DQ.builder()
            .respondent1DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                          .explainedToClient(List.of("CONFIRM"))
                                                          .oneMonthStayRequested(YES)
                                                          .reactionProtocolCompliedWith(YES)
                                                          .build())
            .respondent1DQDisclosureOfElectronicDocuments(DisclosureOfElectronicDocuments.builder()
                                                              .reachedAgreement(YES)
                                                              .build())
            .respondent1DQDisclosureOfNonElectronicDocuments(DisclosureOfNonElectronicDocuments.builder()
                                                                 .directionsForDisclosureProposed(NO)
                                                                 .build())
            .respondent1DQExperts(Experts.builder().expertRequired(NO).build())
            .respondent1DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
            .respondent1DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(NO).build())
            .respondent1DQRequestedCourt(RequestedCourt.builder()
                                             .responseCourtCode("444")
                                             .responseCourtName("Court name 444")
                                             .reasonForHearingAtSpecificCourt("Reason of Respondent 1 to choose court")
                                             .caseLocation(CaseLocationCivil.builder()
                                                               .baseLocation("dummy base").region("dummy region")
                                                               .build()).build())
            .respondent1DQHearingSupport(HearingSupport.builder().requirements(List.of()).build())
            .respondent1DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .respondent1DQLanguage(WelshLanguageRequirements.builder().build())
            .respondent1DQVulnerabilityQuestions(VulnerabilityQuestions.builder()
                                                     .vulnerabilityAdjustmentsRequired(NO).build())
            .respondent1DQStatementOfTruth(StatementOfTruth.builder().name("John Doe").role("Solicitor").build())
            .respondent1DQDraftDirections(DocumentBuilder.builder().documentName("defendant1-directions.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilder respondent1DQWithLocationAndWithoutExperts() {
        respondent1DQ = Respondent1DQ.builder()
            .respondent1DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                          .explainedToClient(List.of("CONFIRM"))
                                                          .oneMonthStayRequested(YES)
                                                          .reactionProtocolCompliedWith(YES)
                                                          .build())
            .respondent1DQDisclosureOfElectronicDocuments(DisclosureOfElectronicDocuments.builder()
                                                              .reachedAgreement(YES)
                                                              .build())
            .respondent1DQDisclosureOfNonElectronicDocuments(DisclosureOfNonElectronicDocuments.builder()
                                                                 .directionsForDisclosureProposed(NO)
                                                                 .build())
            .respondent1DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
            .respondent1DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(NO).build())
            .respondent1DQRequestedCourt(RequestedCourt.builder()
                                             .responseCourtCode("444")
                                             .caseLocation(CaseLocationCivil.builder()
                                                               .baseLocation("dummy base").region("dummy region")
                                                               .build()).build())
            .respondent1DQHearingSupport(HearingSupport.builder().requirements(List.of()).build())
            .respondent1DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .respondent1DQLanguage(WelshLanguageRequirements.builder().build())
            .respondent1DQVulnerabilityQuestions(VulnerabilityQuestions.builder()
                                                     .vulnerabilityAdjustmentsRequired(NO).build())
            .respondent1DQStatementOfTruth(StatementOfTruth.builder().name("John Doe").role("Solicitor").build())
            .respondent1DQDraftDirections(DocumentBuilder.builder().documentName("defendant1-directions.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilder respondent1DQWithUnavailableDates() {
        UnavailableDate unavailableDate = UnavailableDate.builder()
            .date(LocalDate.now().plusDays(1))
            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
            .build();
        this.respondent1DQ = Respondent1DQ.builder()
            .respondent1DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(YES)
                                      .unavailableDates(wrapElements(List.of(unavailableDate))).build())
            .build();
        return this;
    }

    public CaseDataBuilder respondent1DQWithUnavailableDateRange() {
        UnavailableDate unavailableDate = UnavailableDate.builder()
            .fromDate(LocalDate.now().plusDays(1))
            .toDate(LocalDate.now().plusDays(2))
            .unavailableDateType(UnavailableDateType.DATE_RANGE)
            .build();
        this.respondent1DQ = Respondent1DQ.builder()
            .respondent1DQHearing(Hearing.builder().hearingLength(MORE_THAN_DAY).unavailableDatesRequired(YES)
                                      .unavailableDates(wrapElements(List.of(unavailableDate))).build())
            .build();
        return this;
    }

    public CaseDataBuilder respondent2DQWithUnavailableDateRange() {
        UnavailableDate unavailableDate = UnavailableDate.builder()
            .fromDate(LocalDate.now().plusDays(1))
            .toDate(LocalDate.now().plusDays(2))
            .unavailableDateType(UnavailableDateType.DATE_RANGE)
            .build();
        this.respondent2DQ = Respondent2DQ.builder()
            .respondent2DQHearing(Hearing.builder().hearingLength(MORE_THAN_DAY).unavailableDatesRequired(YES)
                                      .unavailableDates(wrapElements(List.of(unavailableDate))).build())
            .build();
        return this;
    }

    public CaseDataBuilder applicant1DQWithUnavailableDateRange() {
        UnavailableDate unavailableDate = UnavailableDate.builder()
            .fromDate(LocalDate.now().plusDays(1))
            .toDate(LocalDate.now().plusDays(2))
            .unavailableDateType(UnavailableDateType.DATE_RANGE)
            .build();
        this.applicant1DQ = Applicant1DQ.builder()
            .applicant1DQHearing(Hearing.builder().hearingLength(MORE_THAN_DAY).unavailableDatesRequired(YES)
                                     .unavailableDates(wrapElements(List.of(unavailableDate))).build())
            .build();
        return this;
    }

    public CaseDataBuilder applicant1DQWithUnavailableDate() {
        UnavailableDate unavailableDate = UnavailableDate.builder()
            .date(LocalDate.now().plusDays(1))
            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
            .build();
        this.applicant1DQ = Applicant1DQ.builder()
            .applicant1DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(YES)
                                      .unavailableDates(wrapElements(List.of(unavailableDate))).build())
            .build();
        return this;
    }

    public CaseDataBuilder respondent2DQWithLocation() {
        respondent2DQ = Respondent2DQ.builder()
            .respondent2DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                          .explainedToClient(List.of("CONFIRM"))
                                                          .oneMonthStayRequested(YES)
                                                          .reactionProtocolCompliedWith(YES)
                                                          .build())
            .respondent2DQDisclosureOfElectronicDocuments(DisclosureOfElectronicDocuments.builder()
                                                              .reachedAgreement(YES)
                                                              .build())
            .respondent2DQDisclosureOfNonElectronicDocuments(DisclosureOfNonElectronicDocuments.builder()
                                                                 .directionsForDisclosureProposed(NO)
                                                                 .build())
            .respondent2DQExperts(Experts.builder().expertRequired(NO).build())
            .respondent2DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
            .respondent2DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(NO).build())
            .respondent2DQRequestedCourt(RequestedCourt.builder()
                                             .responseCourtCode("444")
                                             .responseCourtName("Court name 444")
                                             .reasonForHearingAtSpecificCourt("Reason of Respondent 2 to choose court")
                                             .caseLocation(CaseLocationCivil.builder()
                                                               .baseLocation("dummy base").region("dummy region")
                                                               .build()).build())
            .respondent2DQHearingSupport(HearingSupport.builder().requirements(List.of()).build())
            .respondent2DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .respondent2DQLanguage(WelshLanguageRequirements.builder().build())
            .respondent2DQVulnerabilityQuestions(VulnerabilityQuestions.builder()
                                                     .vulnerabilityAdjustmentsRequired(NO).build())
            .respondent2DQStatementOfTruth(StatementOfTruth.builder().name("John Doe").role("Solicitor").build())
            .respondent2DQDraftDirections(DocumentBuilder.builder().documentName("defendant1-directions.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilder respondent2DQWithLocationAndWithoutExperts() {
        respondent2DQ = Respondent2DQ.builder()
            .respondent2DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                          .explainedToClient(List.of("CONFIRM"))
                                                          .oneMonthStayRequested(YES)
                                                          .reactionProtocolCompliedWith(YES)
                                                          .build())
            .respondent2DQDisclosureOfElectronicDocuments(DisclosureOfElectronicDocuments.builder()
                                                              .reachedAgreement(YES)
                                                              .build())
            .respondent2DQDisclosureOfNonElectronicDocuments(DisclosureOfNonElectronicDocuments.builder()
                                                                 .directionsForDisclosureProposed(NO)
                                                                 .build())
            .respondent2DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
            .respondent2DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(NO).build())
            .respondent2DQRequestedCourt(RequestedCourt.builder()
                                             .responseCourtCode("444")
                                             .caseLocation(CaseLocationCivil.builder()
                                                               .baseLocation("dummy base").region("dummy region")
                                                               .build()).build())
            .respondent2DQHearingSupport(HearingSupport.builder().requirements(List.of()).build())
            .respondent2DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .respondent2DQLanguage(WelshLanguageRequirements.builder().build())
            .respondent2DQVulnerabilityQuestions(VulnerabilityQuestions.builder()
                                                     .vulnerabilityAdjustmentsRequired(NO).build())
            .respondent2DQStatementOfTruth(StatementOfTruth.builder().name("John Doe").role("Solicitor").build())
            .respondent2DQDraftDirections(DocumentBuilder.builder().documentName("defendant1-directions.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilder respondent2DQWithFixedRecoverableCosts() {
        respondent2DQ = respondent2DQ.toBuilder()
            .respondent2DQFixedRecoverableCosts(FixedRecoverableCosts.builder()
                                                    .isSubjectToFixedRecoverableCostRegime(YES)
                                                    .band(BAND_1)
                                                    .complexityBandingAgreed(YES)
                                                    .reasons("Good reason")
                                                    .build()).build();
        return this;
    }

    public CaseDataBuilder respondent2DQ() {
        respondent2DQ = Respondent2DQ.builder()
            .respondent2DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                          .explainedToClient(List.of("CONFIRM"))
                                                          .oneMonthStayRequested(YES)
                                                          .reactionProtocolCompliedWith(YES)
                                                          .build())
            .respondent2DQDisclosureOfElectronicDocuments(DisclosureOfElectronicDocuments.builder()
                                                              .reachedAgreement(YES)
                                                              .build())
            .respondent2DQDisclosureOfNonElectronicDocuments(DisclosureOfNonElectronicDocuments.builder()
                                                                 .directionsForDisclosureProposed(NO)
                                                                 .build())
            .respondent2DQExperts(Experts.builder().expertRequired(NO).build())
            .respondent2DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
            .respondent2DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(NO).build())
            .respondent2DQRequestedCourt(RequestedCourt.builder().build())
            .respondent2DQHearingSupport(HearingSupport.builder().requirements(List.of()).build())
            .respondent2DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .respondent2DQVulnerabilityQuestions(VulnerabilityQuestions.builder()
                                                     .vulnerabilityAdjustmentsRequired(NO).build())
            .respondent2DQLanguage(WelshLanguageRequirements.builder().build())
            .respondent2DQStatementOfTruth(StatementOfTruth.builder().name("Jane Doe").role("Solicitor").build())
            .respondent2DQDraftDirections(DocumentBuilder.builder().documentName("defendant2-directions.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilder respondent2DQ(Respondent2DQ respondent2DQ) {
        this.respondent2DQ = respondent2DQ;
        return this;
    }

    public CaseDataBuilder respondent2DQWithoutSotAndExperts() {
        respondent2DQ = Respondent2DQ.builder()
            .respondent2DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                          .explainedToClient(List.of("CONFIRM"))
                                                          .oneMonthStayRequested(YES)
                                                          .reactionProtocolCompliedWith(YES)
                                                          .build())
            .respondent2DQDisclosureOfElectronicDocuments(DisclosureOfElectronicDocuments.builder()
                                                              .reachedAgreement(YES)
                                                              .build())
            .respondent2DQDisclosureOfNonElectronicDocuments(DisclosureOfNonElectronicDocuments.builder()
                                                                 .directionsForDisclosureProposed(NO)
                                                                 .build())
            .respondent2DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
            .respondent2DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(NO).build())
            .respondent2DQRequestedCourt(RequestedCourt.builder().build())
            .respondent2DQHearingSupport(HearingSupport.builder().requirements(List.of()).build())
            .respondent2DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .respondent2DQVulnerabilityQuestions(VulnerabilityQuestions.builder()
                                                     .vulnerabilityAdjustmentsRequired(NO).build())
            .respondent2DQLanguage(WelshLanguageRequirements.builder().build())
            .respondent2DQDraftDirections(DocumentBuilder.builder().documentName("defendant2-directions.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilder applicant1DQ() {
        applicant1DQ = Applicant1DQ.builder()
            .applicant1DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                         .explainedToClient(List.of("OTHER"))
                                                         .oneMonthStayRequested(NO)
                                                         .reactionProtocolCompliedWith(YES)
                                                         .build())
            .applicant1DQDisclosureOfElectronicDocuments(DisclosureOfElectronicDocuments.builder()
                                                             .reachedAgreement(YES)
                                                             .build())
            .applicant1DQDisclosureOfNonElectronicDocuments(DisclosureOfNonElectronicDocuments.builder()
                                                                .directionsForDisclosureProposed(NO)
                                                                .build())
            .applicant1DQExperts(Experts.builder().expertRequired(NO).build())
            .applicant1DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
            .applicant1DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(NO).build())
            .applicant1DQRequestedCourt(RequestedCourt.builder().build())
            .applicant1DQHearingSupport(HearingSupport.builder()
                                            .supportRequirements(YES)
                                            .supportRequirementsAdditional("Additional support needed")
                                            .requirements(List.of()).build())
            .applicant1DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .applicant1DQLanguage(WelshLanguageRequirements.builder().build())
            .applicant1DQVulnerabilityQuestions(VulnerabilityQuestions.builder()
                                                    .vulnerabilityAdjustmentsRequired(NO).build())
            .applicant1DQStatementOfTruth(StatementOfTruth.builder().name("Bob Jones").role("Solicitor").build())
            .build();
        return this;
    }

    public CaseDataBuilder applicant1DQ(Applicant1DQ applicant1DQ) {
        this.applicant1DQ = applicant1DQ;
        return this;
    }

    public CaseDataBuilder applicant1DQWithExperts() {
        var applicant1DQBuilder = applicant1DQ != null
            ? applicant1DQ.toBuilder() : applicant1DQ().build().getApplicant1DQ().toBuilder();
        applicant1DQBuilder.applicant1DQExperts(
            uk.gov.hmcts.reform.civil.model.dq.Experts.builder()
                .expertRequired(YES)
                .expertReportsSent(ExpertReportsSent.NO)
                .jointExpertSuitable(NO)
                .details(
                    wrapElements(uk.gov.hmcts.reform.civil.model.dq.Expert.builder()
                                     .firstName("Expert")
                                     .lastName("One")
                                     .phoneNumber("01482764322")
                                     .emailAddress("fast.claim.expert1@example.com")
                                     .whyRequired("Good reasons")
                                     .fieldOfExpertise("Some field")
                                     .estimatedCost(BigDecimal.valueOf(10000))
                                     .build()
                    )
                )
                .build()
        );

        applicant1DQ = applicant1DQBuilder.build();
        return this;
    }

    public CaseDataBuilder applicant1DQWithWitnesses() {
        var applicant1DQBuilder = applicant1DQ != null
            ? applicant1DQ.toBuilder() : applicant1DQ().build().getApplicant1DQ().toBuilder();
        applicant1DQBuilder.applicant1DQWitnesses(
            Witnesses.builder()
                .witnessesToAppear(YES)
                .details(wrapElements(
                    Witness.builder()
                        .firstName("Witness")
                        .lastName("One")
                        .phoneNumber("01482764322")
                        .emailAddress("witness.one@example.com")
                        .reasonForWitness("Saw something")
                        .build()))
                .build());

        applicant1DQ = applicant1DQBuilder.build();
        return this;
    }

    public CaseDataBuilder applicant1DQWithHearingSupport() {
        var applicant1DQBuilder = applicant1DQ != null
            ? applicant1DQ.toBuilder() : applicant1DQ().build().getApplicant1DQ().toBuilder();

        applicant1DQBuilder.applicant1DQHearingSupport(
            HearingSupport.builder()
                .supportRequirements(YES)
                .supportRequirementsAdditional("Support requirements works!!!")
                .build()
        ).build();

        applicant1DQ = applicant1DQBuilder.build();
        return this;
    }

    public CaseDataBuilder respondent1DQSmallClaimExperts(ExpertDetails experts, YesOrNo expertsRequired) {
        var respondent1DQBuilder = this.respondent1DQ != null
            ? this.respondent1DQ.toBuilder() : respondent1DQ().build().getRespondent1DQ().toBuilder();
        ExpertDetails expertDetails = experts != null
            ? experts
            : (ExpertDetails.builder()
                .expertName("Mr Expert Defendant")
                .firstName("Expert")
                .lastName("Defendant")
                .phoneNumber("07123456789")
                .emailAddress("test@email.com")
                .fieldofExpertise("Roofing")
                .estimatedCost(new BigDecimal(434))
                .build());

        respondent1DQBuilder.respondToClaimExperts(expertDetails).build();
        respondent1DQ = respondent1DQBuilder.build();

        this.responseClaimExpertSpecRequired(expertsRequired != null ? expertsRequired : YES);
        return this;
    }

    public CaseDataBuilder respondent2DQSmallClaimExperts(ExpertDetails experts, YesOrNo expertsRequired) {
        var respondent2DQBuilder = this.respondent2DQ != null
            ? this.respondent2DQ.toBuilder() : respondent2DQ().build().getRespondent2DQ().toBuilder();
        ExpertDetails expertDetails = experts != null
            ? experts
            : (ExpertDetails.builder()
                .expertName("Mr Expert Defendant")
                .firstName("Expert")
                .lastName("Defendant")
                .phoneNumber("07123456789")
                .emailAddress("test@email.com")
                .fieldofExpertise("Roofing")
                .estimatedCost(new BigDecimal(434))
                .build());

        respondent2DQBuilder.respondToClaimExperts2(expertDetails).build();
        respondent2DQ = respondent2DQBuilder.build();

        this.responseClaimExpertSpecRequired2(expertsRequired != null ? expertsRequired : YES);
        return this;
    }

    public CaseDataBuilder applicant1DQSmallClaimExperts(ExpertDetails experts, YesOrNo expertsRequired) {
        var applicant1DQBuilder = applicant1DQ != null
            ? applicant1DQ.toBuilder() : applicant1DQ().build().getApplicant1DQ().toBuilder();

        ExpertDetails expertDetails = experts != null
            ? experts
            : (ExpertDetails.builder()
            .expertName("Mr Expert Defendant")
            .firstName("Expert")
            .lastName("Defendant")
            .phoneNumber("07123456789")
            .emailAddress("test@email.com")
            .fieldofExpertise("Roofing")
            .estimatedCost(new BigDecimal(434))
            .build());

        applicant1DQBuilder.applicant1RespondToClaimExperts(expertDetails).build();
        this.applicant1ClaimExpertSpecRequired(expertsRequired != null ? expertsRequired : YES);

        applicant1DQ = applicant1DQBuilder.build();
        return this;
    }

    public CaseDataBuilder applicant2DQSmallClaimExperts() {
        return applicant2DQSmallClaimExperts(null, null);
    }

    public CaseDataBuilder applicant2DQSmallClaimExperts(ExpertDetails experts, YesOrNo expertsRequired) {
        var applicant2DQBuilder = applicant2DQ != null
            ? applicant2DQ.toBuilder() : applicant2DQ().build().getApplicant2DQ().toBuilder();

        ExpertDetails expertDetails = experts != null
            ? experts
            : (ExpertDetails.builder()
            .expertName("Mr Expert Defendant")
            .firstName("Expert")
            .lastName("Defendant")
            .phoneNumber("07123456789")
            .emailAddress("test@email.com")
            .fieldofExpertise("Roofing")
            .estimatedCost(new BigDecimal(434))
            .build());

        applicant2DQBuilder.applicant2RespondToClaimExperts(expertDetails).build();
        this.applicantMPClaimExpertSpecRequired(expertsRequired != null ? expertsRequired : YES);

        applicant2DQ = applicant2DQBuilder.build();
        return this;
    }

    public CaseDataBuilder applicant1DQWithLocation() {
        applicant1DQ = Applicant1DQ.builder()
            .applicant1DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                         .explainedToClient(List.of("OTHER"))
                                                         .oneMonthStayRequested(NO)
                                                         .reactionProtocolCompliedWith(YES)
                                                         .build())
            .applicant1DQDisclosureOfElectronicDocuments(DisclosureOfElectronicDocuments.builder()
                                                             .reachedAgreement(YES)
                                                             .build())
            .applicant1DQDisclosureOfNonElectronicDocuments(DisclosureOfNonElectronicDocuments.builder()
                                                                .directionsForDisclosureProposed(NO)
                                                                .build())
            .applicant1DQExperts(Experts.builder().expertRequired(NO).build())
            .applicant1DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
            .applicant1DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(NO).build())
            .applicant1DQRequestedCourt(RequestedCourt.builder()
                                            .responseCourtCode("court4")
                                            .caseLocation(CaseLocationCivil.builder()
                                                              .baseLocation("dummy base").region("dummy region")
                                                              .build()).build())
            .applicant1DQHearingSupport(HearingSupport.builder().requirements(List.of()).build())
            .applicant1DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .applicant1DQLanguage(WelshLanguageRequirements.builder().build())
            .applicant1DQVulnerabilityQuestions(VulnerabilityQuestions.builder()
                                                    .vulnerabilityAdjustmentsRequired(NO).build())
            .applicant1DQStatementOfTruth(StatementOfTruth.builder().name("Bob Jones").role("Solicitor").build())
            .build();
        return this;
    }

    public CaseDataBuilder applicant1DQWithFixedRecoverableCosts() {
        applicant1DQ = applicant1DQ.toBuilder()
            .applicant1DQFixedRecoverableCosts(FixedRecoverableCosts.builder()
                                                   .isSubjectToFixedRecoverableCostRegime(YES)
                                                   .band(BAND_1)
                                                   .complexityBandingAgreed(YES)
                                                   .reasons("Good reason")
                                                   .build()).build();
        return this;
    }

    public CaseDataBuilder applicant2DQWithFixedRecoverableCosts() {
        applicant2DQ = applicant2DQ.toBuilder()
            .applicant2DQFixedRecoverableCosts(FixedRecoverableCosts.builder()
                                                   .isSubjectToFixedRecoverableCostRegime(YES)
                                                   .band(BAND_1)
                                                   .complexityBandingAgreed(YES)
                                                   .reasons("Good reason")
                                                   .build()).build();
        return this;
    }

    public CaseDataBuilder applicant1DQWithLocationWithoutExperts() {
        applicant1DQ = Applicant1DQ.builder()
            .applicant1DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                         .explainedToClient(List.of("OTHER"))
                                                         .oneMonthStayRequested(NO)
                                                         .reactionProtocolCompliedWith(YES)
                                                         .build())
            .applicant1DQDisclosureOfElectronicDocuments(DisclosureOfElectronicDocuments.builder()
                                                             .reachedAgreement(YES)
                                                             .build())
            .applicant1DQDisclosureOfNonElectronicDocuments(DisclosureOfNonElectronicDocuments.builder()
                                                                .directionsForDisclosureProposed(NO)
                                                                .build())
            .applicant1DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
            .applicant1DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(NO).build())
            .applicant1DQRequestedCourt(RequestedCourt.builder()
                                            .responseCourtCode("court4")
                                            .caseLocation(CaseLocationCivil.builder()
                                                              .baseLocation("dummy base").region("dummy region")
                                                              .build()).build())
            .applicant1DQHearingSupport(HearingSupport.builder().requirements(List.of()).build())
            .applicant1DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .applicant1DQLanguage(WelshLanguageRequirements.builder().build())
            .applicant1DQVulnerabilityQuestions(VulnerabilityQuestions.builder()
                                                    .vulnerabilityAdjustmentsRequired(NO).build())
            .applicant1DQStatementOfTruth(StatementOfTruth.builder().name("Bob Jones").role("Solicitor").build())
            .build();
        return this;
    }

    public CaseDataBuilder applicant2DQWithLocation() {
        applicant2DQ = Applicant2DQ.builder()
            .applicant2DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                         .explainedToClient(List.of("OTHER"))
                                                         .oneMonthStayRequested(NO)
                                                         .reactionProtocolCompliedWith(YES)
                                                         .build())
            .applicant2DQDisclosureOfElectronicDocuments(DisclosureOfElectronicDocuments.builder()
                                                             .reachedAgreement(YES)
                                                             .build())
            .applicant2DQDisclosureOfNonElectronicDocuments(DisclosureOfNonElectronicDocuments.builder()
                                                                .directionsForDisclosureProposed(NO)
                                                                .build())
            .applicant2DQExperts(Experts.builder().expertRequired(NO).build())
            .applicant2DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
            .applicant2DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(NO).build())
            .applicant2DQRequestedCourt(RequestedCourt.builder()
                                            .responseCourtCode("court4")
                                            .caseLocation(CaseLocationCivil.builder()
                                                              .baseLocation("dummy base").region("dummy region")
                                                              .build()).build())
            .applicant2DQHearingSupport(HearingSupport.builder().requirements(List.of()).build())
            .applicant2DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .applicant2DQLanguage(WelshLanguageRequirements.builder().build())
            .applicant2DQVulnerabilityQuestions(VulnerabilityQuestions.builder()
                                                    .vulnerabilityAdjustmentsRequired(NO).build())
            .applicant2DQStatementOfTruth(StatementOfTruth.builder().name("Bob Jones").role("Solicitor").build())
            .build();
        return this;
    }

    public CaseDataBuilder applicant2DQ() {
        applicant2DQ = Applicant2DQ.builder()
            .applicant2DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                         .explainedToClient(List.of("OTHER"))
                                                         .oneMonthStayRequested(NO)
                                                         .reactionProtocolCompliedWith(YES)
                                                         .build())
            .applicant2DQDisclosureOfElectronicDocuments(DisclosureOfElectronicDocuments.builder()
                                                             .reachedAgreement(YES)
                                                             .build())
            .applicant2DQDisclosureOfNonElectronicDocuments(DisclosureOfNonElectronicDocuments.builder()
                                                                .directionsForDisclosureProposed(NO)
                                                                .build())
            .applicant2DQExperts(Experts.builder().expertRequired(NO).build())
            .applicant2DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
            .applicant2DQHearing(Hearing.builder().hearingLength(ONE_DAY).unavailableDatesRequired(NO).build())
            .applicant2DQRequestedCourt(RequestedCourt.builder().build())
            .applicant2DQHearingSupport(HearingSupport.builder()
                                            .supportRequirements(YES)
                                            .supportRequirementsAdditional("Additional support needed")
                                            .requirements(List.of()).build())
            .applicant2DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .applicant2DQLanguage(WelshLanguageRequirements.builder().build())
            .applicant2DQStatementOfTruth(StatementOfTruth.builder().name("Bob Jones").role("Solicitor").build())
            .build();
        return this;
    }

    public CaseDataBuilder applicant2DQ(Applicant2DQ applicant2DQ) {
        this.applicant2DQ = applicant2DQ;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1OrganisationDetails(SolicitorOrganisationDetails organisationDetails) {
        this.respondentSolicitor1OrganisationDetails = organisationDetails;
        return this;
    }

    public CaseDataBuilder respondentSolicitor2OrganisationDetails(SolicitorOrganisationDetails organisationDetails) {
        this.respondentSolicitor2OrganisationDetails = organisationDetails;
        return this;
    }

    public CaseDataBuilder applicant1ProceedWithClaim(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaim = yesOrNo;
        return this;
    }

    public CaseDataBuilder applicant1ProceedWithClaimSpec2v1(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimSpec2v1 = yesOrNo;
        return this;
    }

    public CaseDataBuilder applicant1ProceedWithClaimMultiParty2v1(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimMultiParty2v1 = yesOrNo;
        return this;
    }

    public CaseDataBuilder applicant2ProceedWithClaimMultiParty2v1(YesOrNo yesOrNo) {
        this.applicant2ProceedWithClaimMultiParty2v1 = yesOrNo;
        return this;
    }

    public CaseDataBuilder applicantsProceedIntention(YesOrNo yesOrNo) {
        this.applicantsProceedIntention = yesOrNo;
        return this;
    }

    public CaseDataBuilder applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = yesOrNo;
        return this;
    }

    public CaseDataBuilder applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = yesOrNo;
        return this;
    }

    public CaseDataBuilder respondent1ClaimResponseIntentionType(ResponseIntention responseIntention) {
        this.respondent1ClaimResponseIntentionType = responseIntention;
        return this;
    }

    public CaseDataBuilder respondent2ClaimResponseIntentionType(ResponseIntention responseIntention) {
        this.respondent2ClaimResponseIntentionType = responseIntention;
        return this;
    }

    public CaseDataBuilder claimValue(ClaimValue claimValue) {
        this.claimValue = claimValue;
        return this;
    }

    public CaseDataBuilder uploadParticularsOfClaim(YesOrNo uploadParticularsOfClaim) {
        this.uploadParticularsOfClaim = uploadParticularsOfClaim;
        return this;
    }

    public CaseDataBuilder issueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    public CaseDataBuilder hearingReferenceNumber(String hearingReference) {
        this.hearingReference = hearingReference;
        return this;
    }

    public CaseDataBuilder listingOrRelisting(ListingOrRelisting listingOrRelisting) {
        this.listingOrRelisting = listingOrRelisting;
        return this;
    }

    public CaseDataBuilder takenOfflineDate(LocalDateTime takenOfflineDate) {
        this.takenOfflineDate = takenOfflineDate;
        return this;
    }

    public CaseDataBuilder hearingDate(LocalDate hearingDate) {
        this.hearingDate = hearingDate;
        return this;
    }

    public CaseDataBuilder systemGeneratedCaseDocuments(List<Element<CaseDocument>> systemGeneratedCaseDocuments) {
        this.systemGeneratedCaseDocuments = systemGeneratedCaseDocuments;
        return this;
    }

    public CaseDataBuilder applicant1(Party party) {
        this.applicant1 = party;
        return this;
    }

    public CaseDataBuilder applicant2(Party party) {
        this.applicant2 = party;
        return this;
    }

    public CaseDataBuilder respondent1(Party party) {
        this.respondent1 = party;
        return this;
    }

    public CaseDataBuilder legacyCaseReference(String legacyCaseReference) {
        this.legacyCaseReference = legacyCaseReference;
        return this;
    }

    public CaseDataBuilder respondent1Represented(YesOrNo isRepresented) {
        this.respondent1Represented = isRepresented;
        return this;
    }

    public CaseDataBuilder respondent2Represented(YesOrNo isRepresented) {
        this.respondent2Represented = isRepresented;
        return this;
    }

    public CaseDataBuilder applicant1Represented(YesOrNo isRepresented) {
        this.applicant1Represented = isRepresented;
        return this;
    }

    public CaseDataBuilder defendant1LIPAtClaimIssued(YesOrNo defendant1LIPAtClaimIssued) {
        this.defendant1LIPAtClaimIssued = defendant1LIPAtClaimIssued;
        return this;
    }

    public CaseDataBuilder defendant2LIPAtClaimIssued(YesOrNo defendant2LIPAtClaimIssued) {
        this.defendant2LIPAtClaimIssued = defendant2LIPAtClaimIssued;
        return this;
    }

    public CaseDataBuilder respondent1OrgRegistered(YesOrNo respondent1OrgRegistered) {
        this.respondent1OrgRegistered = respondent1OrgRegistered;
        return this;
    }

    public CaseDataBuilder claimDetailsNotificationDate(LocalDateTime localDate) {
        this.claimDetailsNotificationDate = localDate;
        return this;
    }

    public CaseDataBuilder respondent2OrgRegistered(YesOrNo respondent2OrgRegistered) {
        this.respondent2OrgRegistered = respondent2OrgRegistered;
        return this;
    }

    public CaseDataBuilder claimProceedsInCaseman(ClaimProceedsInCaseman claimProceedsInCaseman) {
        this.claimProceedsInCaseman = claimProceedsInCaseman;
        return this;
    }

    public CaseDataBuilder claimProceedsInCasemanLR(ClaimProceedsInCasemanLR claimProceedsInCasemanLR) {
        this.claimProceedsInCasemanLR = claimProceedsInCasemanLR;
        return this;
    }

    public CaseDataBuilder applicant1OrganisationPolicy(OrganisationPolicy applicant1OrganisationPolicy) {
        this.applicant1OrganisationPolicy = applicant1OrganisationPolicy;
        return this;
    }

    public CaseDataBuilder respondent1OrganisationPolicy(OrganisationPolicy respondent1OrganisationPolicy) {
        this.respondent1OrganisationPolicy = respondent1OrganisationPolicy;
        return this;
    }

    public CaseDataBuilder respondent2OrganisationPolicy(OrganisationPolicy respondent2OrganisationPolicy) {
        this.respondent2OrganisationPolicy = respondent2OrganisationPolicy;
        return this;
    }

    public CaseDataBuilder addRespondent2(YesOrNo addRespondent2) {
        this.addRespondent2 = addRespondent2;
        return this;
    }

    public CaseDataBuilder addApplicant2(YesOrNo addApplicant2) {
        this.addApplicant2 = addApplicant2;
        return this;
    }

    public CaseDataBuilder addApplicant2() {
        this.addApplicant2 = YES;
        return this;
    }

    public CaseDataBuilder respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec respondentResponseTypeSpec) {
        this.respondent1ClaimResponseTypeForSpec = respondentResponseTypeSpec;
        return this;
    }

    public CaseDataBuilder respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec respondentResponseTypeSpec) {
        this.respondent2ClaimResponseTypeForSpec = respondentResponseTypeSpec;
        return this;
    }

    public CaseDataBuilder respondent1ClaimResponseType(RespondentResponseType respondent1ClaimResponseType) {
        this.respondent1ClaimResponseType = respondent1ClaimResponseType;
        return this;
    }

    public CaseDataBuilder respondent2ClaimResponseType(RespondentResponseType respondent2ClaimResponseType) {
        this.respondent2ClaimResponseType = respondent2ClaimResponseType;
        return this;
    }

    public CaseDataBuilder setRespondent1LitigationFriendCreatedDate(LocalDateTime createdDate) {
        this.respondent1LitigationFriendCreatedDate = createdDate;
        return this;
    }

    public CaseDataBuilder setRespondent1LitigationFriendDate(LocalDateTime date) {
        this.respondent1LitigationFriendDate = date;
        return this;
    }

    public CaseDataBuilder respondent2SameLegalRepresentative(YesOrNo respondent2SameLegalRepresentative) {
        this.respondent2SameLegalRepresentative = respondent2SameLegalRepresentative;
        return this;
    }

    public CaseDataBuilder caseReference(Long ccdCaseReference) {
        this.ccdCaseReference = ccdCaseReference;
        return this;
    }

    public CaseDataBuilder claimNotificationDeadline(LocalDateTime deadline) {
        this.claimNotificationDeadline = deadline;
        return this;
    }

    public CaseDataBuilder claimDismissedDate(LocalDateTime date) {
        this.claimDismissedDate = date;
        return this;
    }

    public CaseDataBuilder caseDismissedHearingFeeDueDate(LocalDateTime date) {
        this.caseDismissedHearingFeeDueDate = date;
        return this;
    }

    public CaseDataBuilder addLegalRepDeadline(LocalDateTime date) {
        this.addLegalRepDeadline = date;
        return this;
    }

    public CaseDataBuilder takenOfflineByStaffDate(LocalDateTime takenOfflineByStaffDate) {
        this.takenOfflineByStaffDate = takenOfflineByStaffDate;
        return this;
    }

    public CaseDataBuilder extensionDate(LocalDate extensionDate) {
        this.respondentSolicitor1AgreedDeadlineExtension = extensionDate;
        return this;
    }

    public CaseDataBuilder uiStatementOfTruth(StatementOfTruth statementOfTruth) {
        this.uiStatementOfTruth = statementOfTruth;
        return this;
    }

    public CaseDataBuilder defendantSolicitorNotifyClaimOptions(String defaultValue) {
        this.defendantSolicitorNotifyClaimOptions = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .label(defaultValue)
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilder defendantSolicitorNotifyClaimDetailsOptions(String defaultValue) {
        this.defendantSolicitorNotifyClaimDetailsOptions = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .label(defaultValue)
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilder selectLitigationFriend(String defaultValue) {
        this.selectLitigationFriend = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .label(defaultValue)
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilder respondent1ResponseDate(LocalDateTime date) {
        this.respondent1ResponseDate = date;
        return this;
    }

    public CaseDataBuilder respondent2ResponseDate(LocalDateTime date) {
        this.respondent2ResponseDate = date;
        return this;
    }

    public CaseDataBuilder applicant1ResponseDate(LocalDateTime date) {
        this.applicant1ResponseDate = date;
        return this;
    }

    public CaseDataBuilder reasonNotSuitableSDO(ReasonNotSuitableSDO reasonNotSuitableSDO) {
        this.reasonNotSuitableSDO = reasonNotSuitableSDO;
        return this;
    }

    public CaseDataBuilder atState(FlowState.Main flowState) {
        return atState(flowState, ONE_V_ONE);
    }

    public CaseDataBuilder atState(FlowState.Main flowState, MultiPartyScenario mpScenario) {
        switch (flowState) {
            case DRAFT:
                return atStateClaimDraft();
            case CLAIM_SUBMITTED:
                return atStateClaimSubmitted();
            case CLAIM_ISSUED_PAYMENT_SUCCESSFUL:
                return atStatePaymentSuccessful();
            case CLAIM_ISSUED_PAYMENT_FAILED:
                return atStateClaimIssuedPaymentFailed();
            case PENDING_CLAIM_ISSUED:
                return atStatePendingClaimIssued();
            case PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT:
                return atStatePendingClaimIssuedUnregisteredDefendant();
            case PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT:
                return atStatePendingClaimIssuedUnrepresentedDefendant();
            case PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT:
                return atStatePendingClaimIssuedUnrepresentedUnregisteredDefendant();
            case CLAIM_ISSUED:
                return atStateClaimIssued();
            case CLAIM_NOTIFIED:
                return atStateClaimNotified();
            case TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED:
                return atStateProceedsOfflineAfterClaimDetailsNotified();
            case TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED:
                return atStateProceedsOfflineAfterClaimNotified();
            case CLAIM_DETAILS_NOTIFIED:
                return atStateClaimDetailsNotified();
            case CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION:
                return atStateClaimDetailsNotifiedTimeExtension();
            case NOTIFICATION_ACKNOWLEDGED:
                return atStateNotificationAcknowledged();
            case NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION:
                return atStateNotificationAcknowledgedRespondent1TimeExtension();
            case AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED:
                return atStateAwaitingResponseFullDefenceReceived();
            case AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED:
                return atStateAwaitingResponseNotFullDefenceReceived();
            case FULL_DEFENCE:
                return atStateRespondentFullDefenceAfterNotificationAcknowledgement();
            case FULL_ADMISSION:
                return atStateRespondentFullAdmissionAfterNotificationAcknowledged();
            case PART_ADMISSION:
                return atStateRespondentPartAdmissionAfterNotificationAcknowledgement();
            case COUNTER_CLAIM:
                return atStateRespondentCounterClaim();
            case FULL_DEFENCE_PROCEED:
                return atStateApplicantRespondToDefenceAndProceed(mpScenario);
            case FULL_DEFENCE_NOT_PROCEED:
                return atStateApplicantRespondToDefenceAndNotProceed();
            case TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT:
                return atStateClaimIssuedUnrepresentedDefendants();
            case TAKEN_OFFLINE_UNREGISTERED_DEFENDANT:
                return atStateProceedsOfflineUnregisteredDefendants();
            case TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT:
                return atStateProceedsOfflineUnrepresentedDefendant1UnregisteredDefendant2();
            case TAKEN_OFFLINE_BY_STAFF:
                return atStateTakenOfflineByStaff();
            case CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE:
                return atStateClaimDismissed();
            case CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE:
                return atStateClaimDismissedPastClaimDetailsNotificationDeadline();
            case PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA:
                return atStatePastApplicantResponseDeadline();
            case TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE:
                return atStateTakenOfflinePastApplicantResponseDeadline();
            case CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE:
                return atStateClaimDismissedPastClaimNotificationDeadline();
            case TAKEN_OFFLINE_SDO_NOT_DRAWN:
                return atStateTakenOfflineSDONotDrawn(mpScenario);
            case TAKEN_OFFLINE_AFTER_SDO:
                return atStateTakenOfflineAfterSDO(mpScenario);
            default:
                throw new IllegalArgumentException("Invalid internal state: " + flowState);
        }
    }

    public CaseDataBuilder atStateClaimPastClaimNotificationDeadline() {
        atStateClaimIssued();
        ccdState = CASE_DISMISSED;
        claimNotificationDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateClaimDismissedPastClaimNotificationDeadline() {
        atStateClaimPastClaimNotificationDeadline();
        ccdState = CASE_DISMISSED;
        claimDismissedDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateClaimPastClaimDetailsNotificationDeadline() {
        atStateClaimNotified();
        claimDetailsNotificationDeadline = LocalDateTime.now().minusDays(5);
        return this;
    }

    public CaseDataBuilder atStateClaimDismissedPastClaimDetailsNotificationDeadline() {
        atStateClaimPastClaimDetailsNotificationDeadline();
        ccdState = CASE_DISMISSED;
        claimDismissedDate = claimNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateClaimDismissedPastHearingFeeDueDeadline() {
        atStateHearingFeeDueUnpaid();
        ccdState = CASE_DISMISSED;
        caseDismissedHearingFeeDueDate = LocalDateTime.now();
        hearingDate = hearingDueDate.plusWeeks(2);

        return this;
    }

    public CaseDataBuilder atStateClaimIssuedUnrepresentedDefendants() {
        atStatePendingClaimIssuedUnrepresentedDefendant();
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondentSolicitor1OrganisationDetails = null;
        addRespondent2 = YES;
        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
            .build();
        respondent2OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]")
            .build();
        respondent1OrgRegistered = null;
        respondent2OrgRegistered = null;
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v1UnrepresentedDefendant() {
        atStateClaimIssuedUnrepresentedDefendants();
        addRespondent2 = NO;
        respondent2 = null;
        respondent2Represented = null;
        respondent2OrganisationPolicy = null;
        respondent2OrgRegistered = null;
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v1UnrepresentedDefendantSpec() {
        atStateClaimIssuedUnrepresentedDefendants();
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        addRespondent2 = NO;
        respondent2 = null;
        respondent2Represented = null;
        respondent2OrganisationPolicy = null;
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v2UnrepresentedDefendant() {
        atStateClaimIssuedUnrepresentedDefendants();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        takenOfflineDate = LocalDateTime.now();
        respondentSolicitor2OrganisationDetails = null;
        respondent2OrganisationPolicy = null;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedUnrepresentedDefendant1() {
        atStatePendingClaimIssuedUnrepresentedDefendant();
        addRespondent2 = YES;
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondentSolicitor1OrganisationDetails = null;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        respondentSolicitor1OrganisationDetails = null;
        defendant1LIPAtClaimIssued = YES;
        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
            .build();
        respondent2OrganisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("QWERTY R2").build())
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]")
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedUnrepresentedDefendant2() {
        atStatePendingClaimIssuedUnrepresentedDefendant();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondentSolicitor2OrganisationDetails = null;
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        respondentSolicitor1OrganisationDetails = null;
        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("QWERTY R").build())
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
            .build();
        respondent2OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]")
            .build();
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineUnregisteredDefendants() {
        atStatePendingClaimIssuedUnregisteredDefendant();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
            .build();
        respondent2OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]")
            .build();
        respondent1OrgRegistered = NO;
        respondent2OrgRegistered = NO;
        respondent1Represented = YES;
        respondent2Represented = YES;
        respondent2SameLegalRepresentative = NO;

        respondentSolicitor1OrganisationDetails = SolicitorOrganisationDetails.builder()
            .email("testorg@email.com")
            .organisationName("test org name")
            .fax("123123123")
            .dx("test org dx")
            .phoneNumber("0123456789")
            .address(AddressBuilder.defaults().build())
            .build();

        respondentSolicitor2OrganisationDetails = SolicitorOrganisationDetails.builder()
            .email("testorg@email.com")
            .organisationName("test org name")
            .fax("123123123")
            .dx("test org dx")
            .phoneNumber("0123456789")
            .address(AddressBuilder.defaults().build())
            .build();
        return this;
    }

    public CaseDataBuilder atStateProceedsOffline1v1UnregisteredDefendant() {
        atStateProceedsOfflineUnregisteredDefendants();
        addRespondent2 = NO;
        respondent2 = null;
        respondent2Represented = null;
        respondent2OrgRegistered = null;
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineUnregisteredDefendant1() {
        atStatePendingClaimIssuedUnregisteredDefendant();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
            .build();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2SameLegalRepresentative = NO;

        respondentSolicitor1OrganisationDetails = SolicitorOrganisationDetails.builder()
            .email("testorg@email.com")
            .organisationName("test org name")
            .fax("123123123")
            .dx("test org dx")
            .phoneNumber("0123456789")
            .address(AddressBuilder.defaults().build())
            .build();
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineUnregisteredDefendant2() {
        atStatePendingClaimIssuedUnregisteredDefendant();
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent1OrgRegistered = YES;
        respondent2OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]")
            .build();
        respondent2Represented = YES;
        respondent2OrgRegistered = NO;
        respondent2SameLegalRepresentative = NO;

        respondentSolicitor1OrganisationDetails = SolicitorOrganisationDetails.builder()
            .email("testorg@email.com")
            .organisationName("test org name")
            .fax("123123123")
            .dx("test org dx")
            .phoneNumber("0123456789")
            .address(AddressBuilder.defaults().build())
            .build();
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineSameUnregisteredDefendant() {
        atStatePendingClaimIssuedUnregisteredDefendant();
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent1OrgRegistered = NO;
        respondent2OrganisationPolicy = null;
        respondent2Represented = YES;
        respondent2SameLegalRepresentative = YES;

        respondentSolicitor1OrganisationDetails = SolicitorOrganisationDetails.builder()
            .email("testorg@email.com")
            .organisationName("test org name")
            .fax("123123123")
            .dx("test org dx")
            .phoneNumber("0123456789")
            .address(AddressBuilder.defaults().build())
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1() {
        atStateClaimDetailsNotified();
        changeOfRepresentation(false, false, "New-sol-id", "Previous-sol-id", "previous-solicitor@example.com");
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotifiedWithNoticeOfChangeLip() {
        atStateClaimDetailsNotified();
        changeOfRepresentation(false, false, "New-sol-id", null, null);
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotifiedWithNoticeOfChangeOtherSol1Lip() {
        atStateClaimDetailsNotified();
        changeOfRepresentation(true, false, "New-sol-id", null, null);
        respondent1OrganisationPolicy = null;
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotifiedWithNoticeOfChangeOtherSol2Lip() {
        atStateClaimDetailsNotified();
        changeOfRepresentation(true, false, "New-sol-id", null, null);
        respondent2OrganisationPolicy = null;
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineUnrepresentedDefendant1UnregisteredDefendant2() {
        atStatePendingClaimIssuedUnrepresentedDefendant();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent2Represented = YES;
        respondent2OrgRegistered = NO;
        respondent2SameLegalRepresentative = NO;

        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
            .build();
        respondent2OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]")
            .build();

        respondent1Represented = NO;
        respondent1OrgRegistered = null;

        respondentSolicitor2OrganisationDetails = SolicitorOrganisationDetails.builder()
            .email("testorg2@email.com")
            .organisationName("test org name 2")
            .fax("123123123")
            .dx("test org dx 2")
            .phoneNumber("0123456789")
            .address(AddressBuilder.defaults().build())
            .build();
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineUnregisteredDefendant1UnrepresentedDefendant2() {
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        atStatePendingClaimIssuedUnrepresentedDefendant();
        addRespondent2 = YES;
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2SameLegalRepresentative = NO;

        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
            .build();
        respondent2OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]")
            .build();

        respondent2OrgRegistered = null;

        respondentSolicitor1OrganisationDetails = SolicitorOrganisationDetails.builder()
            .email("testorg2@email.com")
            .organisationName("test org name 2")
            .fax("123123123")
            .dx("test org dx 2")
            .phoneNumber("0123456789")
            .address(AddressBuilder.defaults().build())
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimDiscontinued() {
        atStateClaimDetailsNotified();
        return discontinueClaim();
    }

    public CaseDataBuilder discontinueClaim() {
        this.ccdState = CASE_DISMISSED;
        this.discontinueClaim = CloseClaim.builder()
            .date(LocalDate.now())
            .reason("My reason")
            .build();
        return this;
    }

    public CaseDataBuilder discontinueClaim(CloseClaim closeClaim) {
        this.discontinueClaim = closeClaim;
        return this;
    }

    public CaseDataBuilder discontinueClaimFrom(FlowState.Main flowState) {
        atState(flowState);
        this.ccdState = CASE_DISMISSED;
        this.discontinueClaim = CloseClaim.builder()
            .date(LocalDate.now())
            .reason("My reason")
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimWithdrawn() {
        atStateClaimDetailsNotified();
        return withdrawClaim();
    }

    public CaseDataBuilder withdrawClaim(CloseClaim closeClaim) {
        this.withdrawClaim = closeClaim;
        return this;
    }

    public CaseDataBuilder withdrawClaim() {
        this.ccdState = CASE_DISMISSED;
        this.withdrawClaim = CloseClaim.builder()
            .date(LocalDate.now())
            .reason("My reason")
            .build();
        return this;
    }

    public CaseDataBuilder withdrawClaimFrom(FlowState.Main flowState) {
        atState(flowState);
        this.ccdState = CASE_DISMISSED;
        this.withdrawClaim = CloseClaim.builder()
            .date(LocalDate.now())
            .reason("My reason")
            .build();
        return this;
    }

    public CaseDataBuilder claimDismissedDeadline(LocalDateTime date) {
        this.claimDismissedDeadline = date;
        return this;
    }

    public CaseDataBuilder courtLocation_missing() {
        this.courtLocation = null;
        return this;
    }

    public CaseDataBuilder courtLocation_old() {
        this.courtLocation = CourtLocation.builder()
            .applicantPreferredCourt("127").build();
        return this;
    }

    public CaseDataBuilder courtLocation() {
        this.courtLocation = CourtLocation.builder()
            .applicantPreferredCourt("127")
            .caseLocation(CaseLocationCivil.builder()
                              .region("2")
                              .baseLocation("000000")
                              .build())
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimDraft() {
        solicitorReferences = SolicitorReferences.builder()
            .applicantSolicitor1Reference("12345")
            .respondentSolicitor1Reference("6789")
            .build();
        courtLocation = CourtLocation.builder()
            .applicantPreferredCourt("214320")
            .applicantPreferredCourtLocationList(
                DynamicList.builder().value(DynamicListElement.builder().label("sitename").build()).build())
            .caseLocation(CaseLocationCivil.builder()
                              .region("10")
                              .baseLocation("214320")
                              .build())
            .build();
        uploadParticularsOfClaim = NO;
        claimValue = ClaimValue.builder()
            .statementOfValueInPennies(BigDecimal.valueOf(10000000))
            .build();
        claimType = ClaimType.PERSONAL_INJURY;
        personalInjuryType = ROAD_ACCIDENT;
        applicantSolicitor1PbaAccounts = DynamicList.builder()
            .value(DynamicListElement.builder().label("PBA0077597").build())
            .build();
        claimFee = Fee.builder()
            .version("1")
            .code("CODE")
            .calculatedAmountInPence(BigDecimal.valueOf(100))
            .build();
        applicant1 = PartyBuilder.builder().individual().build().toBuilder().partyID("app-1-party-id").build();
        respondent1 = PartyBuilder.builder().soleTrader().build().toBuilder().partyID("res-1-party-id").build();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        respondent2OrgRegistered = YES;
        applicant1OrganisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("QWERTY A").build())
            .build();
        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("QWERTY R").build())
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
            .build();
        respondent2OrganisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("QWERTY R2").build())
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]")
            .build();
        respondentSolicitor1EmailAddress = "respondentsolicitor@example.com";
        respondentSolicitor2EmailAddress = "respondentsolicitor2@example.com";
        applicantSolicitor1UserDetails = IdamUserDetails.builder().email("applicantsolicitor@example.com").build();
        applicantSolicitor1ClaimStatementOfTruth = StatementOfTruthBuilder.defaults().build();
        applicantSolicitor1CheckEmail = CorrectEmail.builder().email("hmcts.civil@gmail.com").correct(YES).build();
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantSolicitorsUnregistered() {
        atStateClaimDraft();
        respondent1OrganisationPolicy = null;
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;

        addRespondent2 = YES;
        respondent2OrganisationPolicy = null;
        respondent2Represented = YES;
        respondent2OrgRegistered = NO;
        respondent2SameLegalRepresentative = NO;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantLips() {
        atStateClaimDraft();
        respondent1OrganisationPolicy = null;
        defendant1LIPAtClaimIssued = YES;
        respondent1Represented = NO;
        respondent2Represented = NO;
        addRespondent2 = YES;
        respondent2OrganisationPolicy = null;
        respondent2SameLegalRepresentative = NO;
        defendant2LIPAtClaimIssued = YES;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendant1Lip1Lr() {
        atStateClaimDraft();
        respondent1OrganisationPolicy = null;
        defendant1LIPAtClaimIssued = YES;

        addRespondent2 = YES;
        respondent2OrganisationPolicy = null;
        respondent2SameLegalRepresentative = NO;
        defendant2LIPAtClaimIssued = NO;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendant1Lr1Lip() {
        atStateClaimDraft();
        respondent1OrganisationPolicy = null;
        defendant1LIPAtClaimIssued = NO;

        addRespondent2 = YES;
        respondent2OrganisationPolicy = null;
        respondent2SameLegalRepresentative = NO;
        defendant2LIPAtClaimIssued = YES;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted() {
        atStateClaimDraft();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = FAST_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        submittedDate = SUBMITTED_DATE_TIME;
        claimIssuedPaymentDetails = PaymentDetails.builder().customerReference("12345").build();
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedSmallClaim() {
        atStateClaimDraft();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = SMALL_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        submittedDate = SUBMITTED_DATE_TIME;
        totalClaimAmount = BigDecimal.valueOf(800);
        claimIssuedPaymentDetails = PaymentDetails.builder().customerReference("12345").build();
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedMultiClaim() {
        atStateClaimDraft();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = MULTI_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        submittedDate = SUBMITTED_DATE_TIME;
        claimIssuedPaymentDetails = PaymentDetails.builder().customerReference("12345").build();
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedSpec() {
        atStateClaimDraft();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = FAST_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        submittedDate = SUBMITTED_DATE_TIME;
        caseAccessCategory = SPEC_CLAIM;
        claimIssuedPaymentDetails = PaymentDetails.builder().customerReference("12345").build();
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedOneRespondentRepresentative() {
        atStateClaimSubmitted();
        addRespondent2 = NO;
        defendant1LIPAtClaimIssued = NO;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedTwoRespondentRepresentatives() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedTwoRespondentRepresentativesBothUnregistered() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent2Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedRespondent1Unregistered() {
        atStateClaimSubmitted();
        respondent1OrgRegistered = NO;

        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedNoRespondentRepresented() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent1OrgRegistered = null;
        respondent2OrgRegistered = null;
        respondent1Represented = NO;
        respondent2Represented = NO;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted1v1AndNoRespondentRepresented() {
        atStateClaimSubmitted();
        addRespondent2 = NO;
        respondent2SameLegalRepresentative = NO;
        respondent1Represented = NO;
        respondent2Represented = NO;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        respondent2Represented = NO;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent1Represented = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v2AndSameRepresentative() {
        atStatePaymentSuccessful();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        addRespondent2 = YES;
        respondent2Represented = YES;
        respondent2SameLegalRepresentative = YES;
        respondent1OrganisationPolicy =
            OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationID("org1").build())
                .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
                .orgPolicyReference("org1PolicyReference")
                .build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v2AndSameUnregisteredRepresentative() {
        atStateClaimIssued1v2AndSameRepresentative();
        respondent1OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted2v1RespondentUnrepresented() {
        atStateClaimSubmitted();
        multiPartyClaimTwoApplicants();
        respondent1Represented = NO;
        respondent1OrgRegistered = null;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted2v1RespondentRegistered() {
        atStateClaimSubmitted();
        multiPartyClaimTwoApplicants();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted2v1RespondentUnregistered() {
        atStateClaimSubmitted();
        multiPartyClaimTwoApplicants();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v2AndBothDefendantsDefaultJudgment() {
        defendantDetails = DynamicList.builder()
            .value(DynamicListElement.builder().label("Both Defendants").build())
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v2AndOneDefendantDefaultJudgment() {
        defendantDetails = DynamicList.builder()
            .value(DynamicListElement.builder().label("Mr. Sole Trader").build())
            .build();
        return this;
    }

    public CaseDataBuilder atRespondToClaimWithSingleUnAvailabilityDate() {

        return  this;
    }

    public CaseDataBuilder atStateSdoFastTrackTrial() {
        fastTrackHearingTime = FastTrackHearingTime.builder()
            .helpText1("If either party considers that the time estimate is insufficient, "
                           + "they must inform the court within 7 days of the date of this order.")
            .helpText2("Not more than seven nor less than three clear days before the trial, "
                           + "the claimant must file at court and serve an indexed and paginated bundle of "
                           + "documents which complies with the requirements of Rule 39.5 Civil Procedure Rules "
                           + "and which complies with requirements of PD32. The parties must endeavour to agree "
                           + "the contents of the bundle before it is filed. The bundle will include a case "
                           + "summary and a chronology.")
            .hearingDuration(FastTrackHearingTimeEstimate.ONE_HOUR)
            .dateFrom(LocalDate.parse("2022-01-01"))
            .dateTo(LocalDate.parse("2022-01-02"))
            .dateToToggle(List.of(DateToShowToggle.SHOW))
            .build();
        fastTrackOrderWithoutJudgement = FastTrackOrderWithoutJudgement.builder()
            .input(String.format("Each party has the right to apply "
                                     + "to have this Order set aside or varied. Any such application must be "
                                     + "received by the Court (together with the appropriate fee) by 4pm "
                                     + "on %s.",
                                 LocalDate.parse("2022-01-30")))
            .build();
        return this;
    }

    public CaseDataBuilder atStateSdoDisposal() {
        disposalOrderWithoutHearing = DisposalOrderWithoutHearing.builder()
             .input(String.format(
            "Each party has the right to apply to have this Order set "
                + "aside or varied. Any such application must be received "
                + "by the Court (together with the appropriate fee) "
                + "by 4pm on %s.", LocalDate.parse("2022-01-30")))
            .build();
        disposalHearingHearingTime = DisposalHearingHearingTime.builder()
            .input("This claim will be listed for final disposal before a judge on the first available date after")
            .time(FIFTEEN_MINUTES)
            .dateFrom(LocalDate.parse("2022-01-01"))
            .dateFrom(LocalDate.parse("2022-01-02"))
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalHearing() {
        caseManagementOrderSelection = DISPOSAL_HEARING;

        disposalHearingJudgesRecitalDJ = DisposalHearingJudgesRecitalDJ
            .builder()
            .judgeNameTitle("test name")
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialHearing() {
        caseManagementOrderSelection = "TRIAL_HEARING";

        trialHearingJudgesRecitalDJ = TrialHearingJudgesRecital
            .builder()
            .judgeNameTitle("test name")
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimantRequestsDJWithUnavailableDates() {
        HearingDates singleDate = HearingDates.builder()
            .hearingUnavailableFrom(LocalDate.of(2023, 8, 20))
            .hearingUnavailableUntil(LocalDate.of(2023, 8, 20))
            .build();

        HearingDates dateRange = HearingDates.builder()
            .hearingUnavailableFrom(LocalDate.of(2023, 8, 20))
            .hearingUnavailableUntil(LocalDate.of(2023, 8, 22))
            .build();

        this.hearingSupportRequirementsDJ = HearingSupportRequirementsDJ.builder()
            .hearingUnavailableDates(YES)
            .hearingDates(wrapElements(List.of(singleDate, dateRange)))
            .build();

        this.defaultJudgmentDocuments.addAll(wrapElements(CaseDocument.builder()
                                                              .documentName("test")
                                                              .createdDatetime(LocalDateTime.now())
                                                              .build()));
        return this;
    }

    private DynamicList getHearingMethodList(String key, String value) {
        Category category = Category.builder().categoryKey("HearingChannel").key(key).valueEn(value).activeFlag("Y").build();
        DynamicList hearingMethodList = DynamicList.fromList(List.of(category), Category::getValueEn, null, false);
        hearingMethodList.setValue(hearingMethodList.getListItems().get(0));
        return hearingMethodList;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalHearingSDOInPersonHearing() {
        DynamicList hearingMethodList = getHearingMethodList("INTER", "In Person");
        hearingMethodValuesDisposalHearing = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalHearingSDOTelephoneHearing() {
        DynamicList hearingMethodList = getHearingMethodList("TEL", "Telephone");
        hearingMethodValuesDisposalHearing = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalHearingSDOVideoHearing() {
        DynamicList hearingMethodList = getHearingMethodList("VID", "Video");
        hearingMethodValuesDisposalHearing = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedFastTrackSDOInPersonHearing() {
        DynamicList hearingMethodList = getHearingMethodList("INTER", "In Person");
        hearingMethodValuesFastTrack = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedFastTrackSDOTelephoneHearing() {
        DynamicList hearingMethodList = getHearingMethodList("TEL", "Telephone");
        hearingMethodValuesFastTrack = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedFastTrackSDOVideoHearing() {
        DynamicList hearingMethodList = getHearingMethodList("VID", "Video");
        hearingMethodValuesFastTrack = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedSmallClaimsSDOInPersonHearing() {
        DynamicList hearingMethodList = getHearingMethodList("INTER", "In Person");
        hearingMethodValuesSmallClaims = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedSmallClaimsSDOTelephoneHearing() {
        DynamicList hearingMethodList = getHearingMethodList("TEL", "Telephone");
        hearingMethodValuesSmallClaims = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedSmallClaimsSDOVideoHearing() {
        DynamicList hearingMethodList = getHearingMethodList("VID", "Video");
        hearingMethodValuesSmallClaims = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialSDOInPersonHearingNew() {
        DynamicList hearingMethodList = getHearingMethodList("INTER", "In Person");
        hearingMethodValuesTrialHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialDJInPersonHearingNew() {
        DynamicList hearingMethodList = getHearingMethodList("INTER", "In Person");
        hearingMethodValuesTrialHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialSDOInPersonHearing() {
        trialHearingMethodDJ = DisposalHearingMethodDJ.disposalHearingMethodInPerson;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialSDOTelephoneHearingNew() {
        DynamicList hearingMethodList = getHearingMethodList("TEL", "Telephone");
        hearingMethodValuesTrialHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialSDOTelephoneHearing() {
        trialHearingMethodDJ = DisposalHearingMethodDJ.disposalHearingMethodTelephoneHearing;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialSDOVideoHearingNew() {
        DynamicList hearingMethodList = getHearingMethodList("VID", "Video");
        hearingMethodValuesTrialHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialSDOVideoHearing() {
        trialHearingMethodDJ = DisposalHearingMethodDJ.disposalHearingMethodVideoConferenceHearing;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialLocationInPerson() {
        trialHearingMethodInPersonDJ = DynamicList.builder().value(
            DynamicListElement.builder().label("Court 1").build()).build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedCaseManagementLocationInPerson() {
        caseManagementLocation = CaseLocationCivil.builder().baseLocation("0123").region("0321").build();
        return this;
    }

    public CaseDataBuilder atStateSdoTrialDj() {
        List<DateToShowToggle> dateToShowTrue = List.of(DateToShowToggle.SHOW);
        trialHearingTimeDJ = TrialHearingTimeDJ.builder()
            .helpText1("If either party considers that the time estimate is insufficient, "
                           + "they must inform the court within 7 days of the date of this order.")
            .helpText2("Not more than seven nor less than three clear days before the trial, "
                           + "the claimant must file at court and serve an indexed and paginated bundle of "
                           + "documents which complies with the requirements of Rule 39.5 Civil Procedure Rules "
                           + "and which complies with requirements of PD32. The parties must endeavour to agree "
                           + "the contents of the bundle before it is filed. The bundle will include a case "
                           + "summary and a chronology.")
            .hearingTimeEstimate(TrialHearingTimeEstimateDJ.ONE_HOUR)
            .dateToToggle(dateToShowTrue)
            .date1(LocalDate.now().plusWeeks(22))
            .date2(LocalDate.now().plusWeeks(30))
            .build();
        trialOrderMadeWithoutHearingDJ = TrialOrderMadeWithoutHearingDJ.builder()
            .input("This order has been made without a hearing. "
                    + "Each party has the right to apply to have this Order "
                    + "set aside or varied. Any such application must be "
                    + "received by the Court "
                    + "(together with the appropriate fee) by 4pm on 01 12 2022.")
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialHearingInfo() {
        trialHearingTrialDJ = TrialHearingTrial
            .builder()
            .input1("The time provisionally allowed for the trial is")
            .date1(LocalDate.now().plusWeeks(22))
            .date2(LocalDate.now().plusWeeks(34))
            .input2("If either party considers that the time estimates is"
                        + " insufficient, they must inform the court within "
                        + "7 days of the date of this order.")
            .input3("Not more than seven nor less than three clear days before "
                        + "the trial, the claimant must file at court and serve an"
                        + "indexed and paginated bundle of documents which complies"
                        + " with the requirements of Rule 39.5 Civil "
                        + "Procedure Rules"
                        + " and Practice Direction 39A. The parties must "
                        + "endeavour to agree the contents of the "
                        + "bundle before it is filed. "
                        + "The bundle will include a case summary"
                        + " and a chronology.")
            .type(List.of(DisposalHearingBundleType.DOCUMENTS))
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalHearingInPerson() {
        disposalHearingBundleDJ = DisposalHearingBundleDJ.builder()
            .input("The claimant must lodge at court at least 7 "
                       + "days before the disposal")
            .type(List.of(DisposalHearingBundleType.DOCUMENTS))
            .build();
        disposalHearingFinalDisposalHearingDJ = DisposalHearingFinalDisposalHearingDJ
            .builder()
            .input("This claim be listed for final "
                       + "disposal before a Judge on the first "
                       + "available date after.")
            .date(LocalDate.now().plusWeeks(16))
            .time(DisposalHearingFinalDisposalHearingTimeEstimate.THIRTY_MINUTES)
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalHearingInPersonDJ() {
        disposalHearingFinalDisposalHearingTimeDJ = DisposalHearingFinalDisposalHearingTimeDJ
            .builder()
            .input("This claim be listed for final "
                       + "disposal before a Judge on the first "
                       + "available date after.")
            .date(LocalDate.now().plusWeeks(16))
            .time(uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate.THIRTY_MINUTES)
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalSDOInPerson() {
        DynamicList hearingMethodList = getHearingMethodList("INTER", "In Person");
        hearingMethodValuesDisposalHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalSDOTelephoneCall() {
        DynamicList hearingMethodList = getHearingMethodList("TEL", "Telephone");
        hearingMethodValuesDisposalHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalSDOVideoCallNew() {
        DynamicList hearingMethodList = getHearingMethodList("VID", "Video");
        hearingMethodValuesDisposalHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalDJVideoCallNew() {
        DynamicList hearingMethodList = getHearingMethodList("VID", "Video");
        hearingMethodValuesDisposalHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalSDOVideoCall() {
        disposalHearingMethodDJ = DisposalHearingMethodDJ.disposalHearingMethodVideoConferenceHearing;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted1v2Respondent2OrgNotRegistered() {
        atStateClaimIssued();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedPaymentFailed() {
        atStateClaimSubmitted();

        claimIssuedPaymentDetails = PaymentDetails.builder()
            .status(FAILED)
            .errorMessage("Your account is deleted")
            .errorCode("CA-E0004")
            .build();
        return this;
    }

    public CaseDataBuilder atStatePaymentFailed() {
        atStateClaimSubmitted();

        paymentDetails = PaymentDetails.builder()
            .status(FAILED)
            .errorMessage("Your account is deleted")
            .errorCode("CA-E0004")
            .build();
        return this;
    }

    public CaseDataBuilder atStatePaymentSuccessful() {
        atStateClaimSubmitted();
        claimIssuedPaymentDetails = PaymentDetails.builder()
            .status(SUCCESS)
            .reference("RC-1604-0739-2145-4711")
            .build();
        paymentReference = "12345";
        paymentSuccessfulDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStatePaymentSuccessfulWithoutPaymentSuccessDate() {
        atStateClaimSubmitted();
        claimIssuedPaymentDetails = PaymentDetails.builder()
            .status(SUCCESS)
            .reference("RC-1604-0739-2145-4711")
            .build();
        paymentReference = "12345";
        return this;
    }

    public CaseDataBuilder atStatePaymentSuccessfulWithCopyOrganisationOnly() {
        atStatePaymentSuccessful();
        respondent1OrganisationIDCopy = respondent1OrganisationPolicy.getOrganisation().getOrganisationID();
        respondent1OrganisationPolicy = respondent1OrganisationPolicy.toBuilder()
            .organisation(Organisation.builder().build()).build();
        return this;
    }

    public CaseDataBuilder atStatePendingClaimIssued() {
        atStatePaymentSuccessful();
        issueDate = CLAIM_ISSUED_DATE;
        return this;
    }

    public CaseDataBuilder atStatePendingClaimIssuedUnregisteredDefendant() {
        atStatePaymentSuccessful();
        issueDate = CLAIM_ISSUED_DATE;
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2OrgRegistered = NO;
        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
            .build();
        respondent2OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]")
            .build();
        return this;
    }

    public CaseDataBuilder atStatePendingClaimIssuedUnrepresentedDefendant() {
        atStatePaymentSuccessful();
        issueDate = CLAIM_ISSUED_DATE;
        respondent1Represented = NO;
        respondent1OrgRegistered = NO;
        respondent2Represented = NO;
        respondent2OrgRegistered = NO;
        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
            .build();
        respondent2OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]")
            .build();
        return this;
    }

    public CaseDataBuilder atStatePendingClaimIssuedUnrepresentedUnregisteredDefendant() {
        atStatePaymentSuccessful();
        issueDate = CLAIM_ISSUED_DATE;
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2Represented = NO;
        respondent2OrgRegistered = NO;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
            .build();
        respondent2OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]")
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssued() {
        atStatePendingClaimIssued();
        claimNotificationDeadline = NOTIFICATION_DEADLINE;
        ccdState = CASE_ISSUED;
        buildHmctsInternalCaseName();
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v1LiP() {
        atStatePendingClaimIssued();
        ccdState = CASE_ISSUED;
        respondent1Represented = NO;
        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
            .build();
        addLegalRepDeadline = DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v2Respondent2LiP() {
        atStatePendingClaimIssued();
        ccdState = CASE_ISSUED;
        respondent2Represented = NO;
        respondent2OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
            .build();
        addLegalRepDeadline = DEADLINE;
        return this;
    }

    public CaseDataBuilder changeOrganisationRequestField(boolean isApplicant, boolean isRespondent2Replaced,
                                                          String newOrgID, String oldOrgId, String email) {
        String caseRole = isApplicant ? CaseRole.APPLICANTSOLICITORONE.getFormattedName() :
            isRespondent2Replaced ? CaseRole.RESPONDENTSOLICITORTWO.getFormattedName() :
                CaseRole.RESPONDENTSOLICITORONE.getFormattedName();
        changeOrganisationRequest = ChangeOrganisationRequest.builder()
            .requestTimestamp(LocalDateTime.now())
            .createdBy(email)
            .caseRoleId(DynamicList.builder()
                            .value(DynamicListElement.builder()
                                       .code(caseRole)
                                       .label(caseRole)
                                       .build())
                            .build())
            .organisationToAdd(Organisation.builder()
                                   .organisationID(newOrgID)
                                   .build())
            .organisationToRemove(Organisation.builder()
                                      .organisationID(oldOrgId)
                                      .build())
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .build();
        return this;
    }

    public CaseDataBuilder changeOfRepresentation(boolean isApplicant, boolean isRespondent2Replaced,
                                                  String newOrgID, String oldOrgId, String formerSolicitorEmail) {
        String caseRole = isApplicant ? CaseRole.APPLICANTSOLICITORONE.getFormattedName() :
            isRespondent2Replaced ? CaseRole.RESPONDENTSOLICITORTWO.getFormattedName() :
                CaseRole.RESPONDENTSOLICITORONE.getFormattedName();
        ChangeOfRepresentation.ChangeOfRepresentationBuilder newChangeBuilder = ChangeOfRepresentation.builder()
            .caseRole(caseRole)
            .organisationToAddID(newOrgID)
            .organisationToRemoveID(oldOrgId)
            .timestamp(LocalDateTime.now());
        if (oldOrgId != null) {
            newChangeBuilder.organisationToRemoveID(oldOrgId);
        }
        if (formerSolicitorEmail != null) {
            newChangeBuilder.formerRepresentationEmailAddress(formerSolicitorEmail);
        }
        changeOfRepresentation = newChangeBuilder.build();
        return this;
    }

    public CaseDataBuilder updateOrgPolicyAfterNoC(boolean isApplicant, boolean isRespondent2, String newOrgId) {
        if (isApplicant) {
            applicant1OrganisationPolicy = OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationID(newOrgId).build())
                .orgPolicyCaseAssignedRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName()).build();
        } else {
            if (isRespondent2) {
                respondent2OrganisationPolicy = OrganisationPolicy.builder()
                    .organisation(Organisation.builder().organisationID(newOrgId).build())
                    .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName()).build();
            } else {
                respondent1OrganisationPolicy = OrganisationPolicy.builder()
                    .organisation(Organisation.builder().organisationID(newOrgId).build())
                    .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()).build();
            }
        }
        return this;
    }

    public CaseDataBuilder atStateClaimNotified() {
        atStateClaimIssued();
        claimNotificationDate = issueDate.plusDays(1).atStartOfDay();
        claimDetailsNotificationDeadline = DEADLINE;
        ccdState = AWAITING_CASE_DETAILS_NOTIFICATION;
        servedDocumentFiles = ServedDocumentFiles.builder().particularsOfClaimText("test").build();
        return this;
    }

    public CaseDataBuilder atStateClaimNotified_1v1() {
        atStateClaimNotified();
        defendantSolicitorNotifyClaimOptions = null;
        return this;
    }

    public CaseDataBuilder atStateClaimNotified_1v2_andNotifyBothSolicitors() {
        atStateClaimNotified();
        multiPartyClaimTwoDefendantSolicitors();
        defendantSolicitorNotifyClaimOptions("Both");
        return this;
    }

    public CaseDataBuilder atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor() {
        atStateClaimNotified();
        multiPartyClaimTwoDefendantSolicitors();
        defendantSolicitorNotifyClaimOptions("Defendant One: Solicitor A");
        return this;
    }

    public CaseDataBuilder atStateClaimNotified1v1LiP(CertificateOfService  certificateOfService) {
        atStatePendingClaimIssued();
        ccdState = CASE_ISSUED;
        respondent1Represented = NO;
        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
            .build();
        defendant1LIPAtClaimIssued = YES;
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        cosNotifyClaimDefendant1 = certificateOfService;
        claimDetailsNotificationDeadline = DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateClaimNotified1v2RespondentLiP() {
        atStatePendingClaimIssued();
        ccdState = CASE_ISSUED;
        respondent2Represented = NO;
        respondent2OrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
            .build();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        claimDetailsNotificationDeadline = DEADLINE;
        defendant2LIPAtClaimIssued = YES;
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineAfterClaimNotified() {
        atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineAfterClaimDetailsNotified() {
        atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotified() {
        atStateClaimNotified();
        claimDetailsNotificationDate = claimNotificationDate.plusDays(1);
        claimDismissedDeadline = LocalDateTime.now().plusMonths(6);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotified1v1() {
        atStateClaimNotified();
        claimDetailsNotificationDate = claimNotificationDate.plusDays(1);
        claimDismissedDeadline = LocalDateTime.now().plusMonths(6);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        respondent2OrgRegistered = null;
        respondent2Represented = null;
        addRespondent2 = null;
        return this;
    }

    public CaseDataBuilder atStatePastResponseDeadline() {
        atStateClaimDetailsNotified1v1();
        respondent1ResponseDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors() {
        atStateClaimDetailsNotified();
        multiPartyClaimTwoDefendantSolicitors();
        defendantSolicitorNotifyClaimDetailsOptions("Both");
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor() {
        atStateClaimDetailsNotified();
        multiPartyClaimTwoDefendantSolicitors();
        defendantSolicitorNotifyClaimDetailsOptions("Defendant One: Solicitor");
        return this;
    }

    public CaseDataBuilder atStateAwaitingResponseFullDefenceReceived() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();
        claimDetailsNotificationDate = claimNotificationDate.plusDays(1);
        claimDismissedDeadline = LocalDateTime.now().plusMonths(6);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        return this;
    }

    public CaseDataBuilder atStateAwaitingResponseNotFullDefenceReceived() {
        atStateAwaitingResponseNotFullDefenceReceived(RespondentResponseType.FULL_ADMISSION);
        return this;
    }

    public CaseDataBuilder atStateAwaitingResponseNotFullDefenceReceived(RespondentResponseType responseType) {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = responseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        claimDetailsNotificationDate = claimNotificationDate.plusDays(1);
        claimDismissedDeadline = LocalDateTime.now().plusMonths(6);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        return this;
    }

    public CaseDataBuilder atStateAddLitigationFriend_1v2_SameSolicitor() {
        atStateClaimDetailsNotified();
        multiPartyClaimOneDefendantSolicitor();
        addGenericRespondentLitigationFriend();
        return this;
    }

    public CaseDataBuilder atStateAddRespondent1LitigationFriend_1v2_SameSolicitor() {
        atStateClaimDetailsNotified();
        multiPartyClaimOneDefendantSolicitor();
        addRespondent1LitigationFriend();
        return this;
    }

    public CaseDataBuilder atStateAddRespondent2LitigationFriend_1v2_SameSolicitor() {
        atStateClaimDetailsNotified();
        multiPartyClaimOneDefendantSolicitor();
        addRespondent2LitigationFriend();
        return this;
    }

    public CaseDataBuilder atStateAddRespondent1LitigationFriend_1v2_DiffSolicitor() {
        return atStateAddRespondent1LitigationFriend_1v2_SameSolicitor()
            .respondent2SameLegalRepresentative(NO);
    }

    public CaseDataBuilder atStateAddRespondent2LitigationFriend_1v2_DiffSolicitor() {
        return atStateAddRespondent2LitigationFriend_1v2_SameSolicitor()
            .respondent2SameLegalRepresentative(NO);
    }

    public CaseDataBuilder atStateClaimDetailsNotifiedTimeExtension() {
        atStateClaimDetailsNotified();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondent1TimeExtensionDate = claimDetailsNotificationDate.plusDays(1);
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDate = null;
        return this;
    }

    public CaseDataBuilder atState1v2DifferentSolicitorClaimDetailsRespondent1NotifiedTimeExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        respondent2SameLegalRepresentative = NO;
        return this;
    }

    public CaseDataBuilder atState1v2DifferentSolicitorClaimDetailsRespondent2NotifiedTimeExtension() {
        atStateClaimDetailsNotified();
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        respondent2SameLegalRepresentative = NO;
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        respondent2TimeExtensionDate = claimDetailsNotificationDate.plusDays(1);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        return this;
    }

    public CaseDataBuilder atState1v2SameSolicitorClaimDetailsRespondentNotifiedTimeExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        respondent2SameLegalRepresentative = YES;
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotifiedTimeExtension_Defendent2() {
        atStateClaimDetailsNotified();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        respondent2TimeExtensionDate = claimDetailsNotificationDate.plusDays(1);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotifiedTimeExtension1v2() {
        atStateClaimDetailsNotified();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondent1TimeExtensionDate = claimDetailsNotificationDate.plusDays(1);
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        respondent2TimeExtensionDate = claimDetailsNotificationDate.plusDays(1);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaff() {
        atStateClaimIssued();
        takenOfflineByStaff();
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffSpec() {
        atStateClaimIssued();
        takenOfflineByStaffSpec();
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffSpec1v2SS() {
        atStateClaimIssued();
        multiPartyClaimTwoDefendantSameSolicitorsSpec();
        takenOfflineByStaffSpec();
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterClaimNotified() {
        atStateClaimNotified();
        takenOfflineByStaff();
        takenOfflineByStaffDate = claimNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterClaimDetailsNotified() {
        atStateClaimDetailsNotified();
        takenOfflineByStaff();
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterClaimDetailsNotifiedExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        takenOfflineByStaff();
        takenOfflineByStaffDate = respondent1TimeExtensionDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterNotificationAcknowledged() {
        atStateNotificationAcknowledged();
        takenOfflineByStaff();
        takenOfflineByStaffDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterDefendantResponse() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        takenOfflineByStaff();
        takenOfflineByStaffDate = respondent1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension() {
        atStateNotificationAcknowledgedRespondent1TimeExtension();
        takenOfflineByStaff();
        takenOfflineByStaffDate = respondent1TimeExtensionDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension1v2() {
        atStateNotificationAcknowledged1v2SameSolicitor();
        atStateClaimDetailsNotifiedTimeExtension1v2();
        multiPartyClaimTwoDefendantSolicitors();
        atStateNotificationAcknowledgedTimeExtension_1v2DS();
        takenOfflineByStaff();
        takenOfflineByStaffDate = respondent1TimeExtensionDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder takenOfflineByStaff() {
        claimProceedsInCaseman = ClaimProceedsInCaseman.builder()
            .date(LocalDate.now())
            .reason(ReasonForProceedingOnPaper.APPLICATION)
            .build();
        takenOfflineByStaffDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder takenOfflineByStaffSpec() {
        claimProceedsInCasemanLR = ClaimProceedsInCasemanLR.builder()
            .date(LocalDate.now())
            .reason(ReasonForProceedingOnPaper.APPLICATION)
            .build();
        takenOfflineByStaffDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateSpec1v1ClaimSubmitted() {
        submittedDate = LocalDateTime.now().plusDays(1);
        addRespondent2 = NO;
        atStatePaymentSuccessful();
        atStatePendingClaimIssued();
        atStateClaimIssued();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateSpec2v1ClaimSubmitted() {
        submittedDate = LocalDateTime.now().plusDays(1);
        addRespondent2 = NO;
        atStatePaymentSuccessful();
        atStatePendingClaimIssued();
        atStateClaimIssued();
        multiPartyClaimTwoApplicants();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateSpec1v2ClaimSubmitted() {
        submittedDate = LocalDateTime.now().plusDays(1);
        atStatePaymentSuccessful();
        atStatePendingClaimIssued();
        atStateClaimIssued();
        multiPartyClaimOneDefendantSolicitor();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondent1v1FullAdmissionSpec() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent1v1FullDefenceSpec() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1FullAdmission() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1FullDefence() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1PartAdmission() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1CounterClaim() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.COUNTER_CLAIM;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.COUNTER_CLAIM;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1SecondFullDefence_FirstPartAdmission() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1FirstFullDefence_SecondPartAdmission() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1BothNotFullDefence_PartAdmissionX2() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateBothClaimantv1BothNotFullDefence_PartAdmissionX2() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1BothNotFullDefence_CounterClaimX2() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.COUNTER_CLAIM;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.COUNTER_CLAIM;
        return this;
    }

    public CaseDataBuilder atStateRespondent1v2FullAdmission() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent1v2AdmitAll_AdmitPart() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent1v2FullDefence_AdmitPart() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent1v2FullDefence_AdmitFull() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent1v2AdmintPart_FullDefence() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceWithHearingSupport() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondent1DQ = Respondent1DQ.builder()
            .respondent1DQRequestedCourt(
                RequestedCourt.builder()
                    .responseCourtCode("121")
                    .reasonForHearingAtSpecificCourt("test")
                    .caseLocation(CaseLocationCivil.builder()
                                      .region("2")
                                      .baseLocation("000000")
                                      .build()).build())
            .respondent1DQHearingSupport(HearingSupport.builder()
                                             .requirements(List.of(SupportRequirements.values()))
                                             .languageToBeInterpreted("English")
                                             .signLanguageRequired("Spanish")
                                             .otherSupport("other support")
                                             .supportRequirements(YES)
                                             .supportRequirementsAdditional("additional support").build()).build();
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant1-defence.pdf").build())
            .build();
        respondent1ResponseDate = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefence() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant1-defence.pdf").build())
            .build();
        respondent1DQWithLocation();
        respondent1ResponseDate = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateClaimantFullDefence() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondentSharedClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant1-defence.pdf").build())
            .build();
        applicant1DQWithLocation();
        applicant1ResponseDate = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceRespondent2() {
        atStateRespondent2RespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent2DQWithLocation();
        respondent2ResponseDate = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondent2RespondToClaim(RespondentResponseType respondentResponseType) {
        atStateNotificationAcknowledged();
        respondent2ClaimResponseType = respondentResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses() {
        atStateRespondentFullDefence();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        respondent2ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant2-defence.pdf").build())
            .build();
        respondent2DQ();
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim() {
        atStateRespondentFullDefence();
        respondent2ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent2DQ();
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefence_1v2_Resp1CounterClaimAndResp2FullDefence() {
        atStateRespondentFullDefence();
        defendantSingleResponseToBothClaimants = NO;
        respondent1ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        respondent1ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent2DQ();
        return this;
    }

    public CaseDataBuilder atStateDivergentResponseWithFullDefence1v2SameSol_NotSingleDQ() {
        atStateRespondentFullDefence();
        respondent2ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent2DQ();
        respondentResponseIsSame(NO);

        return this;
    }

    public CaseDataBuilder atStateDivergentResponseWithRespondent2FullDefence1v2SameSol_NotSingleDQ() {
        atStateRespondentFullAdmission();
        respondent2ClaimResponseType = RespondentResponseType.FULL_ADMISSION;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent2DQ();
        respondentResponseIsSame(NO);

        return this;
    }

    public CaseDataBuilder atStateDivergentResponse_1v2_Resp1FullAdmissionAndResp2CounterClaim() {
        atStateRespondentFullAdmission();
        respondent2ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilder atStateFullAdmission_1v2_BothRespondentSolicitorsSubmitFullAdmissionResponse() {
        atStateRespondentFullAdmission();
        respondent2ClaimResponseType = RespondentResponseType.FULL_ADMISSION;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndNotProceed_1v2_DiffSol() {
        atStateApplicantRespondToDefenceAndNotProceed_1v2();
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceSpec_1v2_BothPartiesFullDefenceResponses() {
        atStateRespondentFullDefenceSpec();
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant2-defence.pdf").build())
            .build();
        respondent2DQ();
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceSpec() {
        atStateRespondentRespondToClaimSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant1-defence.pdf").build())
            .build();
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentRespondToClaimSpec(RespondentResponseTypeSpec respondentResponseTypeSpec) {
        atStateNotificationAcknowledged();
        respondent1ClaimResponseTypeForSpec = respondentResponseTypeSpec;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceAfterNotificationAcknowledgement() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(3);
        return this;
    }

    public CaseDataBuilder atStateTwoRespondentsFullDefenceAfterNotificationAcknowledgement() {
        atStateRespondentRespondToClaim1v2(RespondentResponseType.FULL_DEFENCE, RespondentResponseType.FULL_DEFENCE);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();
        respondent2DQ();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        respondent2ResponseDate = respondent2AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceFastTrack() {
        atStateRespondentRespondToClaimFastTrack(RespondentResponseType.FULL_DEFENCE);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentAdmitPartOfClaimFastTrack() {
        atStateRespondentRespondToClaimFastTrack(RespondentResponseType.PART_ADMISSION);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now();
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceAfterAcknowledgementTimeExtension() {
        atStateNotificationAcknowledgedRespondent1TimeExtension();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        //respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = LocalDateTime.now();
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceAfterNotifyClaimDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent1ResponseDate = LocalDateTime.now();
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();
        respondent2ResponseDeadline = RESPONSE_DEADLINE.plusDays(2);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse() {
        atStateClaimDetailsNotified();
        respondent2ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent2DQ();
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceAfterNotifyClaimDetailsTimeExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = LocalDateTime.now();
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();
        respondent1ResponseDate = respondent1TimeExtensionDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTwoRespondentsFullDefenceAfterNotifyClaimDetailsTimeExtension() {
        atStateClaimDetailsNotifiedTimeExtension1v2();
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent1ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent2ResponseDate = LocalDateTime.now();
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();
        respondent1ResponseDate = respondent1TimeExtensionDate.plusDays(1);

        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent2DQ();
        respondent2ResponseDate = respondent1TimeExtensionDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullAdmission() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_ADMISSION);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentFullAdmissionSpec() {
        atStateRespondentRespondToClaimSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateBothRespondentsSameResponse(RespondentResponseType respondentResponseType) {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = respondentResponseType;
        respondent1ResponseDate = LocalDateTime.now().plusDays(3);
        respondent2Responds(respondentResponseType);
        respondent2ResponseDate = LocalDateTime.now().plusDays(4);
        return this;
    }

    public CaseDataBuilder atStateBothRespondentsSameResponse1v2SameSolicitor(RespondentResponseType responseType) {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = responseType;
        respondent2ClaimResponseType = responseType;
        respondentResponseIsSame(YES);
        respondent1ResponseDate = LocalDateTime.now().plusDays(3);
        respondent2ResponseDate = respondent1ResponseDate;
        respondent2ClaimResponseIntentionType = respondent1ClaimResponseIntentionType;
        return this;
    }

    public CaseDataBuilder atState1v2SameSolicitorDivergentResponse(RespondentResponseType respondent1Response,
                                                       RespondentResponseType respondent2Response) {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = respondent1Response;
        respondent2Responds(respondent2Response);
        respondent1ResponseDate = LocalDateTime.now().plusDays(3);
        respondentResponseIsSame(NO);
        if (caseAccessCategory != SPEC_CLAIM) {
            // at least in spec claims, respondent2 response date is null by front-end
            respondent2ResponseDate = respondent1ResponseDate;
        } else {
            respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec
                .valueOf(respondent1Response.name());
            respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec
                .valueOf(respondent2ClaimResponseType.name());
        }
        return this;
    }

    public CaseDataBuilder atState1v2DivergentResponse(RespondentResponseType respondent1Response,
                                                       RespondentResponseType respondent2Response) {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = respondent1Response;
        respondent1ResponseDate = LocalDateTime.now().plusDays(3);
        respondent2Responds(respondent2Response);
        respondent2ResponseDate = LocalDateTime.now().plusDays(4);
        return this;
    }

    public CaseDataBuilder atState1v2DivergentResponseSpec(RespondentResponseTypeSpec respondent1Response,
                                                           RespondentResponseTypeSpec respondent2Response) {
        respondent1ClaimResponseTypeForSpec = respondent1Response;
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        respondent2RespondsSpec(respondent2Response);
        respondent2ResponseDate = LocalDateTime.now().plusDays(2);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullAdmissionAfterNotificationAcknowledged() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_ADMISSION);
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder addEnterBreathingSpace() {
        this.enter = BreathingSpaceEnterInfo.builder()
                    .type(BreathingSpaceType.STANDARD)
                    .reference("12345")
                    .start(LocalDate.now())
                    .build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).build();

        return this;
    }

    public CaseDataBuilder addEnterMentalHealthBreathingSpace() {
        this.enter = BreathingSpaceEnterInfo.builder()
                    .type(BreathingSpaceType.MENTAL_HEALTH)
                    .reference("12345")
                    .start(LocalDate.now())
                    .build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).build();

        return this;
    }

    public CaseDataBuilder addEnterMentalHealthBreathingSpaceNoOptionalData() {
        this.enter = BreathingSpaceEnterInfo.builder()
            .type(BreathingSpaceType.MENTAL_HEALTH)
            .reference(null)
            .start(null)
            .build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).build();

        return this;
    }

    public CaseDataBuilder addLiftBreathingSpace() {
        this.enter = BreathingSpaceEnterInfo.builder()
            .type(BreathingSpaceType.STANDARD)
            .reference("12345")
            .start(LocalDate.now())
            .build();
        this.lift = BreathingSpaceLiftInfo.builder().expectedEnd(LocalDate.now()).build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).lift(this.lift).build();

        return this;
    }

    public CaseDataBuilder addLiftBreathingSpaceWithoutOptionalData() {
        this.enter = BreathingSpaceEnterInfo.builder()
            .type(BreathingSpaceType.STANDARD)
            .reference(null)
            .start(null)
            .build();
        this.lift = BreathingSpaceLiftInfo.builder().expectedEnd(null).build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).lift(this.lift).build();

        return this;
    }

    public CaseDataBuilder addLiftMentalBreathingSpace() {
        this.enter = BreathingSpaceEnterInfo.builder()
            .type(BreathingSpaceType.MENTAL_HEALTH)
            .reference("12345")
            .start(LocalDate.now())
            .build();
        this.lift = BreathingSpaceLiftInfo.builder().expectedEnd(LocalDate.now()).build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).lift(this.lift).build();

        return this;
    }

    public CaseDataBuilder addLiftMentalBreathingSpaceNoOptionalData() {
        this.enter = BreathingSpaceEnterInfo.builder()
            .type(BreathingSpaceType.MENTAL_HEALTH)
            .reference(null)
            .start(null)
            .build();
        this.lift = BreathingSpaceLiftInfo.builder().expectedEnd(null).build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).lift(this.lift).build();

        return this;
    }

    public CaseDataBuilder addEnterBreathingSpaceWithoutOptionalData() {
        this.enter = BreathingSpaceEnterInfo.builder()
            .type(BreathingSpaceType.STANDARD)
            .reference(null)
            .start(null)
            .build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).build();

        return this;
    }

    public CaseDataBuilder atStateRespondentFullAdmissionAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.FULL_ADMISSION;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder addEnterBreathingSpaceWithOnlyReferenceInfo() {
        this.enter = BreathingSpaceEnterInfo.builder()
            .type(BreathingSpaceType.STANDARD)
            .reference("12345")
            .start(null)
            .build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).build();

        return this;
    }

    public CaseDataBuilder atStateRespondentPartAdmission() {
        atStateRespondentRespondToClaim(RespondentResponseType.PART_ADMISSION);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentPartAdmissionSpec() {
        atStateRespondentRespondToClaimSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentFullAdmissionAfterAcknowledgementTimeExtension() {
        atStateNotificationAcknowledgedRespondent1TimeExtension();
        respondent1ClaimResponseType = RespondentResponseType.FULL_ADMISSION;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentPartAdmissionAfterNotificationAcknowledgement() {
        atStateRespondentRespondToClaim(RespondentResponseType.PART_ADMISSION);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentPartAdmissionAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.PART_ADMISSION;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentPartAdmissionAfterAcknowledgementTimeExtension() {
        atStateNotificationAcknowledgedRespondent1TimeExtension();
        respondent1ClaimResponseType = RespondentResponseType.PART_ADMISSION;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilder atStateRespondentCounterClaim() {
        atStateRespondentRespondToClaim(RespondentResponseType.COUNTER_CLAIM);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentCounterClaimSpec() {
        atStateRespondentRespondToClaimSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondent1CounterClaimAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondent2CounterClaimAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent2 = Party.builder().partyName("Respondent 2").build();
        respondent2ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent2ResponseDate = claimDetailsNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentCounterClaimAfterAcknowledgementTimeExtension() {
        atStateNotificationAcknowledgedRespondent1TimeExtension();
        respondent1ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilder atStateRespondentRespondToClaim(RespondentResponseType respondentResponseType) {
        atStateNotificationAcknowledged();
        respondent1ClaimResponseType = respondentResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataBuilder atStateRespondentRespondToClaim1v2(RespondentResponseType respondent1ResponseType,
                                                              RespondentResponseType respondent2ResponseType) {
        atStateNotificationAcknowledged_1v2_BothDefendants();
        respondent1ClaimResponseType = respondent1ResponseType;
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        respondent2ClaimResponseType = respondent2ResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent2ResponseDate = respondent2AcknowledgeNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineAdmissionOrCounterClaim() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        return this;
    }

    public CaseDataBuilder atStateRespondentRespondToClaimFastTrack(RespondentResponseType respondentResponseType) {
        atStateNotificationAcknowledged();
        respondToClaim = RespondToClaim.builder().howMuchWasPaid(FAST_TRACK_CLAIM_AMOUNT).build();
        totalClaimAmount = FAST_TRACK_CLAIM_AMOUNT;
        respondent1ClaimResponseType = respondentResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = LocalDateTime.now();
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataBuilder atStatePastClaimDismissedDeadline() {
        atStateClaimDetailsNotified();
        claimDismissedDeadline = LocalDateTime.now().minusDays(5);
        return this;
    }

    public CaseDataBuilder atStateHearingDateScheduled() {
        atStateHearingFeeDuePaid();
        hearingDate = LocalDate.now().plusWeeks(3).plusDays(1);
        hearingFeePaymentDetails = PaymentDetails.builder().status(SUCCESS).build();
        ccdState = HEARING_READINESS;
        return this;
    }

    public CaseDataBuilder atStatePastClaimDismissedDeadline_1v2() {
        atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors();
        claimDismissedDeadline = LocalDateTime.now().minusDays(5);
        return this;
    }

    public CaseDataBuilder atStateClaimDismissed() {
        atStatePastClaimDismissedDeadline();
        ccdState = CASE_DISMISSED;
        claimDismissedDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atState2v1Applicant1NotProceedApplicant2Proceeds() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ProceedWithClaimMultiParty2v1 = NO;
        applicant2ProceedWithClaimMultiParty2v1 = YES;
        applicant2DefenceResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("claimant-response.pdf").build())
            .build();
        applicant2DQ();
        applicant2ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = StatementOfTruth.builder().name("John Smith").role("Solicitor").build();
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndProceed() {
        return atStateApplicantRespondToDefenceAndProceed(ONE_V_ONE);
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario mpScenario) {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1DefenceResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("claimant-response.pdf").build())
            .build();
        applicant1DQ();
        applicant1ResponseDate = respondent1ResponseDate.plusDays(2);
        uiStatementOfTruth = StatementOfTruth.builder().name("John Smith").role("Solicitor").build();

        switch (mpScenario) {
            case ONE_V_TWO_ONE_LEGAL_REP: {
                respondent2SameLegalRepresentative = YES;
                return atStateRespondentFullDefenceRespondent2();
            }
            case ONE_V_TWO_TWO_LEGAL_REP: {
                respondent2SameLegalRepresentative = NO;
                return atStateRespondentFullDefenceRespondent2();
            }
            case ONE_V_ONE: {
                applicant1ProceedWithClaim = YES;
                return this;
            }
            default: {
                return this;
            }
        }
    }

    public CaseDataBuilder atStateTrialReadyCheck(MultiPartyScenario mpScenario) {
        atStateApplicantRespondToDefenceAndProceed(mpScenario);
        hearingDate = LocalDate.now().plusWeeks(5).plusDays(6);
        hearingDuration = MINUTES_120;
        ccdState = PREPARE_FOR_HEARING_CONDUCT_HEARING;

        if (mpScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            solicitorReferences = SolicitorReferences.builder()
                .applicantSolicitor1Reference("123456")
                .respondentSolicitor1Reference("123456")
                .respondentSolicitor2Reference("123456").build();
            return this;
        }

        return this;
    }

    public CaseDataBuilder atStateTrialReadyCheck() {
        atStateHearingFeeDuePaid();
        ccdState = PREPARE_FOR_HEARING_CONDUCT_HEARING;
        hearingDate = LocalDate.now().plusWeeks(5).plusDays(5);
        hearingDuration = MINUTES_120;
        return this;
    }

    public CaseDataBuilder atStateTrialReadyCheckLiP(boolean hasEmailAddress) {
        atStateHearingFeeDuePaid().setClaimTypeToSpecClaim();
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        if (!hasEmailAddress) {
            respondent1 = respondent1.toBuilder().partyEmail("").build();
            respondent2 = respondent2.toBuilder().partyEmail("").build();
        }
        legacyCaseReference = "000MC001";
        ccdState = PREPARE_FOR_HEARING_CONDUCT_HEARING;
        hearingDate = LocalDate.now().plusWeeks(5).plusDays(5);
        hearingDuration = MINUTES_120;
        applicant1Represented = NO;
        respondent1Represented = NO;
        respondent2Represented = NO;
        return this;
    }

    public CaseDataBuilder atStateTrialReadyApplicant() {
        atStateTrialReadyCheck();
        trialReadyApplicant = YES;
        applicantRevisedHearingRequirements = RevisedHearingRequirements.builder()
                                                                        .revisedHearingRequirements(YES)
                                                                        .revisedHearingComments("Changes requested.")
                                                                        .build();

        return this;
    }

    public CaseDataBuilder atStateTrialNotReadyApplicant() {
        atStateTrialReadyCheck();
        trialReadyApplicant = NO;
        applicantRevisedHearingRequirements = RevisedHearingRequirements.builder()
            .revisedHearingRequirements(YES)
            .revisedHearingComments("Changes requested.")
            .build();

        return this;
    }

    public CaseDataBuilder atStateTrialReadyRespondent1() {
        atStateTrialReadyCheck();
        trialReadyRespondent1 = YES;
        respondent1RevisedHearingRequirements = RevisedHearingRequirements.builder()
                                                                            .revisedHearingRequirements(YES)
                                                                            .revisedHearingComments("Changes requested.")
                                                                            .build();
        return this;
    }

    public CaseDataBuilder atStateTrialNotReadyRespondent1() {
        atStateTrialReadyCheck();
        trialReadyRespondent1 = NO;
        respondent1RevisedHearingRequirements = RevisedHearingRequirements.builder()
            .revisedHearingRequirements(YES)
            .revisedHearingComments("Changes requested.")
            .build();
        return this;
    }

    public CaseDataBuilder atStateTrialReadyRespondent2() {
        atStateTrialReadyCheck();
        trialReadyRespondent2 = YES;
        applicantRevisedHearingRequirements = RevisedHearingRequirements.builder()
                                                                        .revisedHearingRequirements(YES)
                                                                        .revisedHearingComments("Changes requested.")
                                                                        .build();
        return this;
    }

    public CaseDataBuilder atStateTrialNotReadyRespondent2() {
        atStateTrialReadyCheck();
        trialReadyRespondent2 = NO;
        applicantRevisedHearingRequirements = RevisedHearingRequirements.builder()
            .revisedHearingRequirements(YES)
            .revisedHearingComments("Changes requested.")
            .build();
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndNotProceed() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ProceedWithClaim = NO;
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = StatementOfTruth.builder().name("John Smith").role("Solicitor").build();
        return this;
    }

    public CaseDataBuilder atState1v2SameSolicitorDivergentResponseSpec(RespondentResponseTypeSpec respondent1Response,
                                                                       RespondentResponseTypeSpec respondent2Response) {
        atStateNotificationAcknowledged();
        respondent1ClaimResponseTypeForSpec = respondent1Response;
        respondent2RespondsSpec(respondent2Response);
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        respondentResponseIsSame(NO);
        respondent2ResponseDate = respondent1ResponseDate;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atState1v2DifferentSolicitorDivergentResponseSpec(
        RespondentResponseTypeSpec respondent1Response,
        RespondentResponseTypeSpec respondent2Response) {
        atStateNotificationAcknowledged();
        respondent1ClaimResponseTypeForSpec = respondent1Response;
        respondent2RespondsSpec(respondent2Response);
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        respondentResponseIsSame(NO);
        respondent2ResponseDate = respondent1ResponseDate;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder respondent2RespondsSpec(RespondentResponseTypeSpec responseType) {
        this.respondent2ClaimResponseTypeForSpec = responseType;
        this.respondent2ResponseDate = LocalDateTime.now().plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = YES;
        applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = YES;
        applicant1DefenceResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("claimant-response.pdf").build())
            .build();
        applicant1DQ();
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        respondent2ResponseDate = respondent1ResponseDate.plusDays(1);
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        uiStatementOfTruth = StatementOfTruth.builder().name("John Smith").role("Solicitor").build();
        return this;
    }

    public CaseDataBuilder setMultiTrackClaim() {
        allocatedTrack = MULTI_CLAIM;
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndProceedVsDefendant1Only_1v2() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = YES;
        applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = NO;
        applicant1DefenceResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("claimant-response.pdf").build())
            .build();
        applicant1DQ();
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        uiStatementOfTruth = StatementOfTruth.builder().name("John Smith").role("Solicitor").build();
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndProceedVsDefendant2Only_1v2() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = NO;
        applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = YES;
        applicant1DefenceResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("claimant-response.pdf").build())
            .build();
        applicant1DQ();
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        uiStatementOfTruth = StatementOfTruth.builder().name("John Smith").role("Solicitor").build();
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndNotProceed_1v2() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = NO;
        applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = NO;
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = StatementOfTruth.builder().name("John Smith").role("Solicitor").build();
        return this;
    }

    public CaseDataBuilder atStateBothApplicantsRespondToDefenceAndProceed_2v1() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent1ClaimResponseTypeToApplicant2 = RespondentResponseType.FULL_DEFENCE;
        applicant1ProceedWithClaimMultiParty2v1 = YES;
        applicant2ProceedWithClaimMultiParty2v1 = YES;

        applicant1DefenceResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("claimant-response-1.pdf").build())
            .build();
        applicant1DQ();
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        applicant2DefenceResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("claimant-response-1.pdf").build())
            .build();
        applicant2DQ();
        applicant2ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = StatementOfTruth.builder().name("John Smith").role("Solicitor").build();
        return this;
    }

    public CaseDataBuilder atStateBothApplicantsRespondToDefenceAndProceed_2v1_SPEC() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        defendantSingleResponseToBothClaimants = YES;
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        applicant1ProceedWithClaimMultiParty2v1 = YES;
        applicant2ProceedWithClaimMultiParty2v1 = YES;

        applicant1DefenceResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("claimant-response-1.pdf").build())
            .build();
        applicant1DQ();
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        applicant2DefenceResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("claimant-response-1.pdf").build())
            .build();
        applicant2DQ();
        applicant2ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = StatementOfTruth.builder().name("John Smith").role("Solicitor").build();
        return this;
    }

    public CaseDataBuilder atStateApplicant1RespondToDefenceAndProceed_2v1() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent1ClaimResponseTypeToApplicant2 = RespondentResponseType.FULL_DEFENCE;
        applicant1ProceedWithClaimMultiParty2v1 = YES;
        applicant2ProceedWithClaimMultiParty2v1 = NO;
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        applicant1DefenceResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("claimant-response-1.pdf").build())
            .build();
        applicant1DQ();
        applicant2ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = StatementOfTruth.builder().name("John Smith").role("Solicitor").build();
        return this;
    }

    public CaseDataBuilder atStateApplicant2RespondToDefenceAndProceed_2v1() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent1ClaimResponseTypeToApplicant2 = RespondentResponseType.FULL_DEFENCE;
        applicant1ProceedWithClaimMultiParty2v1 = NO;
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        applicant2ProceedWithClaimMultiParty2v1 = YES;
        applicant2DefenceResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("claimant-response-1.pdf").build())
            .build();
        applicant2DQ();
        applicant2ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = StatementOfTruth.builder().name("John Smith").role("Solicitor").build();
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndNotProceed_2v1() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent1ClaimResponseTypeToApplicant2 = RespondentResponseType.FULL_DEFENCE;
        applicant1ProceedWithClaimMultiParty2v1 = NO;
        applicant2ProceedWithClaimMultiParty2v1 = NO;
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        applicant2ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = StatementOfTruth.builder().name("John Smith").role("Solicitor").build();
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledged1v2SameSolicitor() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseIntentionType = FULL_DEFENCE;
        respondent1AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        respondent2AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledged() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseIntentionType = FULL_DEFENCE;
        respondent1AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateDisposalHearingOrderMadeWithoutHearing() {
        disposalHearingOrderMadeWithoutHearingDJ =
            DisposalHearingOrderMadeWithoutHearingDJ.builder().input("test").build();
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledged_1v2_BothDefendants() {
        atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors();
        respondent1ClaimResponseIntentionType = FULL_DEFENCE;
        respondent1AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        respondent2AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedRespondent2() {
        atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors();
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        respondent2AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedRespondent2Only() {
        atStateNotificationAcknowledgedRespondent2();
        respondent1AcknowledgeNotificationDate = null;
        return this;
    }

    public CaseDataBuilder atDeadlinePassedAfterStateNotificationAcknowledged() {
        atStateNotificationAcknowledged();
        this.claimDismissedDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        this.claimDismissedDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atDeadlinePassedAfterStateNotificationAcknowledgedTimeExtension() {
        atStateNotificationAcknowledgedRespondent1TimeExtension();
        this.claimDismissedDate = respondent1TimeExtensionDate.plusDays(1);
        this.claimDismissedDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atDeadlinePassedAfterStateClaimDetailsNotifiedExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        this.claimDismissedDate = respondent1TimeExtensionDate.plusDays(1);
        this.claimDismissedDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atDeadlinePassedAfterStateClaimDetailsNotified() {
        atStateClaimDismissedPastClaimDetailsNotificationDeadline();
        this.claimDismissedDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedRespondent1TimeExtension(int numberOfHoursAfterCurrentDate) {
        atStateNotificationAcknowledged();
        respondent1TimeExtensionDate = respondent1AcknowledgeNotificationDate.plusHours(numberOfHoursAfterCurrentDate);
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedRespondent1TimeExtension() {
        return atStateNotificationAcknowledgedRespondent1TimeExtension(1);
    }

    public CaseDataBuilder atStateNotificationAcknowledgedTimeExtension_1v2DS() {
        atStateNotificationAcknowledged_1v2_BothDefendants();
        respondent1TimeExtensionDate = respondent1AcknowledgeNotificationDate.plusHours(1);
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondent2TimeExtensionDate = respondent2AcknowledgeNotificationDate.plusHours(1);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedTimeExtensionRespondent1_1v2DS() {
        atStateNotificationAcknowledged_1v2_BothDefendants();
        respondent1TimeExtensionDate = respondent1AcknowledgeNotificationDate.plusHours(1);
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedTimeExtensionRespondent2_1v2DS() {
        atStateNotificationAcknowledged_1v2_BothDefendants();
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondent2TimeExtensionDate = respondent2AcknowledgeNotificationDate.plusHours(1);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedRespondent2TimeExtension(int numberOfHoursAfterCurrentDate) {
        atStateNotificationAcknowledged();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        respondent2TimeExtensionDate = respondent1AcknowledgeNotificationDate.plusHours(numberOfHoursAfterCurrentDate);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedRespondent2TimeExtension() {
        return atStateNotificationAcknowledgedRespondent2TimeExtension(5);
    }

    public CaseDataBuilder atStatePastApplicantResponseDeadline() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ResponseDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflinePastApplicantResponseDeadline() {
        atStatePastApplicantResponseDeadline();
        takenOfflineDate = respondent1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateHearingFeeDueUnpaid() {
        atStateApplicantRespondToDefenceAndProceed();
        hearingDueDate = LocalDate.now().minusDays(1);
        hearingFeePaymentDetails = PaymentDetails.builder().status(FAILED).build();
        ccdState = HEARING_READINESS;
        return this;
    }

    public CaseDataBuilder atStateHearingFeeDuePaid() {
        atStateApplicantRespondToDefenceAndProceed();
        hearingDueDate = now().minusDays(1);
        hearingFeePaymentDetails = PaymentDetails.builder().status(SUCCESS).build();
        ccdState = HEARING_READINESS;
        return this;
    }

    public CaseDataBuilder atStateBeforeTakenOfflineSDONotDrawn() {

        atStateApplicantRespondToDefenceAndProceed();

        ccdState = JUDICIAL_REFERRAL;
        reasonNotSuitableSDO = ReasonNotSuitableSDO.builder()
            .input("unforeseen complexities")
            .build();
        unsuitableSDODate = applicant1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateBeforeTakenOfflineSDONotDrawnOverLimit() {

        atStateApplicantRespondToDefenceAndProceed();

        ccdState = JUDICIAL_REFERRAL;
        reasonNotSuitableSDO = ReasonNotSuitableSDO.builder()
            .input("This is more than 150 111111111111111111111111111111111111111111111111111111111111111111111111111"
                       + "111111111111111111111111111111111111111111111111111111")
            .build();
        unsuitableSDODate = applicant1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateBeforeTransferCaseSDONotDrawn() {

        atStateApplicantRespondToDefenceAndProceed();

        ccdState = JUDICIAL_REFERRAL;
        notSuitableSdoOptions = NotSuitableSdoOptions.CHANGE_LOCATION;

        tocTransferCaseReason = TocTransferCaseReason.builder()
            .reasonForCaseTransferJudgeTxt("unforeseen complexities")
            .build();
        unsuitableSDODate = applicant1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateBeforeTransferCaseSDONotDrawnOverLimit() {

        atStateApplicantRespondToDefenceAndProceed();

        ccdState = JUDICIAL_REFERRAL;
        notSuitableSdoOptions = NotSuitableSdoOptions.CHANGE_LOCATION;

        tocTransferCaseReason = TocTransferCaseReason.builder()
            .reasonForCaseTransferJudgeTxt("This is more than 150 111111111111111111111111111111111111111111111111111111111111111111111111111"
                       + "111111111111111111111111111111111111111111111111111111")
            .build();
        unsuitableSDODate = applicant1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineSDONotDrawn(MultiPartyScenario mpScenario) {

        atStateApplicantRespondToDefenceAndProceed(mpScenario);
        if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2();
        } else if (mpScenario == TWO_V_ONE) {
            atStateBothApplicantsRespondToDefenceAndProceed_2v1();
        }

        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = applicant1ResponseDate.plusDays(1);

        reasonNotSuitableSDO = ReasonNotSuitableSDO.builder()
            .input("unforeseen complexities")
            .build();
        unsuitableSDODate = applicant1ResponseDate.plusDays(1);

        return this;
    }

    public CaseDataBuilder atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotified(MultiPartyScenario mpScenario, boolean isReason) {
        if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP || mpScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            respondent2 = PartyBuilder.builder().soleTrader().build().toBuilder().partyID("res-2-party-id").build();
            atStateClaimDetailsNotified1v1().respondent2Copy(respondent2).build();
            respondent2SameLegalRepresentative = mpScenario == ONE_V_TWO_ONE_LEGAL_REP ? YES : NO;
        } else {
            atStateClaimDetailsNotified1v1();
        }

        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();

        reasonNotSuitableSDO = ReasonNotSuitableSDO.builder()
            .input(isReason ? "unforeseen complexities" : "")
            .build();
        unsuitableSDODate = LocalDateTime.now();

        return this;
    }

    public CaseDataBuilder atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension(boolean isReason) {
        atStateClaimDetailsNotified1v1();
        respondent1TimeExtensionDate = LocalDateTime.now();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();

        reasonNotSuitableSDO = ReasonNotSuitableSDO.builder()
            .input(isReason ? "unforeseen complexities" : "")
            .build();
        unsuitableSDODate = LocalDateTime.now();

        return this;
    }

    public CaseDataBuilder atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledged(MultiPartyScenario mpScenario, boolean isReason) {
        if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP || mpScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            respondent2 = PartyBuilder.builder().soleTrader().build().toBuilder().partyID("res-2-party-id").build();
            atStateNotificationAcknowledged_1v2_BothDefendants().respondent2Copy(respondent2).build();
            respondent2SameLegalRepresentative = mpScenario == ONE_V_TWO_ONE_LEGAL_REP ? YES : NO;
        } else {
            atStateNotificationAcknowledged();
        }

        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();

        reasonNotSuitableSDO = ReasonNotSuitableSDO.builder()
            .input(isReason ? "unforeseen complexities" : "")
            .build();
        unsuitableSDODate = LocalDateTime.now();

        return this;
    }

    public CaseDataBuilder atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension(MultiPartyScenario mpScenario, boolean isReason) {
        if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP || mpScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            respondent2 = PartyBuilder.builder().soleTrader().build().toBuilder().partyID("res-2-party-id").build();
            atStateNotificationAcknowledged_1v2_BothDefendants().respondent2Copy(respondent2).build();
            respondent2SameLegalRepresentative = mpScenario == ONE_V_TWO_ONE_LEGAL_REP ? YES : NO;
            respondent1TimeExtensionDate = LocalDateTime.now();
            respondent2TimeExtensionDate = LocalDateTime.now();
        } else {
            atDeadlinePassedAfterStateNotificationAcknowledgedTimeExtension();
        }

        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();

        reasonNotSuitableSDO = ReasonNotSuitableSDO.builder()
            .input(isReason ? "unforeseen complexities" : "")
            .build();
        unsuitableSDODate = LocalDateTime.now();

        return this;
    }

    public CaseDataBuilder atStateTakenOfflineAfterSDO(MultiPartyScenario mpScenario) {

        atStateApplicantRespondToDefenceAndProceed(mpScenario);
        if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2();
        } else if (mpScenario == TWO_V_ONE) {
            atStateBothApplicantsRespondToDefenceAndProceed_2v1();
        }

        drawDirectionsOrderRequired = NO;
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = applicant1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterSDO(MultiPartyScenario mpScenario) {
        atStateApplicantRespondToDefenceAndProceed(mpScenario);
        if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2();
        } else if (mpScenario == TWO_V_ONE) {
            atStateBothApplicantsRespondToDefenceAndProceed_2v1();
        }

        drawDirectionsOrderRequired = NO;
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = applicant1ResponseDate.plusDays(1);
        takenOfflineByStaffDate = applicant1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateApplicantProceedAllMediation(MultiPartyScenario mpScenario) {

        applicant1ClaimMediationSpecRequired = new SmallClaimMedicalLRspec(YES);
        applicantMPClaimMediationSpecRequired = new SmallClaimMedicalLRspec(YES);
        respondent1MediationRequired = YES;
        respondent2MediationRequired = YES;
        responseClaimTrack = SMALL_CLAIM.name();
        caseAccessCategory = SPEC_CLAIM;

        atStateApplicantRespondToDefenceAndProceed(mpScenario);

        if (mpScenario == ONE_V_ONE) {
            atStateRespondentFullDefenceSpec();
        } else if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2();
            atStateRespondentFullDefenceSpec();
        } else if (mpScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2();
            atState1v2DifferentSolicitorDivergentResponseSpec(
                RespondentResponseTypeSpec.FULL_DEFENCE,
                RespondentResponseTypeSpec.FULL_DEFENCE
            );
        } else if (mpScenario == TWO_V_ONE) {
            applicant1ProceedWithClaimSpec2v1 = YES;
            atStateBothApplicantsRespondToDefenceAndProceed_2v1_SPEC();
        }

        return this;
    }

    public CaseDataBuilder atStateMediationUnsuccessful(MultiPartyScenario mpScenario) {
        atStateApplicantProceedAllMediation(mpScenario);
        applicantsProceedIntention = YES;
        caseDataLiP = CaseDataLiP.builder()
                                      .applicant1ClaimMediationSpecRequiredLip(
                                          ClaimantMediationLip.builder()
                                              .hasAgreedFreeMediation(MediationDecision.Yes)
                                              .build()).build();

        mediation = Mediation.builder().unsuccessfulMediationReason("Unsuccessful").build();

        return this;
    }

    public CaseDataBuilder businessProcess(BusinessProcess businessProcess) {
        this.businessProcess = businessProcess;
        return this;
    }

    public CaseDataBuilder applicant2ResponseDate(LocalDateTime applicant2ResponseDate) {
        this.applicant2ResponseDate = applicant2ResponseDate;
        return this;
    }

    public CaseDataBuilder caseBundles(List<IdValue<Bundle>> caseBundles) {
        this.caseBundles = caseBundles;
        return this;
    }

    public CaseDataBuilder applicantSolicitor1UserDetails(IdamUserDetails applicantSolicitor1UserDetails) {
        this.applicantSolicitor1UserDetails = applicantSolicitor1UserDetails;
        return this;
    }

    public CaseDataBuilder addApplicant1LitigationFriend() {
        this.applicant1LitigationFriend = LitigationFriend.builder()
            .partyID("app-1-litfriend-party-id")
            .fullName("Mr Applicant Litigation Friend")
            .firstName("Applicant")
            .lastName("Litigation Friend")
            .primaryAddress(AddressBuilder.defaults().build())
            .hasSameAddressAsLitigant(YES)
            .certificateOfSuitability(List.of())
            .build();
        this.applicant1LitigationFriendRequired = YES;
        return this;
    }

    public CaseDataBuilder addApplicant2LitigationFriend() {
        this.applicant2LitigationFriend = LitigationFriend.builder()
            .partyID("app-2-litfriend-party-id")
            .fullName("Mr Applicant Litigation Friend")
            .firstName("Applicant Two")
            .lastName("Litigation Friend")
            .primaryAddress(AddressBuilder.defaults().build())
            .hasSameAddressAsLitigant(YES)
            .certificateOfSuitability(List.of())
            .build();
        this.applicant2LitigationFriendRequired = YES;
        return this;
    }

    public CaseDataBuilder addRespondent1LitigationFriend() {
        this.respondent1LitigationFriend = LitigationFriend.builder()
            .partyID("res-1-litfriend-party-id")
            .fullName("Mr Litigation Friend")
            .firstName("Litigation")
            .lastName("Friend")
            .primaryAddress(AddressBuilder.defaults().build())
            .hasSameAddressAsLitigant(YES)
            .certificateOfSuitability(List.of())
            .build();
        LocalDateTime tomrrowsDateTime = LocalDateTime.now().plusDays(1);
        this.respondent1LitigationFriendDate = tomrrowsDateTime;
        this.respondent1LitigationFriendCreatedDate = tomrrowsDateTime;
        return this;
    }

    public CaseDataBuilder addGenericRespondentLitigationFriend() {
        this.genericLitigationFriend = LitigationFriend.builder()
            .fullName("Mr Litigation Friend")
            .build();
        return this;
    }

    public CaseDataBuilder addRespondent2LitigationFriend() {
        this.respondent2LitigationFriend = LitigationFriend.builder()
            .partyID("res-2-litfriend-party-id")
            .fullName("Mr Litigation Friend")
            .firstName("Litigation")
            .lastName("Friend")
            .primaryAddress(AddressBuilder.defaults().build())
            .hasSameAddressAsLitigant(YES)
            .certificateOfSuitability(List.of())
            .build();
        LocalDateTime tomrrowsDateTime = LocalDateTime.now().plusDays(1);
        this.respondent2LitigationFriendDate = tomrrowsDateTime;
        this.respondent2LitigationFriendCreatedDate = tomrrowsDateTime;
        return this;
    }

    public CaseDataBuilder addBothRespondent1LitigationFriend() {
        this.respondent1LitigationFriend = LitigationFriend.builder()
            .fullName("Mr Litigation Friend")
            .build();
        this.respondent2LitigationFriend = LitigationFriend.builder()
            .fullName("Mr Litigation Friend 2")
            .build();
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantSolicitors() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        this.respondent2Represented = YES;
        this.respondent2SameLegalRepresentative = NO;
        this.respondentSolicitor2Reference = "01234";
        this.solicitorReferences = SolicitorReferences.builder()
            .applicantSolicitor1Reference("12345")
            .respondentSolicitor1Reference("6789")
            .respondentSolicitor2Reference("01234")
            .build();
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantsLiP() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        this.respondent2Represented = NO;
        this.respondent1Represented = NO;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantSolicitorsForSdoMP() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        this.respondent2SameLegalRepresentative = NO;
        this.respondentSolicitor2Reference = "01234";
        respondent2ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilder multiPartyClaimOneDefendantSolicitor() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        this.respondent2SameLegalRepresentative = YES;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantSolicitorsSpec() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        this.respondent2SameLegalRepresentative = NO;
        this.respondentSolicitor2Reference = "01234";
        this.specRespondent1Represented = YES;
        this.specRespondent2Represented = YES;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantSameSolicitorsSpec() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build();
        this.respondent2SameLegalRepresentative = YES;
        this.respondentSolicitor2Reference = "01234";
        this.specRespondent1Represented = YES;
        this.specRespondent2Represented = YES;
        return this;
    }

    public CaseDataBuilder multiPartyClaimOneClaimant1ClaimResponseType() {
        this.claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilder respondent1ClaimResponseTypeToApplicant2Spec() {
        this.respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoApplicants() {
        this.addApplicant2 = YES;
        this.applicant2 = PartyBuilder.builder().individual("Jason").build()
            .toBuilder().partyID("app-2-party-id").build();
        return this;
    }

    private List<CaseData> get2v1DifferentResponseCase() {
        Party applicant1 = Party.builder().build();
        Party applicant2 = Party.builder().build();
        List<CaseData> cases = new ArrayList<>();
        for (RespondentResponseTypeSpec r1 : RespondentResponseTypeSpec.values()) {
            for (RespondentResponseTypeSpec r2 : RespondentResponseTypeSpec.values()) {
                if (!r1.equals(r2)) {
                    cases.add(CaseData.builder()
                                  .applicant1(applicant1)
                                  .applicant2(applicant2)
                                  .claimant1ClaimResponseTypeForSpec(r1)
                                  .claimant2ClaimResponseTypeForSpec(r2)
                                  .build());
                }
            }
        }
        return cases;
    }

    public CaseDataBuilder setClaimTypeToSpecClaim() {
        this.caseAccessCategory = SPEC_CLAIM;
        return this;
    }

    public CaseDataBuilder setClaimTypeToUnspecClaim() {
        this.caseAccessCategory = UNSPEC_CLAIM;
        return this;
    }

    public CaseDataBuilder setClaimNotificationDate() {
        claimNotificationDate = issueDate.plusDays(1).atStartOfDay();
        return this;
    }

    public CaseDataBuilder respondent2Responds(RespondentResponseType responseType) {
        this.respondent2ClaimResponseType = responseType;
        this.respondent2ResponseDate = LocalDateTime.now().plusDays(3);
        return this;
    }

    public CaseDataBuilder respondent2Responds1v2SameSol(RespondentResponseType responseType) {
        this.respondent2ClaimResponseType = responseType;
        this.respondent2ResponseDate = respondent1ResponseDate;
        return this;
    }

    public CaseDataBuilder respondent2Responds1v2DiffSol(RespondentResponseType responseType) {
        this.respondent2ClaimResponseType = responseType;
        this.respondent2ResponseDate = respondent1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder respondent1ClaimResponseTypeToApplicant2(RespondentResponseType responseType) {
        this.respondent1ClaimResponseTypeToApplicant2 = responseType;
        return this;
    }

    public CaseDataBuilder respondent1ClaimResponseTypeToApplicant1(RespondentResponseType responseType) {
        this.respondent1ClaimResponseType = responseType;
        respondent1DQ();
        return this;
    }

    public CaseDataBuilder respondentResponseIsSame(YesOrNo isSame) {
        this.respondentResponseIsSame = isSame;
        return this;
    }

    public CaseDataBuilder respondent1Copy(Party party) {
        this.respondent1Copy = party;
        return this;
    }

    public CaseDataBuilder respondent2Copy(Party party) {
        this.respondent2Copy = party;
        return this;
    }

    public CaseDataBuilder generateYearsAndMonthsIncorrectInput() {
        atStateRespondentRespondToClaimFastTrack(RespondentResponseType.PART_ADMISSION);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now();
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;

        respondToClaimAdmitPartUnemployedLRspec = UnemployedComplexTypeLRspec.builder()
            .unemployedComplexTypeRequired("No")
            .lengthOfUnemployment(LengthOfUnemploymentComplexTypeLRspec.builder()
                                      .numberOfMonthsInUnemployment("1.5")
                                      .numberOfYearsInUnemployment("2.6")
                                      .build())
            .build();

        return this;
    }

    public CaseDataBuilder generatePaymentDateForAdmitPartResponse() {
        atStateRespondentRespondToClaimFastTrack(RespondentResponseType.PART_ADMISSION);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now();
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;

        respondToClaimAdmitPartLRspec = RespondToClaimAdmitPartLRspec.builder()
            .whenWillThisAmountBePaid(PAST_DATE)
            .build();

        return this;
    }

    public CaseDataBuilder generateRepaymentDateForAdmitPartResponse() {
        atStateRespondentRespondToClaimFastTrack(RespondentResponseType.PART_ADMISSION);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();

        respondent1RepaymentPlan = RepaymentPlanLRspec.builder().paymentAmount(BigDecimal.valueOf(9000))
            .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH).firstRepaymentDate(FUTURE_DATE).build();

        respondent2RepaymentPlan = RepaymentPlanLRspec.builder().paymentAmount(BigDecimal.valueOf(9000))
            .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH).firstRepaymentDate(FUTURE_DATE).build();

        return this;
    }

    public CaseDataBuilder generateDefendant2RepaymentDateForAdmitPartResponse() {
        atStateRespondentRespondToClaimFastTrack(RespondentResponseType.PART_ADMISSION);
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent2DQ();

        respondent2RepaymentPlan = RepaymentPlanLRspec.builder().paymentAmount(BigDecimal.valueOf(9000))
            .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH).firstRepaymentDate(FUTURE_DATE).build();

        return this;
    }

    public CaseDataBuilder receiveUpdatePaymentRequest() {
        atStateRespondentFullDefence();
        this.hearingFeePaymentDetails = PaymentDetails.builder()
            .customerReference("RC-1604-0739-2145-4711")
            .build();

        return this;
    }

    public CaseDataBuilder buildHmctsInternalCaseName() {
        String applicant2Name = applicant2 != null ? " and " + applicant2.getPartyName() : "";
        String respondent2Name = respondent2 != null ? " and " + respondent2.getPartyName() : "";

        this.caseNameHmctsInternal = String.format("%s%s v %s%s", applicant1.getPartyName(),
                                                   applicant2Name, respondent1.getPartyName(), respondent2Name);
        return this;
    }

    public CaseDataBuilder atSpecAoSApplicantCorrespondenceAddressRequired(
        YesOrNo specAoSApplicantCorrespondenceAddressRequired) {
        this.specAoSApplicantCorrespondenceAddressRequired = specAoSApplicantCorrespondenceAddressRequired;
        return this;
    }

    public CaseDataBuilder atSpecAoSApplicantCorrespondenceAddressDetails(
        Address specAoSApplicantCorrespondenceAddressDetails) {
        this.specAoSApplicantCorrespondenceAddressDetails = specAoSApplicantCorrespondenceAddressDetails;
        return this;
    }

    public CaseDataBuilder addRespondent1PinToPostLRspec(DefendantPinToPostLRspec respondent1PinToPostLRspec) {
        this.respondent1PinToPostLRspec = respondent1PinToPostLRspec;
        return this;
    }

    public CaseDataBuilder atSpecAoSRespondentCorrespondenceAddressRequired(
        YesOrNo specAosRespondentCorrespondenceAddressRequired) {
        this.specAoSRespondentCorrespondenceAddressRequired = specAosRespondentCorrespondenceAddressRequired;
        return this;
    }

    public CaseDataBuilder atSpecAoSRespondentCorrespondenceAddressDetails(
        Address specAoSRespondentCorrespondenceAddressDetails) {
        this.specAoSRespondentCorrespondenceAddressDetails = specAoSRespondentCorrespondenceAddressDetails;
        return this;
    }

    public CaseDataBuilder atSpecAoSRespondent2HomeAddressRequired(YesOrNo specAoSRespondent2HomeAddressRequired) {
        this.specAoSRespondent2HomeAddressRequired = specAoSRespondent2HomeAddressRequired;
        return this;
    }

    public CaseDataBuilder atSpecAoSRespondent2HomeAddressDetails(Address specAoSRespondent2HomeAddressDetails) {
        this.specAoSRespondent2HomeAddressDetails = specAoSRespondent2HomeAddressDetails;
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotified_1v2_andNotifyBothCoS() {
        atStateClaimDetailsNotified();
        multiPartyClaimTwoDefendantLips();
        respondent2 = PartyBuilder.builder().soleTrader().build();
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotified_1v2_1Lip_1Lr() {
        atStateClaimDetailsNotified();
        multiPartyClaimTwoDefendant1Lip1Lr();
        respondent2 = PartyBuilder.builder().soleTrader().build().toBuilder().partyID("res-2-party-id").build();
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotified_1v2_1Lr_1Lip() {
        atStateClaimDetailsNotified();
        multiPartyClaimTwoDefendant1Lr1Lip();
        respondent2 = PartyBuilder.builder().soleTrader().build().toBuilder().partyID("res-2-party-id").build();
        return this;
    }

    public CaseDataBuilder respondent1DQWitnessesRequiredSpec(YesOrNo respondent1DQWitnessesRequiredSpec) {
        this.respondent1DQWitnessesRequiredSpec = respondent1DQWitnessesRequiredSpec;
        return this;
    }

    public CaseDataBuilder respondent1DQWitnessesDetailsSpec(List<Element<Witness>> respondent1DQWitnessesDetailsSpec) {
        this.respondent1DQWitnessesDetailsSpec = respondent1DQWitnessesDetailsSpec;
        return this;
    }

    public CaseDataBuilder caseAccessCategory(CaseCategory caseAccessCategory) {
        this.caseAccessCategory = caseAccessCategory;
        return this;
    }

    public CaseDataBuilder caseManagementLocation(CaseLocationCivil caseManagementLocation) {
        this.caseManagementLocation = caseManagementLocation;
        return this;
    }

    public CaseDataBuilder removeSolicitorReferences() {
        this.solicitorReferences = null;
        this.respondentSolicitor2Reference = null;
        return this;
    }

    public CaseDataBuilder transferCourtLocationList(DynamicList transferCourtLocationList) {
        this.transferCourtLocationList = transferCourtLocationList;
        return this;
    }

    public CaseDataBuilder reasonForTransfer(String reasonForTransfer) {
        this.reasonForTransfer = reasonForTransfer;
        return this;
    }

    public CaseDataBuilder flightDelay(FlightDelayDetails flightDelayDetails) {
        this.flightDelayDetails = flightDelayDetails;
        return this;
    }

    public CaseDataBuilder isFlightDelayClaim(YesOrNo isFlightDelayClaim) {
        this.isFlightDelayClaim = isFlightDelayClaim;
        return this;
    }

    public CaseDataBuilder reasonForReconsideration(ReasonForReconsideration reasonForReconsideration) {
        this.reasonForReconsideration = reasonForReconsideration;
        return this;
    }

    public CaseData buildMakePaymentsCaseData() {
        Organisation orgId = Organisation.builder()
            .organisationID("OrgId").build();

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(100)).code("CODE").build())
            .claimIssuedPBADetails(
                SRPbaDetails.builder()
                    .fee(
                        Fee.builder()
                            .code("FE203")
                            .calculatedAmountInPence(BigDecimal.valueOf(27500))
                            .version("1")
                            .build())
                    .serviceReqReference(CUSTOMER_REFERENCE).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(orgId).build())
            .build();
    }

    public CaseData buildCuiCaseDataWithFee() {
        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(100)).code("CODE").build())
            .build();
    }

    public CaseData buildMakePaymentsCaseDataWithoutClaimIssuedPbaDetails() {
        Organisation orgId = Organisation.builder()
            .organisationID("OrgId").build();

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(orgId).build())
            .build();
    }

    public CaseData buildMakePaymentsCaseDataWithoutServiceRequestReference() {
        Organisation orgId = Organisation.builder()
            .organisationID("OrgId").build();

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimIssuedPBADetails(
                SRPbaDetails.builder()
                    .fee(
                        Fee.builder()
                            .code("FE203")
                            .calculatedAmountInPence(BigDecimal.valueOf(27500))
                            .version("1")
                            .build()).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(orgId).build())
            .build();
    }

    public CaseData buildMakePaymentsCaseDataWithHearingDueDateWithHearingFeePBADetails() {
        Organisation orgId = Organisation.builder()
            .organisationID("OrgId").build();

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimIssuedPBADetails(
                SRPbaDetails.builder()
                    .fee(
                        Fee.builder()
                            .code("FE203")
                            .calculatedAmountInPence(BigDecimal.valueOf(27500))
                            .version("1")
                            .build())
                    .serviceReqReference(CUSTOMER_REFERENCE).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(orgId).build())
            .hearingDueDate(LocalDate.now().plusWeeks(2))
            .hearingFeePBADetails(SRPbaDetails.builder()
                                      .fee(
                                          Fee.builder()
                                              .code("FE203")
                                              .calculatedAmountInPence(BigDecimal.valueOf(27500))
                                              .version("1")
                                              .build())
                                      .serviceReqReference(CUSTOMER_REFERENCE).build())
            .build();
    }

    public CaseData withHearingFeePBADetailsPaymentFailed() {
        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .hearingFeePBADetails(SRPbaDetails.builder()
                                      .fee(
                                          Fee.builder()
                                              .code("FE203")
                                              .calculatedAmountInPence(BigDecimal.valueOf(27500))
                                              .version("1")
                                              .build())
                                      .paymentDetails(PaymentDetails.builder()
                                                          .status(FAILED)
                                                          .build())
                                      .serviceReqReference(CUSTOMER_REFERENCE).build())
            .build();
    }

    public CaseData withHearingFeePBADetailsPaymentSuccess() {
        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .hearingFeePBADetails(SRPbaDetails.builder()
                                      .fee(
                                          Fee.builder()
                                              .code("FE203")
                                              .calculatedAmountInPence(BigDecimal.valueOf(27500))
                                              .version("1")
                                              .build())
                                      .paymentDetails(PaymentDetails.builder()
                                                          .status(SUCCESS)
                                                          .build())
                                      .serviceReqReference(CUSTOMER_REFERENCE).build())
            .build();
    }

    public CaseData withHearingFeePBADetailsNoPaymentStatus() {
        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimValue(ClaimValue.builder()
                            .statementOfValueInPennies(BigDecimal.valueOf(10800))
                            .build())
            .allocatedTrack(SMALL_CLAIM)
            .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(
                Organisation.builder()
                    .organisationID("OrgId").build()).build())
            .build();
    }

    public CaseData buildMakePaymentsCaseDataWithHearingDate() {
        Organisation orgId = Organisation.builder()
            .organisationID("OrgId").build();

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimIssuedPBADetails(
                SRPbaDetails.builder()
                    .fee(
                        Fee.builder()
                            .code("FE203")
                            .calculatedAmountInPence(BigDecimal.valueOf(27500))
                            .version("1")
                            .build())
                    .serviceReqReference(CUSTOMER_REFERENCE).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(orgId).build())
            .hearingDate(LocalDate.now().plusWeeks(2))
            .hearingDueDate(LocalDate.now().plusWeeks(2))
            .build();
    }

    public CaseData buildMakePaymentsCaseDataWithHearingDueDateWithoutClaimIssuedPbaDetails() {
        Organisation orgId = Organisation.builder()
            .organisationID("OrgId").build();

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimIssuedPBADetails(
                SRPbaDetails.builder()
                    .fee(
                        Fee.builder()
                            .code("FE203")
                            .calculatedAmountInPence(BigDecimal.valueOf(27500))
                            .version("1")
                            .build())
                    .serviceReqReference(CUSTOMER_REFERENCE).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(orgId).build())
            .hearingDate(LocalDate.now().plusWeeks(2))
            .hearingDueDate(LocalDate.now().plusWeeks(2))
            .build();
    }

    public CaseData buildMakePaymentsCaseDataWithHearingDateWithoutClaimIssuedPbaDetails() {
        uk.gov.hmcts.reform.ccd.model.Organisation orgId = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
            .organisationID("OrgId").build();

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(orgId).build())
            .hearingDueDate(LocalDate.now().plusWeeks(2))
            .build();
    }

    public CaseData buildMakePaymentsCaseDataWithHearingDateWithHearingFeePBADetails() {
        uk.gov.hmcts.reform.ccd.model.Organisation orgId = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
            .organisationID("OrgId").build();

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimIssuedPBADetails(
                SRPbaDetails.builder()
                    .fee(
                        Fee.builder()
                            .code("FE203")
                            .calculatedAmountInPence(BigDecimal.valueOf(27500))
                            .version("1")
                            .build())
                    .serviceReqReference(CUSTOMER_REFERENCE).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(orgId).build())
            .hearingDate(LocalDate.now().plusWeeks(2))
            .hearingFeePBADetails(SRPbaDetails.builder()
                                      .fee(
                                          Fee.builder()
                                              .code("FE203")
                                              .calculatedAmountInPence(BigDecimal.valueOf(27500))
                                              .version("1")
                                              .build())
                                      .serviceReqReference(CUSTOMER_REFERENCE).build())
            .build();
    }

    public CaseData buildClaimIssuedPaymentCaseData() {
        Organisation orgId = Organisation.builder()
            .organisationID("OrgId").build();

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .ccdState(PENDING_CASE_ISSUED)
            .claimFee(
                Fee.builder()
                            .code("FE203")
                            .calculatedAmountInPence(BigDecimal.valueOf(27500))
                            .version("1")
                            .build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(orgId).build())
            .applicant1(Party.builder()
                            .individualFirstName("First name")
                            .individualLastName("Second name")
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("test").build())
            .build();
    }

    public CaseData buildPaymentFailureCaseData() {
        Organisation orgId = Organisation.builder()
            .organisationID("OrgId").build();

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .ccdCaseReference(1644495739087775L)
            .legacyCaseReference("000DC001")
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .claimIssuedPBADetails(
                SRPbaDetails.builder()
                    .paymentDetails(PaymentDetails.builder()
                                        .status(PaymentStatus.FAILED)
                                        .reference("RC-1658-4258-2679-9795")
                                        .customerReference(CUSTOMER_REFERENCE)
                                        .build())
                    .fee(
                        Fee.builder()
                            .code("FE203")
                            .calculatedAmountInPence(BigDecimal.valueOf(27500))
                            .version("1")
                            .build())
                    .serviceReqReference(CUSTOMER_REFERENCE).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(orgId).build())
            .build();
    }

    public CaseData buildPaymentSuccessfulCaseData() {
        Organisation orgId = Organisation.builder()
            .organisationID("OrgId").build();

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .ccdCaseReference(1644495739087775L)
            .legacyCaseReference("000DC001")
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .claimIssuedPBADetails(
                SRPbaDetails.builder()
                    .paymentSuccessfulDate(LocalDateTime.of(
                        LocalDate.of(2020, 01, 01),
                        LocalTime.of(12, 00, 00)
                    ))
                    .paymentDetails(PaymentDetails.builder()
                                        .status(PaymentStatus.SUCCESS)
                                        .reference("RC-1234-1234-1234-1234")
                                        .customerReference(CUSTOMER_REFERENCE)
                                        .build())
                    .fee(
                        Fee.builder()
                            .code("FE203")
                            .calculatedAmountInPence(BigDecimal.valueOf(27500))
                            .version("1")
                            .build())
                    .serviceReqReference(CUSTOMER_REFERENCE).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(orgId).build())
            .build();
    }

    public CaseData buildJudmentOnlineCaseDataWithPaymentByInstalment() {
        return build().toBuilder()
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .joJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER)
            .joJudgmentInstalmentDetails(JudgmentInstalmentDetails.builder()
                                             .firstInstalmentDate(LocalDate.of(2022, 12, 12))
                                             .instalmentAmount("120")
                                             .paymentFrequency(PaymentFrequency.MONTHLY).build())
            .joAmountOrdered("1200")
            .joAmountCostOrdered("1100")
            .joPaymentPlanSelection(PaymentPlanSelection.PAY_IN_INSTALMENTS)
            .joOrderMadeDate(LocalDate.of(2022, 12, 12))
            .joIsRegisteredWithRTL(YES).build();
    }

    public CaseData buildJudmentOnlineCaseDataWithPaymentImmediately() {
        return build().toBuilder()
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .joJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER)
            .joAmountOrdered("1200")
            .joAmountCostOrdered("1100")
            .joPaymentPlanSelection(PaymentPlanSelection.PAY_IMMEDIATELY)
            .joOrderMadeDate(LocalDate.of(2022, 12, 12))
            .joIsRegisteredWithRTL(YES).build();
    }

    public CaseData buildJudmentOnlineCaseDataWithPaymentByDate() {
        return build().toBuilder()
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .joJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER)
            .joAmountOrdered("1200")
            .joAmountCostOrdered("1100")
            .joPaymentPlanSelection(PaymentPlanSelection.PAY_BY_DATE)
            .joOrderMadeDate(LocalDate.of(2022, 12, 12))
            .joPaymentToBeMadeByDate(LocalDate.of(2023, 12, 12))
            .joIsRegisteredWithRTL(YES).build();
    }

    public CaseData buildJudgmentOnlineCaseWithMarkJudgementPaidAfter30Days() {
        JudgmentStatusDetails judgmentStatusDetails = JudgmentStatusDetails.builder()
            .judgmentStatusTypes(JudgmentStatusType.SATISFIED)
            .lastUpdatedDate(LocalDateTime.now()).build();
        return build().toBuilder()
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .joOrderMadeDate(LocalDate.of(2023, 7, 1))
            .joJudgmentPaidInFull(JudgmentPaidInFull.builder()
                                      .dateOfFullPaymentMade(LocalDate.of(2023, 9, 15))
                                      .confirmFullPaymentMade(List.of("CONFIRMED"))
                                      .build())
            .joIsRegisteredWithRTL(YES)
            .joJudgmentStatusDetails(judgmentStatusDetails).build();
    }

    public CaseData buildJudgmentOnlineCaseWithMarkJudgementPaidWithin30Days() {
        JudgmentStatusDetails judgmentStatusDetails = JudgmentStatusDetails.builder()
            .judgmentStatusTypes(JudgmentStatusType.SATISFIED)
            .lastUpdatedDate(LocalDateTime.now()).build();
        return build().toBuilder()
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .joOrderMadeDate(LocalDate.of(2023, 9, 1))
            .joJudgmentPaidInFull(JudgmentPaidInFull.builder()
                                      .dateOfFullPaymentMade(LocalDate.of(2023, 9, 15))
                                      .confirmFullPaymentMade(List.of("CONFIRMED"))
                                      .build())
            .joIsRegisteredWithRTL(YES)
            .joJudgmentStatusDetails(judgmentStatusDetails).build();
    }

    public CaseDataBuilder setUnassignedCaseListDisplayOrganisationReferences() {
        this.unassignedCaseListDisplayOrganisationReferences = "Organisation references String";
        return this;
    }

    public CaseDataBuilder setCaseListDisplayDefendantSolicitorReferences(boolean isOneDefendantSolicitor) {
        if (!isOneDefendantSolicitor) {
            this.caseListDisplayDefendantSolicitorReferences =
                this.solicitorReferences.getRespondentSolicitor1Reference() + this.respondentSolicitor2Reference;
        } else {
            this.caseListDisplayDefendantSolicitorReferences =
                this.solicitorReferences.getRespondentSolicitor1Reference();
        }
        return this;
    }

    public CaseDataBuilder setCoSClaimDetailsWithDate(boolean setCos1, boolean setCos2,
                                                      LocalDate cos1Date, LocalDate cos2Date,
                                                      boolean file1, boolean file2) {
        List<Element<Document>> files = wrapElements(Document.builder()
                .documentUrl("fake-url")
                .documentFileName("file-name")
                .documentBinaryUrl("binary-url")
                .build());
        List<Element<Document>> files2 = wrapElements(Document.builder()
                .documentUrl("fake-url2")
                .documentFileName("file-name2")
                .documentBinaryUrl("binary-url2")
                .build());
        ArrayList<String> cosUIStatement = new ArrayList<>();
        cosUIStatement.add("CERTIFIED");
        if (setCos1) {
            CertificateOfService.CertificateOfServiceBuilder cos1Builder = CertificateOfService.builder()
                .cosDateOfServiceForDefendant(cos1Date);
            if (file1) {
                cos1Builder.cosEvidenceDocument(files);
            }
            this.cosNotifyClaimDetails1 = cos1Builder.build();
        }
        if (setCos2) {
            CertificateOfService.CertificateOfServiceBuilder cos2Builder = CertificateOfService.builder()
                .cosDateOfServiceForDefendant(cos2Date);
            if (file2) {
                cos2Builder.cosEvidenceDocument(files2);
            }
            this.cosNotifyClaimDetails2 = cos2Builder.build();
        }
        return this;
    }

    public CaseDataBuilder ccjPaymentDetails(CCJPaymentDetails ccjPaymentDetails) {
        this.ccjPaymentDetails = ccjPaymentDetails;
        return this;
    }

    public CaseDataBuilder specRespondent1Represented(YesOrNo specRespondent1Represented) {
        this.specRespondent1Represented = specRespondent1Represented;
        return this;
    }

    public CaseDataBuilder claimFee(Fee fee) {
        this.claimFee = fee;
        return this;
    }

    public CaseDataBuilder totalInterest(BigDecimal interest) {
        this.totalInterest = interest;
        return this;
    }

    public CaseDataBuilder applicant1AcceptAdmitAmountPaidSpec(YesOrNo isPaymemtAccepted) {
        this.applicant1AcceptAdmitAmountPaidSpec = isPaymemtAccepted;
        return this;
    }

    public CaseDataBuilder applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo isPartPaymentAccepted) {
        this.applicant1AcceptPartAdmitPaymentPlanSpec = isPartPaymentAccepted;
        return this;
    }

    public CaseDataBuilder respondToAdmittedClaimOwingAmountPounds(BigDecimal admitedCliaimAmount) {
        this.respondToAdmittedClaimOwingAmountPounds = admitedCliaimAmount;
        return this;
    }

    public CaseDataBuilder addApplicant1ExpertsAndWitnesses() {
        this.applicant1DQ = applicant1DQ.toBuilder()
            .applicant1DQExperts(Experts.builder()
                                     .expertRequired(YES)
                                     .details(wrapElements(
                                         Expert.builder()
                                             .firstName("Applicant")
                                             .lastName("Expert")
                                             .build()
                                     ))
                                     .build())
            .applicant1DQWitnesses(Witnesses.builder()
                                       .witnessesToAppear(YES)
                                       .details(wrapElements(
                                           Witness.builder()
                                               .firstName("Applicant")
                                               .lastName("Witness")
                                               .build()
                                       ))
                                       .build())
            .build();
        this.applicantExperts = wrapElements(PartyFlagStructure.builder()
                                                   .partyID("app-1-expert-party-id")
                                                   .firstName("Applicant")
                                                   .lastName("Expert")
                                                   .build());
        this.applicantWitnesses = wrapElements(PartyFlagStructure.builder()
                                                   .partyID("app-1-witness-party-id")
                                                   .firstName("Applicant")
                                                   .lastName("Witness")
                                                   .build());
        return this;
    }

    public CaseDataBuilder addApplicant2ExpertsAndWitnesses() {
        this.applicant2DQ = applicant2DQ.toBuilder()
            .applicant2DQExperts(Experts.builder()
                                     .expertRequired(YES)
                                     .details(wrapElements(
                                         Expert.builder()
                                             .firstName("Applicant Two")
                                             .lastName("Expert")
                                             .build()
                                     ))
                                     .build())
            .applicant2DQWitnesses(Witnesses.builder()
                                       .witnessesToAppear(YES)
                                       .details(wrapElements(
                                           Witness.builder()
                                               .firstName("Applicant Two")
                                               .lastName("Witness")
                                               .build()
                                       ))
                                       .build())
            .build();
        this.applicantExperts = wrapElements(PartyFlagStructure.builder()
                                                 .partyID("app-2-expert-party-id")
                                                 .firstName("Applicant Two")
                                                 .lastName("Expert")
                                                 .build());
        this.applicantWitnesses = wrapElements(PartyFlagStructure.builder()
                                                   .partyID("app-2-witness-party-id")
                                                   .firstName("Applicant Two")
                                                   .lastName("Witness")
                                                   .build());
        return this;
    }

    public CaseDataBuilder addRespondent1ExpertsAndWitnesses() {
        this.respondent1DQ = respondent1DQ.toBuilder()
            .respondent1DQExperts(Experts.builder()
                                      .expertRequired(YES)
                                      .details(wrapElements(
                                          Expert.builder()
                                              .firstName("Respondent")
                                              .lastName("Expert")
                                              .build()
                                      ))
                                      .build())
            .respondent1DQWitnesses(Witnesses.builder()
                                        .witnessesToAppear(YES)
                                        .details(wrapElements(
                                            Witness.builder()
                                                .firstName("Respondent")
                                                .lastName("Witness")
                                                .build()
                                        ))
                                        .build())
            .build();
        this.respondent1Experts = wrapElements(PartyFlagStructure.builder()
                                                   .partyID("res-1-expert-party-id")
                                                 .firstName("Respondent")
                                                 .lastName("Expert")
                                                 .build());
        this.respondent1Witnesses = wrapElements(PartyFlagStructure.builder()
                                                     .partyID("res-1-witness-party-id")
                                                   .firstName("Respondent")
                                                   .lastName("Witness")
                                                   .build());
        return this;
    }

    public CaseDataBuilder addRespondent2ExpertsAndWitnesses() {
        this.respondent2DQ = respondent2DQ.toBuilder()
            .respondent2DQExperts(Experts.builder()
                                      .expertRequired(YES)
                                      .details(wrapElements(
                                          Expert.builder()
                                              .firstName("Respondent Two")
                                              .lastName("Expert")
                                              .build()
                                      ))
                                      .build())
            .respondent2DQWitnesses(Witnesses.builder()
                                        .witnessesToAppear(YES)
                                        .details(wrapElements(
                                            Witness.builder()
                                                .firstName("Respondent Two")
                                                .lastName("Witness")
                                                .build()
                                        ))
                                        .build())
            .build();
        this.respondent2Experts = wrapElements(PartyFlagStructure.builder()
                                                   .partyID("res-2-expert-party-id")
                                                   .firstName("Respondent Two")
                                                   .lastName("Expert")
                                                   .build());
        this.respondent2Witnesses = wrapElements(PartyFlagStructure.builder()
                                                     .partyID("res-2-witness-party-id")
                                                     .firstName("Respondent Two")
                                                     .lastName("Witness")
                                                     .build());
        return this;
    }

    public CaseDataBuilder withApplicant1Flags() {
        return withApplicant1Flags(flagDetails());
    }

    public CaseDataBuilder withApplicant1Flags(List<Element<FlagDetail>> flags) {
        this.applicant1 = applicant1.toBuilder()
            .partyID("res-1-party-id")
            .flags(Flags.builder()
                       .partyName(applicant1.getPartyName())
                       .roleOnCase("Claimant 1")
                       .details(flags)
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilder withApplicant1WitnessFlags() {
        this.applicantWitnesses = wrapElements(PartyFlagStructure.builder()
                                                   .firstName("W first")
                                                   .lastName("W last")
                                                   .flags(Flags.builder()
                                                              .partyName("W First W Last")
                                                              .roleOnCase("Claimant 1 Witness")
                                                              .details(flagDetails())
                                                              .build())
                                                   .build());
        return this;
    }

    public CaseDataBuilder withApplicant1ExpertFlags() {
        this.applicantExperts = wrapElements(PartyFlagStructure.builder()
                                                 .firstName("E first")
                                                 .lastName("E last")
                                                 .flags(Flags.builder()
                                                            .partyName("E First E Last")
                                                            .roleOnCase("Claimant 1 Expert")
                                                            .details(flagDetails())
                                                            .build())
                                                 .build());
        return this;
    }

    public CaseDataBuilder withApplicant1LitigationFriendFlags() {
        this.applicant1LitigationFriend = applicant1LitigationFriend.toBuilder()
            .flags(Flags.builder()
                       .partyName(applicant1LitigationFriend.getFullName())
                       .roleOnCase("Claimant 1 Litigation Friend")
                       .details(flagDetails())
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilder withApplicant2Flags() {
        this.applicant2 = applicant2.toBuilder()
            .flags(Flags.builder()
                       .partyName(applicant2.getPartyName())
                       .roleOnCase("Claimant 2")
                       .details(flagDetails())
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilder withApplicant2WitnessFlags() {
        this.applicantWitnesses = wrapElements(PartyFlagStructure.builder()
                                                   .firstName("W first")
                                                   .lastName("W last")
                                                   .flags(Flags.builder()
                                                              .partyName("W First W Last")
                                                              .roleOnCase("Claimant 2 Witness")
                                                              .details(flagDetails())
                                                              .build())
                                                   .build());
        return this;
    }

    public CaseDataBuilder withApplicant2ExpertFlags() {
        this.applicantExperts = wrapElements(PartyFlagStructure.builder()
                                                 .firstName("E first")
                                                 .lastName("E last")
                                                 .flags(Flags.builder()
                                                            .partyName("E First E Last")
                                                            .roleOnCase("Claimant 2 Expert")
                                                            .details(flagDetails())
                                                            .build())
                                                 .build());
        return this;
    }

    public CaseDataBuilder withApplicant2LitigationFriendFlags() {
        this.applicant2LitigationFriend = applicant2LitigationFriend.toBuilder()
            .flags(Flags.builder()
                       .partyName(applicant2LitigationFriend.getFullName())
                       .roleOnCase("Claimant 2 Litigation Friend")
                       .details(flagDetails())
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilder withRespondent1LitigationFriendFlags() {
        return withRespondent1LitigationFriendFlags(flagDetails());
    }

    public CaseDataBuilder withRespondent1LitigationFriendFlags(List<Element<FlagDetail>> flags) {
        this.respondent1LitigationFriend = respondent1LitigationFriend.toBuilder()
            .partyID("res-1-litfriend-party-id")
            .flags(Flags.builder()
                       .partyName(respondent1LitigationFriend.getFullName())
                       .roleOnCase("Defendant 1 Litigation Friend")
                       .details(flags)
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilder withRespondent1Flags() {
        return withRespondent1Flags(flagDetails());
    }

    public CaseDataBuilder withRespondent1Flags(List<Element<FlagDetail>> flags) {
        this.respondent1 = respondent1.toBuilder()
            .partyID("res-1-party-id")
            .flags(Flags.builder()
                       .partyName(respondent1.getPartyName())
                       .roleOnCase("Defendant 1")
                       .details(flags)
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilder withRespondent1WitnessFlags() {
        this.respondent1Witnesses = wrapElements(
            PartyFlagStructure.builder()
                .partyID("res-1-witness-party-id")
                .firstName("W first")
                .lastName("W last")
                .flags(Flags.builder()
                           .partyName("W First W Last")
                           .roleOnCase("Defendant 1 Witness")
                           .details(flagDetails())
                           .build())
                .build());
        return this;
    }

    public CaseDataBuilder withRespondent1ExpertFlags() {
        this.respondent1Experts = wrapElements(
            PartyFlagStructure.builder()
                .partyID("res-1-expert-party-id")
                .firstName("E first")
                .lastName("E last")
                .flags(Flags.builder()
                           .partyName("E First E Last")
                           .roleOnCase("Defendant 1 Expert")
                           .details(flagDetails())
                           .build())
                .build());
        return this;
    }

    public CaseDataBuilder withRespondent2Flags() {
        this.respondent2 = respondent2.toBuilder()
            .flags(Flags.builder()
                       .partyName(respondent2.getPartyName())
                       .roleOnCase("Defendant 2")
                       .details(flagDetails())
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilder withRespondent2ExpertFlags() {
        this.respondent2Experts = wrapElements(PartyFlagStructure.builder()
                                                   .firstName("E first")
                                                   .lastName("E last")
                                                   .flags(Flags.builder()
                                                              .partyName("E First E Last")
                                                              .roleOnCase("Defendant 2 Expert")
                                                              .details(flagDetails())
                                                              .build())
                                                   .build());
        return this;
    }

    public CaseDataBuilder withRespondent2WitnessFlags() {
        this.respondent2Witnesses = wrapElements(PartyFlagStructure.builder()
                                                     .firstName("W first")
                                                     .lastName("W last")
                                                     .flags(Flags.builder()
                                                                .partyName("W First W Last")
                                                                .roleOnCase("Defendant 2 Witness")
                                                                .details(flagDetails())
                                                                .build())
                                                     .build());
        return this;
    }

    public CaseDataBuilder withRespondent2LitigationFriendFlags() {
        this.respondent2LitigationFriend = respondent2LitigationFriend.toBuilder()
            .flags(Flags.builder()
                       .partyName(respondent2LitigationFriend.getFullName())
                       .roleOnCase("Defendant 2 Litigation Friend")
                       .details(flagDetails())
                       .build())
            .build();
        return this;
    }

    public List<Element<FlagDetail>> flagDetails() {
        FlagDetail details1 = FlagDetail.builder()
            .name("Vulnerable user")
            .flagComment("comment")
            .flagCode("AB001")
            .hearingRelevant(YES)
            .status("Active")
            .build();

        FlagDetail details2 = FlagDetail.builder()
            .name("Flight risk")
            .flagComment("comment")
            .flagCode("SM001")
            .hearingRelevant(YES)
            .status("Active")
            .build();

        FlagDetail details3 = FlagDetail.builder()
            .name("Audio/Video evidence")
            .flagComment("comment")
            .flagCode("RA001")
            .hearingRelevant(NO)
            .status("Active")
            .build();

        FlagDetail details4 = FlagDetail.builder()
            .name("Other")
            .flagComment("comment")
            .flagCode("AB001")
            .hearingRelevant(YES)
            .status("Inactive")
            .build();

        return wrapElements(details1, details2, details3, details4);
    }

    public CaseDataBuilder applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo intentionToSettle) {
        this.applicant1PartAdmitIntentionToSettleClaimSpec = intentionToSettle;
        return this;
    }

    public CaseDataBuilder responseClaimTrack(String claimType) {
        this.responseClaimTrack = claimType;
        return this;
    }

    public CaseDataBuilder setClaimantMediationFlag(YesOrNo response) {
        respondent1MediationRequired = response;
        return this;
    }

    public CaseDataBuilder applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo confirmation) {
        this.applicant1PartAdmitConfirmAmountPaidSpec = confirmation;
        return this;
    }

    public CaseDataBuilder defendantSingleResponseToBothClaimants(YesOrNo response) {
        this.defendantSingleResponseToBothClaimants = response;
        return this;
    }

    public CaseDataBuilder caseDataLip(CaseDataLiP caseDataLiP) {
        this.caseDataLiP = caseDataLiP;
        return this;
    }

    public CaseDataBuilder specClaim1v1LrVsLip() {
        this.caseAccessCategory = SPEC_CLAIM;
        this.respondent1Represented = NO;
        return this;
    }

    public CaseDataBuilder enableRespondent2ResponseFlag() {
        this.claimant2ResponseFlag = YES;
        return this;
    }

    public CaseDataBuilder setSpecClaimResponseTimelineList(TimelineUploadTypeSpec timelineUploadTypeSpec) {
        this.specClaimResponseTimelineList = timelineUploadTypeSpec;
        return this;
    }

    public CaseDataBuilder setSpecClaimResponseTimelineList2(TimelineUploadTypeSpec timelineUploadTypeSpec2) {
        this.specClaimResponseTimelineList2 = timelineUploadTypeSpec2;
        return this;
    }

    public CaseDataBuilder setDefenceAdmitPartEmploymentTypeRequired(YesOrNo yesOrNo) {
        this.defenceAdmitPartEmploymentTypeRequired = defenceAdmitPartEmploymentTypeRequired;
        return this;
    }

    public CaseDataBuilder specDefenceFullAdmitted2Required(YesOrNo yesOrNo) {
        this.specDefenceFullAdmitted2Required = specDefenceFullAdmitted2Required;
        return this;
    }

    public CaseDataBuilder defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec respondentResponsePartAdmissionPaymentTimeLRspec) {
        this.defenceAdmitPartPaymentTimeRouteRequired = respondentResponsePartAdmissionPaymentTimeLRspec;
        return this;
    }

    public CaseDataBuilder showResponseOneVOneFlag(ResponseOneVOneShowTag showResponseOneVOneFlag) {
        this.showResponseOneVOneFlag = showResponseOneVOneFlag;
        return this;
    }

    public CaseDataBuilder claimantUserDetails(IdamUserDetails claimantUserDetails) {
        this.claimantUserDetails = claimantUserDetails;
        return this;
    }

    public CaseDataBuilder updateDetailsForm(UpdateDetailsForm form) {
        this.updateDetailsForm = form;
        return this;
    }

    public CaseDataBuilder atSmallClaimsWitnessStatementWithNegativeInputs() {
        atStateClaimNotified();
        this.smallClaimsWitnessStatement = SmallClaimsWitnessStatement.builder()
            .input2("-3")
            .input3("-3")
            .build();

        return this;
    }

    public CaseDataBuilder atFastTrackWitnessOfFactWithNegativeInputs() {
        atStateClaimNotified();
        this.fastTrackWitnessOfFact = FastTrackWitnessOfFact.builder()
            .input2("-3")
            .input3("-3")
            .build();

        return this;
    }

    public CaseDataBuilder atSmallClaimsWitnessStatementWithPositiveInputs() {
        atStateClaimNotified();
        this.smallClaimsWitnessStatement = SmallClaimsWitnessStatement.builder()
            .input2("3")
            .input3("3")
            .build();

        return this;
    }

    public CaseDataBuilder atFastTrackWitnessOfFactWithPositiveInputs() {
        atStateClaimNotified();
        this.fastTrackWitnessOfFact = FastTrackWitnessOfFact.builder()
            .input2("3")
            .input3("3")
            .build();

        return this;
    }

    public CaseDataBuilder atTrialHearingWitnessOfFactWithNegativeInputs() {
        atStateClaimNotified();
        this.trialHearingWitnessOfFactDJ = TrialHearingWitnessOfFact.builder()
            .input2("-3")
            .input3("-3")
            .build();

        return this;
    }

    public CaseDataBuilder addApplicantLRIndividual(String firstName, String lastName) {
        List<Element<PartyFlagStructure>> individual =
            wrapElements(PartyFlagStructure.builder()
                             .partyID("app-lr-ind-party-id")
                             .firstName(firstName)
                             .lastName(lastName)
                             .email("abc@def.ghi")
                             .phone("07777777777")
                             .build());
        if (this.applicant1LRIndividuals != null && !this.applicant1LRIndividuals.isEmpty()) {
            this.applicant1LRIndividuals.addAll(individual);
        } else {
            this.applicant1LRIndividuals = individual;
        }
        return this;
    }

    public CaseDataBuilder addRespondent1LRIndividual(String firstName, String lastName) {
        List<Element<PartyFlagStructure>> individual =
            wrapElements(PartyFlagStructure.builder()
                             .partyID("res-1-lr-ind-party-id")
                             .firstName(firstName)
                             .lastName(lastName)
                             .email("abc@def.ghi")
                             .phone("07777777777")
                             .build());
        if (this.respondent1LRIndividuals != null && !this.respondent1LRIndividuals.isEmpty()) {
            this.respondent1LRIndividuals.addAll(individual);
        } else {
            this.respondent1LRIndividuals = individual;
        }
        return this;
    }

    public CaseDataBuilder addRespondent2LRIndividual(String firstName, String lastName) {
        List<Element<PartyFlagStructure>> individual =
            wrapElements(PartyFlagStructure.builder()
                             .partyID("res-2-lr-ind-party-id")
                             .firstName(firstName)
                             .lastName(lastName)
                             .email("abc@def.ghi")
                             .phone("07777777777")
                             .build());
        if (this.respondent2LRIndividuals != null && !this.respondent2LRIndividuals.isEmpty()) {
            this.respondent2LRIndividuals.addAll(individual);
        } else {
            this.respondent2LRIndividuals = individual;
        }
        return this;
    }

    public CaseDataBuilder addApplicant1OrgIndividual(String firstName, String lastName) {
        List<Element<PartyFlagStructure>> individual =
            wrapElements(PartyFlagStructure.builder()
                             .partyID("app-1-org-ind-party-id")
                             .firstName(firstName)
                             .lastName(lastName)
                             .email("abc@def.ghi")
                             .phone("07777777777")
                             .build());
        if (this.applicant1OrgIndividuals != null && !this.applicant1OrgIndividuals.isEmpty()) {
            this.applicant1OrgIndividuals.addAll(individual);
        } else {
            this.applicant1OrgIndividuals = individual;
        }
        return this;
    }

    public CaseDataBuilder addApplicant2OrgIndividual(String firstName, String lastName) {
        List<Element<PartyFlagStructure>> individual =
            wrapElements(PartyFlagStructure.builder()
                             .partyID("app-2-org-ind-party-id")
                             .firstName(firstName)
                             .lastName(lastName)
                             .email("abc@def.ghi")
                             .phone("07777777777")
                             .build());
        if (this.applicant2OrgIndividuals != null && !this.applicant2OrgIndividuals.isEmpty()) {
            this.applicant2OrgIndividuals.addAll(individual);
        } else {
            this.applicant2OrgIndividuals = individual;
        }
        return this;
    }

    public CaseDataBuilder addRespondent1OrgIndividual(String firstName, String lastName) {
        List<Element<PartyFlagStructure>> individual =
            wrapElements(PartyFlagStructure.builder()
                             .partyID("res-1-org-ind-party-id")
                             .firstName(firstName)
                             .lastName(lastName)
                             .email("abc@def.ghi")
                             .phone("07777777777")
                             .build());
        if (this.respondent1OrgIndividuals != null && !this.respondent1OrgIndividuals.isEmpty()) {
            this.respondent1OrgIndividuals.addAll(individual);
        } else {
            this.respondent1OrgIndividuals = individual;
        }
        return this;
    }

    public CaseDataBuilder addRespondent2OrgIndividual(String firstName, String lastName) {
        List<Element<PartyFlagStructure>> individual =
            wrapElements(PartyFlagStructure.builder()
                             .partyID("res-2-org-ind-party-id")
                             .firstName(firstName)
                             .lastName(lastName)
                             .email("abc@def.ghi")
                             .phone("07777777777")
                             .build());
        if (this.respondent2OrgIndividuals != null && !this.respondent2OrgIndividuals.isEmpty()) {
            this.respondent2OrgIndividuals.addAll(individual);
        } else {
            this.respondent2OrgIndividuals = individual;
        }
        return this;
    }

    public static CaseDataBuilder builder() {
        return new CaseDataBuilder();
    }

    public CaseData build() {
        return CaseData.builder()
            // Create Claim
            .caseNameHmctsInternal(caseNameHmctsInternal)
            .legacyCaseReference(legacyCaseReference)
            .allocatedTrack(allocatedTrack)
            .generalAppType(generalAppType)
            .generalAppVaryJudgementType(generalAppVaryJudgementType)
            .generalAppN245FormUpload(generalAppN245FormUpload)
            .generalAppHearingDate(generalAppHearingDate)
            .solicitorReferences(solicitorReferences)
            .courtLocation(courtLocation)
            .claimValue(claimValue)
            .uploadParticularsOfClaim(uploadParticularsOfClaim)
            .claimType(claimType)
            .claimTypeOther(claimTypeOther)
            .personalInjuryType(personalInjuryType)
            .personalInjuryTypeOther(personalInjuryTypeOther)
            .applicantSolicitor1PbaAccounts(applicantSolicitor1PbaAccounts)
            .claimFee(claimFee)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .applicant1Represented(applicant1Represented)
            .respondent1(respondent1)
            .respondent2(respondent2)
            .respondent1Represented(respondent1Represented)
            .respondent2Represented(respondent2Represented)
            .respondent1OrgRegistered(respondent1OrgRegistered)
            .respondent2OrgRegistered(respondent2OrgRegistered)
            .respondentSolicitor1EmailAddress(respondentSolicitor1EmailAddress)
            .respondentSolicitor2EmailAddress(respondentSolicitor2EmailAddress)
            .applicantSolicitor1ClaimStatementOfTruth(applicantSolicitor1ClaimStatementOfTruth)
            .claimIssuedPaymentDetails(claimIssuedPaymentDetails)
            .paymentDetails(paymentDetails)
            .claimFee(claimFee)
            .hearingFeePaymentDetails(hearingFeePaymentDetails)
            .paymentReference(paymentReference)
            .applicantSolicitor1CheckEmail(applicantSolicitor1CheckEmail)
            .applicantSolicitor1UserDetails(applicantSolicitor1UserDetails)
            .interestClaimOptions(interestClaimOptions)
            .claimInterest(claimInterest)
            .sameRateInterestSelection(sameRateInterestSelection)
            .interestClaimFrom(interestClaimFrom)
            .interestClaimUntil(interestClaimUntil)
            .interestFromSpecificDate(interestFromSpecificDate)
            .breakDownInterestTotal(breakDownInterestTotal)
            .totalClaimAmount(totalClaimAmount)
            //Deadline extension
            .respondentSolicitor1AgreedDeadlineExtension(respondentSolicitor1AgreedDeadlineExtension)
            .respondentSolicitor2AgreedDeadlineExtension(respondentSolicitor2AgreedDeadlineExtension)
            // Acknowledge Claim
            .respondent1ClaimResponseIntentionType(respondent1ClaimResponseIntentionType)
            .respondent2ClaimResponseIntentionType(respondent2ClaimResponseIntentionType)
            // Defendant Response Defendant 1
            .respondent1ClaimResponseType(respondent1ClaimResponseType)
            .respondent1ClaimResponseDocument(respondent1ClaimResponseDocument)
            // Defendant Response Defendant 2
            .respondent2ClaimResponseType(respondent2ClaimResponseType)
            .respondent2ClaimResponseDocument(respondent2ClaimResponseDocument)
            .respondentResponseIsSame(respondentResponseIsSame)
            // Defendant Response 2 Applicants
            .respondent1ClaimResponseTypeToApplicant2(respondent1ClaimResponseTypeToApplicant2)
            // Claimant Response
            .applicant1ProceedWithClaim(applicant1ProceedWithClaim)
            .applicant1ProceedWithClaimMultiParty2v1(applicant1ProceedWithClaimMultiParty2v1)
            .applicant2ProceedWithClaimMultiParty2v1(applicant2ProceedWithClaimMultiParty2v1)
            .applicant1DefenceResponseDocument(applicant1DefenceResponseDocument)
            .claimantDefenceResDocToDefendant2(applicant2DefenceResponseDocument)
            .defendantDetails(defendantDetails)

            //Case procceds in Caseman
            .claimProceedsInCaseman(claimProceedsInCaseman)
            .claimProceedsInCasemanLR(claimProceedsInCasemanLR)

            .ccdState(ccdState)
            .businessProcess(businessProcess)
            .ccdCaseReference(ccdCaseReference)
            .systemGeneratedCaseDocuments(systemGeneratedCaseDocuments)
            .withdrawClaim(withdrawClaim)
            .discontinueClaim(discontinueClaim)
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .applicant1DQ(applicant1DQ)
            .applicant2DQ(applicant2DQ)
            .respondent2DQ(respondent2DQ)
            .respondentSolicitor1OrganisationDetails(respondentSolicitor1OrganisationDetails)
            .applicant1OrganisationPolicy(applicant1OrganisationPolicy)
            .respondent1OrganisationPolicy(respondent1OrganisationPolicy)
            .respondent2OrganisationPolicy(respondent2OrganisationPolicy)
            .addApplicant2(addApplicant2)
            .addRespondent2(addRespondent2)
            .respondent2SameLegalRepresentative(respondent2SameLegalRepresentative)
            .respondent1LitigationFriend(respondent1LitigationFriend)
            .applicant1LitigationFriend(applicant1LitigationFriend)
            .applicant1LitigationFriendRequired(applicant1LitigationFriendRequired)
            .applicant1AcceptFullAdmitPaymentPlanSpec(applicant1AcceptFullAdmitPaymentPlanSpec)
            .applicant2LitigationFriend(applicant2LitigationFriend)
            .applicant2LitigationFriendRequired(applicant2LitigationFriendRequired)
            .respondent1LitigationFriendDate(respondent1LitigationFriendDate)
            .respondent1LitigationFriendCreatedDate(respondent1LitigationFriendCreatedDate)
            .respondent2LitigationFriend(respondent2LitigationFriend)
            .respondent2LitigationFriendDate(respondent2LitigationFriendDate)
            .respondent2LitigationFriendCreatedDate(respondent2LitigationFriendCreatedDate)
            .genericLitigationFriend(genericLitigationFriend)
            //dates
            .submittedDate(submittedDate)
            .issueDate(issueDate)
            .claimNotificationDate(claimNotificationDate)
            .claimDetailsNotificationDate(claimDetailsNotificationDate)
            .paymentSuccessfulDate(paymentSuccessfulDate)
            .claimNotificationDeadline(claimNotificationDeadline)
            .claimDetailsNotificationDate(claimDetailsNotificationDate)
            .claimDetailsNotificationDeadline(claimDetailsNotificationDeadline)
            .servedDocumentFiles(servedDocumentFiles)
            .respondent1ResponseDeadline(respondent1ResponseDeadline)
            .respondent2ResponseDeadline(respondent2ResponseDeadline)
            .claimDismissedDeadline(claimDismissedDeadline)
            .respondent1TimeExtensionDate(respondent1TimeExtensionDate)
            .respondent2TimeExtensionDate(respondent2TimeExtensionDate)
            .respondent1AcknowledgeNotificationDate(respondent1AcknowledgeNotificationDate)
            .respondent2AcknowledgeNotificationDate(respondent2AcknowledgeNotificationDate)
            .respondent1ResponseDate(respondent1ResponseDate)
            .respondent2ResponseDate(respondent2ResponseDate)
            .applicant1ResponseDate(applicant1ResponseDate)
            .applicant2ResponseDate(applicant2ResponseDate)
            .applicant1ResponseDeadline(applicant1ResponseDeadline)
            .takenOfflineDate(takenOfflineDate)
            .takenOfflineByStaffDate(takenOfflineByStaffDate)
            .unsuitableSDODate(unsuitableSDODate)
            .claimDismissedDate(claimDismissedDate)
            .caseDismissedHearingFeeDueDate(caseDismissedHearingFeeDueDate)
            .addLegalRepDeadline(addLegalRepDeadline)
            .applicantSolicitor1ServiceAddress(applicantSolicitor1ServiceAddress)
            .respondentSolicitor1ServiceAddress(respondentSolicitor1ServiceAddress)
            .respondentSolicitor2ServiceAddress(respondentSolicitor2ServiceAddress)
            .isRespondent1(isRespondent1)
            .isRespondent2(isRespondent2)
            .defendantSolicitorNotifyClaimOptions(defendantSolicitorNotifyClaimOptions)
            .defendantSolicitorNotifyClaimDetailsOptions(defendantSolicitorNotifyClaimDetailsOptions)
            .selectLitigationFriend(selectLitigationFriend)
            .caseNotes(caseNotes)
            .hearingDueDate(hearingDueDate)
            .hearingDate(hearingDate)
            //ui field
            .uiStatementOfTruth(uiStatementOfTruth)
            .caseAccessCategory(caseAccessCategory == null ? UNSPEC_CLAIM : caseAccessCategory)
            .caseBundles(caseBundles)
            .respondToClaim(respondToClaim)
            //spec route
            .respondent1ClaimResponseTypeForSpec(respondent1ClaimResponseTypeForSpec)
            .respondToAdmittedClaim(respondToClaim)
            .responseClaimAdmitPartEmployer(responseClaimAdmitPartEmployer)
            //case progression
            .hearingFeePaymentDetails(hearingFeePaymentDetails)
            .hearingDuration(hearingDuration)
            .trialReadyApplicant(trialReadyApplicant)
            .trialReadyRespondent1(trialReadyRespondent1)
            .trialReadyRespondent2(trialReadyRespondent2)
            //workaround fields
            .respondent1Copy(respondent1Copy)
            .respondent2Copy(respondent2Copy)
            .respondToClaimAdmitPartUnemployedLRspec(respondToClaimAdmitPartUnemployedLRspec)
            .respondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec)
            .respondent1PartnerAndDependent(respondent1PartnerAndDependent)
            .respondent2PartnerAndDependent(respondent2PartnerAndDependent)
            .respondent1RepaymentPlan(respondent1RepaymentPlan)
            .respondent2RepaymentPlan(respondent2RepaymentPlan)
            .applicantsProceedIntention(applicantsProceedIntention)
            .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(
                applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2)
            .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(
                applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2)
            .claimant1ClaimResponseTypeForSpec(claimant1ClaimResponseTypeForSpec)
            .claimant2ClaimResponseTypeForSpec(claimant2ClaimResponseTypeForSpec)
            .respondent1ClaimResponseTypeForSpec(respondent1ClaimResponseTypeForSpec)
            .respondent2ClaimResponseTypeForSpec(respondent2ClaimResponseTypeForSpec)
            .responseClaimTrack(responseClaimTrack)
            .applicant1ClaimMediationSpecRequired(applicant1ClaimMediationSpecRequired)
            .applicantMPClaimMediationSpecRequired(applicantMPClaimMediationSpecRequired)
            .responseClaimMediationSpecRequired(respondent1MediationRequired)
            .responseClaimMediationSpec2Required(respondent1MediationRequired)
            .mediation(mediation)
            .respondentSolicitor2Reference(respondentSolicitor2Reference)
            .claimant1ClaimResponseTypeForSpec(claimant1ClaimResponseTypeForSpec)
            .claimant2ClaimResponseTypeForSpec(claimant2ClaimResponseTypeForSpec)
            .respondent1ClaimResponseTypeForSpec(respondent1ClaimResponseTypeForSpec)
            .respondent2ClaimResponseTypeForSpec(respondent2ClaimResponseTypeForSpec)
            .specAoSApplicantCorrespondenceAddressRequired(specAoSApplicantCorrespondenceAddressRequired)
            .specAoSRespondentCorrespondenceAddressRequired(specAoSRespondentCorrespondenceAddressRequired)
            .specAoSApplicantCorrespondenceAddressdetails(specAoSApplicantCorrespondenceAddressDetails)
            .specAoSRespondentCorrespondenceAddressdetails(specAoSRespondentCorrespondenceAddressDetails)
            .specAoSRespondent2HomeAddressRequired(specAoSRespondent2HomeAddressRequired)
            .specAoSRespondent2HomeAddressDetails(specAoSRespondent2HomeAddressDetails)
            .respondent1DQWitnessesRequiredSpec(respondent1DQWitnessesRequiredSpec)
            .respondent1DQWitnessesDetailsSpec(respondent1DQWitnessesDetailsSpec)
            .applicant1ProceedWithClaimSpec2v1(applicant1ProceedWithClaimSpec2v1)
            .respondent1OrganisationIDCopy(respondent1OrganisationIDCopy)
            .respondent2OrganisationIDCopy(respondent2OrganisationIDCopy)
            .specRespondent1Represented(specRespondent1Represented)
            .specRespondent2Represented(specRespondent2Represented)
            .defendantSingleResponseToBothClaimants(defendantSingleResponseToBothClaimants)
            .breathing(breathing)
            .caseManagementOrderSelection(caseManagementOrderSelection)
            .respondent1PinToPostLRspec(respondent1PinToPostLRspec)
            .trialHearingMethodDJ(trialHearingMethodDJ)
            .hearingMethodValuesDisposalHearingDJ(hearingMethodValuesDisposalHearingDJ)
            .hearingMethodValuesTrialHearingDJ(hearingMethodValuesTrialHearingDJ)
            .disposalHearingMethodDJ(disposalHearingMethodDJ)
            .trialHearingMethodInPersonDJ(trialHearingMethodInPersonDJ)
            .disposalHearingBundleDJ(disposalHearingBundleDJ)
            .disposalHearingFinalDisposalHearingDJ(disposalHearingFinalDisposalHearingDJ)
            .trialHearingTrialDJ(trialHearingTrialDJ)
            .disposalHearingJudgesRecitalDJ(disposalHearingJudgesRecitalDJ)
            .trialHearingJudgesRecitalDJ(trialHearingJudgesRecitalDJ)
            .claimIssuedPBADetails(srPbaDetails)
            .changeOfRepresentation(changeOfRepresentation)
            .changeOrganisationRequestField(changeOrganisationRequest)
            .unassignedCaseListDisplayOrganisationReferences(unassignedCaseListDisplayOrganisationReferences)
            .caseListDisplayDefendantSolicitorReferences(caseListDisplayDefendantSolicitorReferences)
            .caseManagementLocation(caseManagementLocation)
            .disposalHearingOrderMadeWithoutHearingDJ(disposalHearingOrderMadeWithoutHearingDJ)
            .hearingDate(hearingDate)
            .cosNotifyClaimDefendant1(cosNotifyClaimDefendant1)
            .cosNotifyClaimDefendant2(cosNotifyClaimDefendant2)
            .defendant1LIPAtClaimIssued(defendant1LIPAtClaimIssued)
            .defendant2LIPAtClaimIssued(defendant2LIPAtClaimIssued)
            //Unsuitable for SDO
            .reasonNotSuitableSDO(reasonNotSuitableSDO)
            .fastTrackHearingTime(fastTrackHearingTime)
            .fastTrackOrderWithoutJudgement(fastTrackOrderWithoutJudgement)
            .fastTrackTrialDateToToggle(fastTrackTrialDateToToggle)
            .disposalHearingHearingTime(disposalHearingHearingTime)
            .disposalOrderWithoutHearing(disposalOrderWithoutHearing)
            .disposalHearingOrderMadeWithoutHearingDJ(disposalHearingOrderMadeWithoutHearingDJ)
            .disposalHearingFinalDisposalHearingTimeDJ(disposalHearingFinalDisposalHearingTimeDJ)
            .trialHearingTimeDJ(trialHearingTimeDJ)
            .trialOrderMadeWithoutHearingDJ(trialOrderMadeWithoutHearingDJ)
                //Certificate of Service
                .cosNotifyClaimDetails1(cosNotifyClaimDetails1)
                .cosNotifyClaimDetails2(cosNotifyClaimDetails2)
            .ccjPaymentDetails(ccjPaymentDetails)
            .totalInterest(totalInterest)
            .applicant1AcceptAdmitAmountPaidSpec(applicant1AcceptAdmitAmountPaidSpec)
            .applicant1AcceptPartAdmitPaymentPlanSpec(applicant1AcceptPartAdmitPaymentPlanSpec)
            .respondToAdmittedClaimOwingAmountPounds(respondToAdmittedClaimOwingAmountPounds)
            .hearingMethodValuesDisposalHearing(hearingMethodValuesDisposalHearing)
            .hearingMethodValuesFastTrack(hearingMethodValuesFastTrack)
            .hearingMethodValuesSmallClaims(hearingMethodValuesSmallClaims)
            .applicantExperts(applicantExperts)
            .applicantWitnesses(applicantWitnesses)
            .respondent1Experts(respondent1Experts)
            .respondent1Witnesses(respondent1Witnesses)
            .respondent2Experts(respondent2Experts)
            .respondent2Witnesses(respondent2Witnesses)
            .respondentSolicitor1ServiceAddressRequired(respondentSolicitor1ServiceAddressRequired)
            .respondentSolicitor2ServiceAddressRequired(respondentSolicitor2ServiceAddressRequired)
            .applicant1PartAdmitIntentionToSettleClaimSpec(applicant1PartAdmitIntentionToSettleClaimSpec)
            .applicant1PartAdmitConfirmAmountPaidSpec(applicant1PartAdmitConfirmAmountPaidSpec)
            .applicant1Represented(applicant1Represented)
            .caseDataLiP(caseDataLiP)
            .claimant2ResponseFlag(claimant2ResponseFlag)
            .specClaimResponseTimelineList(specClaimResponseTimelineList)
            .specClaimResponseTimelineList2(specClaimResponseTimelineList2)
            .defenceAdmitPartEmploymentTypeRequired(defenceAdmitPartEmploymentTypeRequired)
            .defenceAdmitPartPaymentTimeRouteRequired(defenceAdmitPartPaymentTimeRouteRequired)
            .specDefenceFullAdmitted2Required(specDefenceFullAdmitted2Required)
            .showResponseOneVOneFlag(showResponseOneVOneFlag)
            .hearingSupportRequirementsDJ(hearingSupportRequirementsDJ)
            .hearingReferenceNumber(hearingReference)
            .listingOrRelisting(listingOrRelisting)
            .claimantUserDetails(claimantUserDetails)
            .updateDetailsForm(updateDetailsForm)
            .defaultJudgmentDocuments(defaultJudgmentDocuments)
            .smallClaimsWitnessStatement(smallClaimsWitnessStatement)
            .fastTrackWitnessOfFact(fastTrackWitnessOfFact)
            .trialHearingWitnessOfFactDJ(trialHearingWitnessOfFactDJ)
            //Transfer Online Case
            .notSuitableSdoOptions(notSuitableSdoOptions)
            .tocTransferCaseReason(tocTransferCaseReason)
            .drawDirectionsOrderRequired(drawDirectionsOrderRequired)
            .transferCourtLocationList(transferCourtLocationList)
            .reasonForTransfer(reasonForTransfer)
            .applicant1LRIndividuals(applicant1LRIndividuals)
            .respondent1LRIndividuals(respondent1LRIndividuals)
            .respondent2LRIndividuals(respondent2LRIndividuals)
            .applicant1OrgIndividuals(applicant1OrgIndividuals)
            .applicant2OrgIndividuals(applicant2OrgIndividuals)
            .respondent1OrgIndividuals(respondent1OrgIndividuals)
            .respondent2OrgIndividuals(respondent2OrgIndividuals)
            .flightDelayDetails(flightDelayDetails)
            .responseClaimExpertSpecRequired(responseClaimExpertSpecRequired)
            .responseClaimExpertSpecRequired2(responseClaimExpertSpecRequired2)
            .applicant1ClaimExpertSpecRequired(applicant1ClaimExpertSpecRequired)
            .applicantMPClaimExpertSpecRequired(applicantMPClaimExpertSpecRequired)
            .isFlightDelayClaim(isFlightDelayClaim)
            .reasonForReconsideration(reasonForReconsideration)
            .build();
    }
}
