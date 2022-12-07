package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.PersonalInjuryType;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialHearingTimeEstimateDJ;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.CloseClaim;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.LengthOfUnemploymentComplexTypeLRspec;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.PartnerAndDependentsLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnemployedComplexTypeLRspec;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocation;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingFinalDisposalHearingDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingJudgesRecitalDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfNonElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
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
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalOrderWithoutHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackOrderWithoutJudgement;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.PersonalInjuryType.ROAD_ACCIDENT;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.HearingLength.ONE_DAY;
import static uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate.FIFTEEN_MINUTES;
import static uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate.FIFTEEN_MINUTES;
import static uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentOrderFormGenerator.DISPOSAL_HEARING;
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

    // Create Claim
    protected Long ccdCaseReference;
    protected SolicitorReferences solicitorReferences;
    protected String respondentSolicitor2Reference;
    protected CourtLocation courtLocation;
    protected Party applicant1;
    protected Party applicant2;
    protected YesOrNo applicant1LitigationFriendRequired;
    protected Party respondent1;
    protected Party respondent2;
    protected YesOrNo respondent1Represented;
    protected YesOrNo respondent2Represented;
    protected String respondentSolicitor1EmailAddress;
    protected String respondentSolicitor2EmailAddress;
    protected ClaimValue claimValue;
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

    protected CloseClaim withdrawClaim;
    protected CloseClaim discontinueClaim;
    protected YesOrNo respondent1OrgRegistered;
    protected YesOrNo respondent2OrgRegistered;
    protected OrganisationPolicy applicant1OrganisationPolicy;
    protected OrganisationPolicy respondent1OrganisationPolicy;
    protected OrganisationPolicy respondent2OrganisationPolicy;
    protected YesOrNo addApplicant2;
    protected SuperClaimType superClaimType;
    protected YesOrNo addRespondent2;
    protected CaseCategory caseAccessCategory;

    protected YesOrNo specRespondent1Represented;
    protected YesOrNo specRespondent2Represented;

    protected YesOrNo respondent2SameLegalRepresentative;
    protected LitigationFriend respondent1LitigationFriend;
    protected LitigationFriend respondent2LitigationFriend;
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

    protected SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails;
    protected SolicitorOrganisationDetails respondentSolicitor2OrganisationDetails;
    protected Address applicantSolicitor1ServiceAddress;
    protected Address respondentSolicitor1ServiceAddress;
    protected Address respondentSolicitor2ServiceAddress;
    protected YesOrNo isRespondent1;
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
    private YesOrNo specAoSApplicantCorrespondenceAddressRequired;
    private Address specAoSApplicantCorrespondenceAddressDetails;
    private YesOrNo specAoSRespondent2HomeAddressRequired;
    private Address specAoSRespondent2HomeAddressDetails;
    private YesOrNo respondent1DQWitnessesRequiredSpec;
    private List<Element<Witness>> respondent1DQWitnessesDetailsSpec;

    private String respondent1OrganisationIDCopy;
    private String respondent2OrganisationIDCopy;
    private String caseManagementOrderSelection;
    private LocalDateTime addLegalRepDeadline;
    private DefendantPinToPostLRspec respondent1PinToPostLRspec;
    private DisposalHearingMethodDJ trialHearingMethodDJ;
    private DisposalHearingMethodDJ disposalHearingMethodDJ;
    private DynamicList trialHearingMethodInPersonDJ;
    private DisposalHearingBundleDJ disposalHearingBundleDJ;
    private DisposalHearingFinalDisposalHearingDJ disposalHearingFinalDisposalHearingDJ;
    private TrialHearingTrial trialHearingTrialDJ;
    private DisposalHearingJudgesRecitalDJ disposalHearingJudgesRecitalDJ;
    private TrialHearingJudgesRecital trialHearingJudgesRecitalDJ;
    private LocalDate hearingDueDate;

    private CaseLocation caseManagementLocation;
    private DisposalHearingOrderMadeWithoutHearingDJ disposalHearingOrderMadeWithoutHearingDJ;
    private DisposalHearingFinalDisposalHearingTimeDJ disposalHearingFinalDisposalHearingTimeDJ;

    private YesOrNo generalAppVaryJudgementType;
    private Document generalAppN245FormUpload;
    private GAApplicationType generalAppType;
    private GAHearingDateGAspec generalAppHearingDate;

    private List<Element<ChangeOfRepresentation>> changeOfRepresentation;
    private ChangeOrganisationRequest changeOrganisationRequest;

    private String unassignedCaseListDisplayOrganisationReferences;
    private String caseListDisplayDefendantSolicitorReferences;

    private FastTrackHearingTime fastTrackHearingTime;
    private FastTrackOrderWithoutJudgement fastTrackOrderWithoutJudgement;

    private DisposalOrderWithoutHearing disposalOrderWithoutHearing;
    private DisposalHearingHearingTime disposalHearingHearingTime;

    private TrialHearingTimeDJ trialHearingTimeDJ;
    private TrialOrderMadeWithoutHearingDJ trialOrderMadeWithoutHearingDJ;

    public CaseDataBuilder sameRateInterestSelection(SameRateInterestSelection sameRateInterestSelection) {
        this.sameRateInterestSelection = sameRateInterestSelection;
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

    public CaseDataBuilder respondent1DQ(Respondent1DQ respondent1DQ) {
        this.respondent1DQ = respondent1DQ;
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
                                             .caseLocation(CaseLocation.builder()
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
                                             .caseLocation(CaseLocation.builder()
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
            .applicant1DQHearingSupport(HearingSupport.builder().requirements(List.of()).build())
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
                                            .caseLocation(CaseLocation.builder()
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
                                            .caseLocation(CaseLocation.builder()
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
            .applicant2DQHearingSupport(HearingSupport.builder().requirements(List.of()).build())
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

    public CaseDataBuilder issueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    public CaseDataBuilder takenOfflineDate(LocalDateTime takenOfflineDate) {
        this.takenOfflineDate = takenOfflineDate;
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
                return atStatePaymentFailed();
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
        respondent2 = PartyBuilder.builder().individual().build();
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
        respondent2 = PartyBuilder.builder().individual().build();
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
        respondent2 = PartyBuilder.builder().individual().build();
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
        respondent2 = PartyBuilder.builder().individual().build();
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
        respondent2 = PartyBuilder.builder().individual().build();
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
        respondent2 = PartyBuilder.builder().individual().build();
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

    public CaseDataBuilder atStateProceedsOfflineUnrepresentedDefendant1UnregisteredDefendant2() {
        atStatePendingClaimIssuedUnrepresentedDefendant();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
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
        respondent2 = PartyBuilder.builder().individual().build();
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

    public CaseDataBuilder courtLocation_old() {
        this.courtLocation = CourtLocation.builder()
            .applicantPreferredCourt("127").build();
        return this;
    }

    public CaseDataBuilder courtLocation() {
        this.courtLocation = CourtLocation.builder()
            .applicantPreferredCourt("127")
            .caseLocation(CaseLocation.builder()
                              .region("regionId1")
                              .baseLocation("epimmsId1")
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
            .applicantPreferredCourtLocationList(
                DynamicList.builder().value(DynamicListElement.builder().label("sitename").build()).build())
            .build();
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
        applicant1 = PartyBuilder.builder().individual().build();
        respondent1 = PartyBuilder.builder().soleTrader().build();
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
        respondent1OrganisationIDCopy = respondent1OrganisationPolicy.getOrganisation().getOrganisationID();
        respondent2OrganisationIDCopy = respondent2OrganisationPolicy.getOrganisation().getOrganisationID();
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

    public CaseDataBuilder atStateClaimSubmittedOneRespondentRepresentative() {
        atStateClaimSubmitted();
        addRespondent2 = NO;
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
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent1Represented = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
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

    public CaseDataBuilder atStateClaimIssuedTrialSDOInPersonHearing() {
        trialHearingMethodDJ = DisposalHearingMethodDJ.disposalHearingMethodInPerson;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialLocationInPerson() {
        trialHearingMethodInPersonDJ = DynamicList.builder().value(
            DynamicListElement.builder().label("Court 1").build()).build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedCaseManagementLocationInPerson() {
        caseManagementLocation = CaseLocation.builder().baseLocation("0123").region("0321").build();
        return this;
    }

    public CaseDataBuilder atStateSdoTrialDj() {
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
            .date1(LocalDate.parse("2022-01-01"))
            .date2(LocalDate.parse("2022-01-02"))
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
            .type(DisposalHearingBundleType.DOCUMENTS)
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalHearingInPerson() {
        disposalHearingBundleDJ = DisposalHearingBundleDJ.builder()
            .input("The claimant must lodge at court at least 7 "
                       + "days before the disposal")
            .type(DisposalHearingBundleType.DOCUMENTS)
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

    public CaseDataBuilder atStatePaymentFailed() {
        atStateClaimSubmitted();

        claimIssuedPaymentDetails = PaymentDetails.builder()
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

    public CaseDataBuilder atStatePaymentSuccessfulWithCopyOrganisationOnly() {
        atStatePaymentSuccessful();
        respondent1OrganisationIDCopy = respondent1OrganisationPolicy.getOrganisation().getOrganisationID();
        respondent1OrganisationPolicy = respondent1OrganisationPolicy.toBuilder()
            .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().build()).build();
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
        respondent2 = PartyBuilder.builder().individual().build();
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
        respondent1OrganisationIDCopy = "QWERTY R";
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
                                                          String newOrgID, String oldOrgId) {
        String caseRole = isApplicant ? CaseRole.APPLICANTSOLICITORONE.getFormattedName() :
            isRespondent2Replaced ? CaseRole.RESPONDENTSOLICITORTWO.getFormattedName() :
                CaseRole.RESPONDENTSOLICITORONE.getFormattedName();
        changeOrganisationRequest = ChangeOrganisationRequest.builder()
            .requestTimestamp(LocalDateTime.now())
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
                                                  String newOrgID, String oldOrgId) {
        String caseRole = isApplicant ? CaseRole.APPLICANTSOLICITORONE.getFormattedName() :
            isRespondent2Replaced ? CaseRole.RESPONDENTSOLICITORTWO.getFormattedName() :
                CaseRole.RESPONDENTSOLICITORONE.getFormattedName();
        ChangeOfRepresentation newChange = ChangeOfRepresentation.builder()
            .caseRole(caseRole)
            .organisationToAddID(newOrgID)
            .organisationToRemoveID(oldOrgId)
            .timestamp(LocalDateTime.now())
            .build();
        changeOfRepresentation = wrapElements(newChange);
        return this;
    }

    public CaseDataBuilder updateOrgPolicyAfterNoC(boolean isApplicant, boolean isRespondent2) {
        if (isApplicant) {
            applicant1OrganisationPolicy = OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationID("1234").build())
                .orgPolicyCaseAssignedRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName()).build();
        } else {
            if (isRespondent2) {
                respondent2OrganisationPolicy = OrganisationPolicy.builder()
                    .organisation(Organisation.builder().organisationID("1234").build())
                    .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName()).build();
            } else {
                respondent1OrganisationPolicy = OrganisationPolicy.builder()
                    .organisation(Organisation.builder().organisationID("1234").build())
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
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2SameLegalRepresentative = NO;
        return this;
    }

    public CaseDataBuilder atState1v2DifferentSolicitorClaimDetailsRespondent2NotifiedTimeExtension() {
        atStateClaimDetailsNotified();
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2SameLegalRepresentative = NO;
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        respondent2TimeExtensionDate = claimDetailsNotificationDate.plusDays(1);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        return this;
    }

    public CaseDataBuilder atState1v2SameSolicitorClaimDetailsRespondentNotifiedTimeExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        respondent2 = PartyBuilder.builder().individual().build();
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

    public CaseDataBuilder atStateRespondentFullDefence() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant1-defence.pdf").build())
            .build();
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceRespondent2() {
        atStateRespondent2RespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent2DQ();
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
        respondent1ResponseDate = LocalDateTime.now().minusDays(1);
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
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
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
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        respondent2Responds(respondentResponseType);
        respondent2ResponseDate = LocalDateTime.now().plusDays(2);
        return this;
    }

    public CaseDataBuilder atStateBothRespondentsSameResponse1v2SameSolicitor(RespondentResponseType responseType) {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = responseType;
        respondent2ClaimResponseType = responseType;
        respondentResponseIsSame(YES);
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        respondent2ResponseDate = respondent1ResponseDate;
        respondent2ClaimResponseIntentionType = respondent1ClaimResponseIntentionType;
        return this;
    }

    public CaseDataBuilder atState1v2SameSolicitorDivergentResponse(RespondentResponseType respondent1Response,
                                                       RespondentResponseType respondent2Response) {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = respondent1Response;
        respondent2Responds(respondent2Response);
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        respondentResponseIsSame(NO);
        if (superClaimType != SPEC_CLAIM) {
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
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        respondent2Responds(respondent2Response);
        respondent2ResponseDate = LocalDateTime.now().plusDays(2);
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

    public CaseDataBuilder atStateRespondentCounterClaimAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
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

    public CaseDataBuilder atStateApplicantRespondToDefenceAndProceed() {
        return atStateApplicantRespondToDefenceAndProceed(ONE_V_ONE);
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario mpScenario) {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1DefenceResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("claimant-response.pdf").build())
            .build();
        applicant1DQ();
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
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

    public CaseDataBuilder atStateNotificationAcknowledgedRespondent2TimeExtension(int numberOfHoursAfterCurrentDate) {
        atStateNotificationAcknowledged();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent2 = PartyBuilder.builder().individual().build();
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
        hearingDueDate = LocalDate.now().minusDays(1);
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

    public CaseDataBuilder atStateTakenOfflineSDONotDrawn(MultiPartyScenario mpScenario) {

        atStateApplicantRespondToDefenceAndProceed(mpScenario);
        if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2();
        } else if (mpScenario == TWO_V_ONE) {
            atStateBothApplicantsRespondToDefenceAndProceed_2v1();
        }

        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;

        reasonNotSuitableSDO = ReasonNotSuitableSDO.builder()
                                                   .input("unforeseen complexities")
            .build();
        unsuitableSDODate = applicant1ResponseDate.plusDays(1);

        return this;
    }

    public CaseDataBuilder atStateTakenOfflineAfterSDO(MultiPartyScenario mpScenario) {

        atStateApplicantRespondToDefenceAndProceed(mpScenario);
        if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2();
        } else if (mpScenario == TWO_V_ONE) {
            atStateBothApplicantsRespondToDefenceAndProceed_2v1();
        }

        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;

        takenOfflineDate = applicant1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateApplicantProceedAllMediation(MultiPartyScenario mpScenario) {

        applicant1ClaimMediationSpecRequired = new SmallClaimMedicalLRspec(YES);
        applicantMPClaimMediationSpecRequired = new SmallClaimMedicalLRspec(YES);
        respondent1MediationRequired = YES;
        respondent2MediationRequired = YES;
        responseClaimTrack = SMALL_CLAIM.name();
        superClaimType = SPEC_CLAIM;

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

    public CaseDataBuilder addRespondent1LitigationFriend() {
        this.respondent1LitigationFriend = LitigationFriend.builder()
            .fullName("Mr Litigation Friend")
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
            .fullName("Mr Litigation Friend")
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
        this.respondent2 = PartyBuilder.builder().individual().build();
        this.respondent2SameLegalRepresentative = NO;
        this.respondentSolicitor2Reference = "01234";
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantSolicitorsForSdoMP() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build();
        this.respondent2SameLegalRepresentative = NO;
        this.respondentSolicitor2Reference = "01234";
        respondent2ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilder multiPartyClaimOneDefendantSolicitor() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build();
        this.respondent2SameLegalRepresentative = YES;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantSolicitorsSpec() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build();
        this.respondent2SameLegalRepresentative = NO;
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
        this.applicant2 = PartyBuilder.builder().individual("Jason").build();
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

    public CaseDataBuilder setSuperClaimTypeToSpecClaim() {
        this.superClaimType = SPEC_CLAIM;
        return this;
    }

    public CaseDataBuilder setSuperClaimTypeToUnspecClaim() {
        this.superClaimType = UNSPEC_CLAIM;
        return this;
    }

    public CaseDataBuilder respondent2Responds(RespondentResponseType responseType) {
        this.respondent2ClaimResponseType = responseType;
        this.respondent2ResponseDate = LocalDateTime.now().plusDays(1);
        return this;
    }

    public CaseDataBuilder respondent2Responds1v2SameSol(RespondentResponseType responseType) {
        this.respondent2ClaimResponseType = responseType;
        this.respondent2ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
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

    public CaseDataBuilder atSpecAoSRespondent2HomeAddressRequired(YesOrNo specAoSRespondent2HomeAddressRequired) {
        this.specAoSRespondent2HomeAddressRequired = specAoSRespondent2HomeAddressRequired;
        return this;
    }

    public CaseDataBuilder atSpecAoSRespondent2HomeAddressDetails(Address specAoSRespondent2HomeAddressDetails) {
        this.specAoSRespondent2HomeAddressDetails = specAoSRespondent2HomeAddressDetails;
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

    public CaseDataBuilder removeSolicitorReferences() {
        this.solicitorReferences = null;
        this.respondentSolicitor2Reference = null;
        return this;
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

    public static CaseDataBuilder builder() {
        return new CaseDataBuilder();
    }

    public CaseData build() {
        return CaseData.builder()
            // Create Claim
            .legacyCaseReference(legacyCaseReference)
            .allocatedTrack(allocatedTrack)
            .generalAppType(generalAppType)
            .generalAppVaryJudgementType(generalAppVaryJudgementType)
            .generalAppN245FormUpload(generalAppN245FormUpload)
            .generalAppHearingDate(generalAppHearingDate)
            .solicitorReferences(solicitorReferences)
            .courtLocation(courtLocation)
            .claimValue(claimValue)
            .claimType(claimType)
            .claimTypeOther(claimTypeOther)
            .personalInjuryType(personalInjuryType)
            .personalInjuryTypeOther(personalInjuryTypeOther)
            .applicantSolicitor1PbaAccounts(applicantSolicitor1PbaAccounts)
            .claimFee(claimFee)
            .applicant1(applicant1)
            .applicant2(applicant2)
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
            .defendantSolicitorNotifyClaimOptions(defendantSolicitorNotifyClaimOptions)
            .defendantSolicitorNotifyClaimDetailsOptions(defendantSolicitorNotifyClaimDetailsOptions)
            .selectLitigationFriend(selectLitigationFriend)
            .caseNotes(caseNotes)
            .hearingDueDate(hearingDueDate)
            .hearingDate(hearingDate)
            //ui field
            .uiStatementOfTruth(uiStatementOfTruth)
            .superClaimType(superClaimType == null ? UNSPEC_CLAIM : superClaimType)
            .caseBundles(caseBundles)
            .respondToClaim(respondToClaim)
            //spec route
            .respondent1ClaimResponseTypeForSpec(respondent1ClaimResponseTypeForSpec)
            .respondToAdmittedClaim(respondToClaim)
            .responseClaimAdmitPartEmployer(responseClaimAdmitPartEmployer)
            //case progression
            .hearingFeePaymentDetails(hearingFeePaymentDetails)
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
            .respondentSolicitor2Reference(respondentSolicitor2Reference)
            .claimant1ClaimResponseTypeForSpec(claimant1ClaimResponseTypeForSpec)
            .claimant2ClaimResponseTypeForSpec(claimant2ClaimResponseTypeForSpec)
            .respondent1ClaimResponseTypeForSpec(respondent1ClaimResponseTypeForSpec)
            .respondent2ClaimResponseTypeForSpec(respondent2ClaimResponseTypeForSpec)
            .specAoSApplicantCorrespondenceAddressRequired(specAoSApplicantCorrespondenceAddressRequired)
            .specAoSApplicantCorrespondenceAddressdetails(specAoSApplicantCorrespondenceAddressDetails)
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
            .disposalHearingMethodDJ(disposalHearingMethodDJ)
            .trialHearingMethodInPersonDJ(trialHearingMethodInPersonDJ)
            .disposalHearingBundleDJ(disposalHearingBundleDJ)
            .disposalHearingFinalDisposalHearingDJ(disposalHearingFinalDisposalHearingDJ)
            .trialHearingTrialDJ(trialHearingTrialDJ)
            .disposalHearingJudgesRecitalDJ(disposalHearingJudgesRecitalDJ)
            .trialHearingJudgesRecitalDJ(trialHearingJudgesRecitalDJ)
            .changeOfRepresentation(changeOfRepresentation)
            .changeOrganisationRequestField(changeOrganisationRequest)
            .unassignedCaseListDisplayOrganisationReferences(unassignedCaseListDisplayOrganisationReferences)
            .caseListDisplayDefendantSolicitorReferences(caseListDisplayDefendantSolicitorReferences)
            .caseManagementLocation(caseManagementLocation)
            .disposalHearingOrderMadeWithoutHearingDJ(disposalHearingOrderMadeWithoutHearingDJ)
            //Unsuitable for SDO
            .reasonNotSuitableSDO(reasonNotSuitableSDO)
            .fastTrackHearingTime(fastTrackHearingTime)
            .fastTrackOrderWithoutJudgement(fastTrackOrderWithoutJudgement)
            .disposalHearingHearingTime(disposalHearingHearingTime)
            .disposalOrderWithoutHearing(disposalOrderWithoutHearing)
            .disposalHearingOrderMadeWithoutHearingDJ(disposalHearingOrderMadeWithoutHearingDJ)
            .disposalHearingFinalDisposalHearingTimeDJ(disposalHearingFinalDisposalHearingTimeDJ)
            .trialHearingTimeDJ(trialHearingTimeDJ)
            .trialOrderMadeWithoutHearingDJ(trialOrderMadeWithoutHearingDJ)
            .build();
    }

}
