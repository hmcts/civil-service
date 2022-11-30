package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.*;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialHearingTimeEstimateDJ;
import uk.gov.hmcts.reform.civil.model.*;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.*;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.dq.*;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimUntilType;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
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
import static uk.gov.hmcts.reform.civil.enums.CaseState.*;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.*;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.PersonalInjuryType.ROAD_ACCIDENT;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.HearingLength.ONE_DAY;
import static uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentOrderFormGenerator.DISPOSAL_HEARING;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class CaseDataBuilderSpec {

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

    private YesOrNo generalAppVaryJudgementType;
    private Document generalAppN245FormUpload;
    private GAApplicationType generalAppType;
    private GAHearingDateGAspec generalAppHearingDate;

    private List<Element<ChangeOfRepresentation>> changeOfRepresentation;
    private ChangeOrganisationRequest changeOrganisationRequest;

    private String unassignedCaseListDisplayOrganisationReferences;
    private String caseListDisplayDefendantSolicitorReferences;

    private TrialHearingTimeDJ trialHearingTimeDJ;
    private TrialOrderMadeWithoutHearingDJ trialOrderMadeWithoutHearingDJ;

    public CaseDataBuilderSpec sameRateInterestSelection(SameRateInterestSelection sameRateInterestSelection) {
        this.sameRateInterestSelection = sameRateInterestSelection;
        return this;
    }

    public CaseDataBuilderSpec generalAppVaryJudgementType(YesOrNo generalAppVaryJudgementType) {
        this.generalAppVaryJudgementType = generalAppVaryJudgementType;
        return this;
    }

    public CaseDataBuilderSpec generalAppType(GAApplicationType generalAppType) {
        this.generalAppType = generalAppType;
        return this;
    }

    public CaseDataBuilderSpec generalAppHearingDate(GAHearingDateGAspec generalAppHearingDate) {
        this.generalAppHearingDate = generalAppHearingDate;
        return this;
    }

    public CaseDataBuilderSpec generalAppN245FormUpload(Document generalAppN245FormUpload) {
        this.generalAppN245FormUpload = generalAppN245FormUpload;
        return this;
    }

    public CaseDataBuilderSpec breakDownInterestTotal(BigDecimal breakDownInterestTotal) {
        this.breakDownInterestTotal = breakDownInterestTotal;
        return this;
    }

    public CaseDataBuilderSpec interestFromSpecificDate(LocalDate interestFromSpecificDate) {
        this.interestFromSpecificDate = interestFromSpecificDate;
        return this;
    }

    public CaseDataBuilderSpec totalClaimAmount(BigDecimal totalClaimAmount) {
        this.totalClaimAmount = totalClaimAmount;
        return this;
    }

    public CaseDataBuilderSpec interestClaimOptions(InterestClaimOptions interestClaimOptions) {
        this.interestClaimOptions = interestClaimOptions;
        return this;
    }

    public CaseDataBuilderSpec interestClaimFrom(InterestClaimFromType interestClaimFrom) {
        this.interestClaimFrom = interestClaimFrom;
        return this;
    }

    public CaseDataBuilderSpec interestClaimUntil(InterestClaimUntilType interestClaimUntil) {
        this.interestClaimUntil = interestClaimUntil;
        return this;
    }

    public CaseDataBuilderSpec claimInterest(YesOrNo claimInterest) {
        this.claimInterest = claimInterest;
        return this;
    }

    //workaround fields
    protected Party respondent1Copy;
    protected Party respondent2Copy;

    public CaseDataBuilderSpec respondent1ResponseDeadline(LocalDateTime deadline) {
        this.respondent1ResponseDeadline = deadline;
        return this;
    }

    public CaseDataBuilderSpec respondent2ResponseDeadline(LocalDateTime deadline) {
        this.respondent2ResponseDeadline = deadline;
        return this;
    }

    public CaseDataBuilderSpec respondent1AcknowledgeNotificationDate(LocalDateTime dateTime) {
        this.respondent1AcknowledgeNotificationDate = dateTime;
        return this;
    }

    public CaseDataBuilderSpec respondent2AcknowledgeNotificationDate(LocalDateTime dateTime) {
        this.respondent2AcknowledgeNotificationDate = dateTime;
        return this;
    }

    public CaseDataBuilderSpec applicantSolicitor1ServiceAddress(Address applicantSolicitor1ServiceAddress) {
        this.applicantSolicitor1ServiceAddress = applicantSolicitor1ServiceAddress;
        return this;
    }

    public CaseDataBuilderSpec respondentSolicitor1ServiceAddress(Address respondentSolicitor1ServiceAddress) {
        this.respondentSolicitor1ServiceAddress = respondentSolicitor1ServiceAddress;
        return this;
    }

    public CaseDataBuilderSpec respondentSolicitor2ServiceAddress(Address respondentSolicitor2ServiceAddress) {
        this.respondentSolicitor2ServiceAddress = respondentSolicitor2ServiceAddress;
        return this;
    }

    public CaseDataBuilderSpec respondentSolicitor1EmailAddress(String respondentSolicitor1EmailAddress) {
        this.respondentSolicitor1EmailAddress = respondentSolicitor1EmailAddress;
        return this;
    }

    public CaseDataBuilderSpec respondentSolicitor2EmailAddress(String respondentSolicitor2EmailAddress) {
        this.respondentSolicitor2EmailAddress = respondentSolicitor2EmailAddress;
        return this;
    }

    public CaseDataBuilderSpec isRespondent1(YesOrNo isRespondent1) {
        this.isRespondent1 = isRespondent1;
        return this;
    }

    public CaseDataBuilderSpec respondentSolicitor1AgreedDeadlineExtension(LocalDate extensionDate) {
        this.respondentSolicitor1AgreedDeadlineExtension = extensionDate;
        return this;
    }

    public CaseDataBuilderSpec respondentSolicitor2AgreedDeadlineExtension(LocalDate extensionDate) {
        this.respondentSolicitor2AgreedDeadlineExtension = extensionDate;
        return this;
    }

    public CaseDataBuilderSpec respondent1TimeExtensionDate(LocalDateTime extensionDate) {
        this.respondent1TimeExtensionDate = extensionDate;
        return this;
    }

    public CaseDataBuilderSpec respondent2TimeExtensionDate(LocalDateTime extensionDate) {
        this.respondent2TimeExtensionDate = extensionDate;
        return this;
    }

    public CaseDataBuilderSpec respondent2(Party party) {
        this.respondent2 = party;
        return this;
    }

    public CaseDataBuilderSpec caseNotes(CaseNote caseNote) {
        this.caseNotes = wrapElements(caseNote);
        return this;
    }

    public CaseDataBuilderSpec respondent1OrganisationIDCopy(String id) {
        this.respondent1OrganisationIDCopy = id;
        return this;
    }

    public CaseDataBuilderSpec respondent2OrganisationIDCopy(String id) {
        this.respondent2OrganisationIDCopy = id;
        return this;
    }

    public CaseDataBuilderSpec respondent1DQ() {
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

    public CaseDataBuilderSpec respondent1DQ(Respondent1DQ respondent1DQ) {
        this.respondent1DQ = respondent1DQ;
        return this;
    }

    public CaseDataBuilderSpec respondent1DQWithLocation() {
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

    public CaseDataBuilderSpec respondent2DQ() {
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

    public CaseDataBuilderSpec respondent2DQ(Respondent2DQ respondent2DQ) {
        this.respondent2DQ = respondent2DQ;
        return this;
    }

    public CaseDataBuilderSpec applicant1DQ() {
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

    public CaseDataBuilderSpec applicant1DQ(Applicant1DQ applicant1DQ) {
        this.applicant1DQ = applicant1DQ;
        return this;
    }

    public CaseDataBuilderSpec applicant1DQWithLocation() {
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

    public CaseDataBuilderSpec applicant2DQ() {
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

    public CaseDataBuilderSpec applicant2DQ(Applicant2DQ applicant2DQ) {
        this.applicant2DQ = applicant2DQ;
        return this;
    }

    public CaseDataBuilderSpec respondentSolicitor1OrganisationDetails(SolicitorOrganisationDetails organisationDetails) {
        this.respondentSolicitor1OrganisationDetails = organisationDetails;
        return this;
    }

    public CaseDataBuilderSpec respondentSolicitor2OrganisationDetails(SolicitorOrganisationDetails organisationDetails) {
        this.respondentSolicitor2OrganisationDetails = organisationDetails;
        return this;
    }

    public CaseDataBuilderSpec applicant1ProceedWithClaim(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaim = yesOrNo;
        return this;
    }

    public CaseDataBuilderSpec applicant1ProceedWithClaimSpec2v1(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimSpec2v1 = yesOrNo;
        return this;
    }

    public CaseDataBuilderSpec applicant1ProceedWithClaimMultiParty2v1(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimMultiParty2v1 = yesOrNo;
        return this;
    }

    public CaseDataBuilderSpec applicant2ProceedWithClaimMultiParty2v1(YesOrNo yesOrNo) {
        this.applicant2ProceedWithClaimMultiParty2v1 = yesOrNo;
        return this;
    }

    public CaseDataBuilderSpec applicantsProceedIntention(YesOrNo yesOrNo) {
        this.applicantsProceedIntention = yesOrNo;
        return this;
    }

    public CaseDataBuilderSpec applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = yesOrNo;
        return this;
    }

    public CaseDataBuilderSpec applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = yesOrNo;
        return this;
    }

    public CaseDataBuilderSpec respondent1ClaimResponseIntentionType(ResponseIntention responseIntention) {
        this.respondent1ClaimResponseIntentionType = responseIntention;
        return this;
    }

    public CaseDataBuilderSpec respondent2ClaimResponseIntentionType(ResponseIntention responseIntention) {
        this.respondent2ClaimResponseIntentionType = responseIntention;
        return this;
    }

    public CaseDataBuilderSpec claimValue(ClaimValue claimValue) {
        this.claimValue = claimValue;
        return this;
    }

    public CaseDataBuilderSpec issueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    public CaseDataBuilderSpec takenOfflineDate(LocalDateTime takenOfflineDate) {
        this.takenOfflineDate = takenOfflineDate;
        return this;
    }

    public CaseDataBuilderSpec systemGeneratedCaseDocuments(List<Element<CaseDocument>> systemGeneratedCaseDocuments) {
        this.systemGeneratedCaseDocuments = systemGeneratedCaseDocuments;
        return this;
    }

    public CaseDataBuilderSpec applicant1(Party party) {
        this.applicant1 = party;
        return this;
    }

    public CaseDataBuilderSpec applicant2(Party party) {
        this.applicant2 = party;
        return this;
    }

    public CaseDataBuilderSpec respondent1(Party party) {
        this.respondent1 = party;
        return this;
    }

    public CaseDataBuilderSpec legacyCaseReference(String legacyCaseReference) {
        this.legacyCaseReference = legacyCaseReference;
        return this;
    }

    public CaseDataBuilderSpec respondent1Represented(YesOrNo isRepresented) {
        this.respondent1Represented = isRepresented;
        return this;
    }

    public CaseDataBuilderSpec respondent2Represented(YesOrNo isRepresented) {
        this.respondent2Represented = isRepresented;
        return this;
    }

    public CaseDataBuilderSpec respondent1OrgRegistered(YesOrNo respondent1OrgRegistered) {
        this.respondent1OrgRegistered = respondent1OrgRegistered;
        return this;
    }

    public CaseDataBuilderSpec claimDetailsNotificationDate(LocalDateTime localDate) {
        this.claimDetailsNotificationDate = localDate;
        return this;
    }

    public CaseDataBuilderSpec respondent2OrgRegistered(YesOrNo respondent2OrgRegistered) {
        this.respondent2OrgRegistered = respondent2OrgRegistered;
        return this;
    }

    public CaseDataBuilderSpec claimProceedsInCaseman(ClaimProceedsInCaseman claimProceedsInCaseman) {
        this.claimProceedsInCaseman = claimProceedsInCaseman;
        return this;
    }

    public CaseDataBuilderSpec applicant1OrganisationPolicy(OrganisationPolicy applicant1OrganisationPolicy) {
        this.applicant1OrganisationPolicy = applicant1OrganisationPolicy;
        return this;
    }

    public CaseDataBuilderSpec respondent1OrganisationPolicy(OrganisationPolicy respondent1OrganisationPolicy) {
        this.respondent1OrganisationPolicy = respondent1OrganisationPolicy;
        return this;
    }

    public CaseDataBuilderSpec respondent2OrganisationPolicy(OrganisationPolicy respondent2OrganisationPolicy) {
        this.respondent2OrganisationPolicy = respondent2OrganisationPolicy;
        return this;
    }

    public CaseDataBuilderSpec addRespondent2(YesOrNo addRespondent2) {
        this.addRespondent2 = addRespondent2;
        return this;
    }

    public CaseDataBuilderSpec respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec respondentResponseTypeSpec) {
        this.respondent1ClaimResponseTypeForSpec = respondentResponseTypeSpec;
        return this;
    }

    public CaseDataBuilderSpec respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec respondentResponseTypeSpec) {
        this.respondent2ClaimResponseTypeForSpec = respondentResponseTypeSpec;
        return this;
    }

    public CaseDataBuilderSpec respondent1ClaimResponseType(RespondentResponseType respondent1ClaimResponseType) {
        this.respondent1ClaimResponseType = respondent1ClaimResponseType;
        return this;
    }

    public CaseDataBuilderSpec respondent2ClaimResponseType(RespondentResponseType respondent2ClaimResponseType) {
        this.respondent2ClaimResponseType = respondent2ClaimResponseType;
        return this;
    }

    public CaseDataBuilderSpec setRespondent1LitigationFriendCreatedDate(LocalDateTime createdDate) {
        this.respondent1LitigationFriendCreatedDate = createdDate;
        return this;
    }

    public CaseDataBuilderSpec setRespondent1LitigationFriendDate(LocalDateTime date) {
        this.respondent1LitigationFriendDate = date;
        return this;
    }

    public CaseDataBuilderSpec respondent2SameLegalRepresentative(YesOrNo respondent2SameLegalRepresentative) {
        this.respondent2SameLegalRepresentative = respondent2SameLegalRepresentative;
        return this;
    }

    public CaseDataBuilderSpec caseReference(Long ccdCaseReference) {
        this.ccdCaseReference = ccdCaseReference;
        return this;
    }

    public CaseDataBuilderSpec claimNotificationDeadline(LocalDateTime deadline) {
        this.claimNotificationDeadline = deadline;
        return this;
    }

    public CaseDataBuilderSpec claimDismissedDate(LocalDateTime date) {
        this.claimDismissedDate = date;
        return this;
    }

    public CaseDataBuilderSpec addLegalRepDeadline(LocalDateTime date) {
        this.addLegalRepDeadline = date;
        return this;
    }

    public CaseDataBuilderSpec takenOfflineByStaffDate(LocalDateTime takenOfflineByStaffDate) {
        this.takenOfflineByStaffDate = takenOfflineByStaffDate;
        return this;
    }

    public CaseDataBuilderSpec extensionDate(LocalDate extensionDate) {
        this.respondentSolicitor1AgreedDeadlineExtension = extensionDate;
        return this;
    }

    public CaseDataBuilderSpec uiStatementOfTruth(StatementOfTruth statementOfTruth) {
        this.uiStatementOfTruth = statementOfTruth;
        return this;
    }

    public CaseDataBuilderSpec defendantSolicitorNotifyClaimOptions(String defaultValue) {
        this.defendantSolicitorNotifyClaimOptions = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .label(defaultValue)
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilderSpec defendantSolicitorNotifyClaimDetailsOptions(String defaultValue) {
        this.defendantSolicitorNotifyClaimDetailsOptions = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .label(defaultValue)
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilderSpec selectLitigationFriend(String defaultValue) {
        this.selectLitigationFriend = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .label(defaultValue)
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilderSpec atState(FlowState.Main flowState) {
        return atState(flowState, ONE_V_ONE);
    }

    public CaseDataBuilderSpec atState(FlowState.Main flowState, MultiPartyScenario mpScenario) {
        switch (flowState) {
            case SPEC_DRAFT:
                return atStateClaimDraft();
            case CLAIM_SUBMITTED:
                return atStateSpec1v1ClaimSubmitted();

            default:
                throw new IllegalArgumentException("Invalid internal state: " + flowState);
        }
    }

    public CaseDataBuilderSpec atStateClaimDraft() {
        superClaimType = SPEC_CLAIM;
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

    public CaseDataBuilderSpec atStateSpec1v1ClaimSubmitted() {
        atStateClaimDraft();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = FAST_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        submittedDate = SUBMITTED_DATE_TIME;
        claimIssuedPaymentDetails = PaymentDetails.builder().customerReference("12345").build();
        return this;
    }

    public CaseDataBuilderSpec atStateClaimSubmittedTwoRespondentSameSolicitorSpec() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2SameLegalRepresentative = YES;
        return this;
    }

    public CaseDataBuilderSpec atStateClaimSubmittedTwoRespondentDifferentSolicitorSpec() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        respondent2SameLegalRepresentative = NO;
        return this;
    }

    public CaseDataBuilderSpec atStateClaimSubmitted2v1() {
        atStateSpec1v1ClaimSubmitted();
        addApplicant2 = YES;
        applicant2 = PartyBuilder.builder().individual().build();
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v1DefendantUnrepresentedClaimSubmitted() {
        atStateSpec1v1ClaimSubmitted();
        respondent1Represented = NO;

        return this;
    }

    public CaseDataBuilderSpec atStateSpec2v1DefendantUnrepresentedClaimSubmitted() {
        atStateSpec1v1ClaimSubmitted();
        addApplicant2 = YES;
        applicant2 = PartyBuilder.builder().individual().build();
        respondent1Represented = NO;

        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2OneDefendantUnrepresentedClaimSubmitted() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        respondent2Represented = NO;
        respondent2OrgRegistered = NO;

        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2BothDefendantUnrepresentedClaimSubmitted() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent1Represented = NO;
        respondent2Represented = NO;
        respondent1OrgRegistered = NO;
        respondent2OrgRegistered = NO;

        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v1DefendantUnregisteredClaimSubmitted() {
        atStateSpec1v1ClaimSubmitted();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;

        return this;
    }

    public CaseDataBuilderSpec atStateSpec2v1DefendantUnregisteredClaimSubmitted() {
        atStateSpec1v1ClaimSubmitted();
        addApplicant2 = YES;
        applicant2 = PartyBuilder.builder().individual().build();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;

        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2Solicitor1UnregisteredSolicitor2Registered() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;

        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2OneDefendantRepresentedUnregisteredOtherUnrepresentedClaimSubmitted() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent1Represented = NO;
        respondent1OrgRegistered = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;

        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2BothDefendantRepresentedAndUnregistered() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2SameLegalRepresentative = NO;
        respondent1Represented = YES;
        respondent2Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2OrgRegistered = NO;

        return this;
    }


    public static CaseDataBuilderSpec builder() {
        return new CaseDataBuilderSpec();
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
            //Unsuitable for SDO
            .reasonNotSuitableSDO(reasonNotSuitableSDO)
            .trialHearingTimeDJ(trialHearingTimeDJ)
            .trialOrderMadeWithoutHearingDJ(trialOrderMadeWithoutHearingDJ)
            .build();
    }

}
