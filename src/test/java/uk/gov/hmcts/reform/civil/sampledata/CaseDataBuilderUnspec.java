package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
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
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.CloseClaim;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.PartnerAndDependentsLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
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
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingFinalDisposalHearingDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
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
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimUntilType;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
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

public class CaseDataBuilderUnspec {

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
    protected CaseState ccdState;
    protected List<Element<CaseDocument>> systemGeneratedCaseDocuments;
    protected PaymentDetails claimIssuedPaymentDetails;
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
    protected LocalDateTime claimDismissedDate;
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
    private RespondentResponseTypeSpec respondent2ClaimResponseTypeForSpec;
    private UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec;
    private RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec;
    private Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer;
    private PartnerAndDependentsLRspec respondent1PartnerAndDependent;
    private PartnerAndDependentsLRspec respondent2PartnerAndDependent;
    private RepaymentPlanLRspec respondent1RepaymentPlan;
    private RepaymentPlanLRspec respondent2RepaymentPlan;
    private YesOrNo applicantsProceedIntention;
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

    //update pdf document from general applications
    private List<Element<CaseDocument>> generalOrderDocument;

    public CaseDataBuilderUnspec sameRateInterestSelection(SameRateInterestSelection sameRateInterestSelection) {
        this.sameRateInterestSelection = sameRateInterestSelection;
        return this;
    }

    public CaseDataBuilderUnspec breakDownInterestTotal(BigDecimal breakDownInterestTotal) {
        this.breakDownInterestTotal = breakDownInterestTotal;
        return this;
    }

    public CaseDataBuilderUnspec interestFromSpecificDate(LocalDate interestFromSpecificDate) {
        this.interestFromSpecificDate = interestFromSpecificDate;
        return this;
    }

    public CaseDataBuilderUnspec totalClaimAmount(BigDecimal totalClaimAmount) {
        this.totalClaimAmount = totalClaimAmount;
        return this;
    }

    public CaseDataBuilderUnspec interestClaimOptions(InterestClaimOptions interestClaimOptions) {
        this.interestClaimOptions = interestClaimOptions;
        return this;
    }

    public CaseDataBuilderUnspec interestClaimFrom(InterestClaimFromType interestClaimFrom) {
        this.interestClaimFrom = interestClaimFrom;
        return this;
    }

    public CaseDataBuilderUnspec interestClaimUntil(InterestClaimUntilType interestClaimUntil) {
        this.interestClaimUntil = interestClaimUntil;
        return this;
    }

    public CaseDataBuilderUnspec claimInterest(YesOrNo claimInterest) {
        this.claimInterest = claimInterest;
        return this;
    }

    //workaround fields
    protected Party respondent1Copy;
    protected Party respondent2Copy;

    public CaseDataBuilderUnspec respondent1ResponseDeadline(LocalDateTime deadline) {
        this.respondent1ResponseDeadline = deadline;
        return this;
    }

    public CaseDataBuilderUnspec respondent2ResponseDeadline(LocalDateTime deadline) {
        this.respondent2ResponseDeadline = deadline;
        return this;
    }

    public CaseDataBuilderUnspec respondent1AcknowledgeNotificationDate(LocalDateTime dateTime) {
        this.respondent1AcknowledgeNotificationDate = dateTime;
        return this;
    }

    public CaseDataBuilderUnspec respondent2AcknowledgeNotificationDate(LocalDateTime dateTime) {
        this.respondent2AcknowledgeNotificationDate = dateTime;
        return this;
    }

    public CaseDataBuilderUnspec applicantSolicitor1ServiceAddress(Address applicantSolicitor1ServiceAddress) {
        this.applicantSolicitor1ServiceAddress = applicantSolicitor1ServiceAddress;
        return this;
    }

    public CaseDataBuilderUnspec respondentSolicitor1ServiceAddress(Address respondentSolicitor1ServiceAddress) {
        this.respondentSolicitor1ServiceAddress = respondentSolicitor1ServiceAddress;
        return this;
    }

    public CaseDataBuilderUnspec respondentSolicitor2ServiceAddress(Address respondentSolicitor2ServiceAddress) {
        this.respondentSolicitor2ServiceAddress = respondentSolicitor2ServiceAddress;
        return this;
    }

    public CaseDataBuilderUnspec respondentSolicitor1EmailAddress(String respondentSolicitor1EmailAddress) {
        this.respondentSolicitor1EmailAddress = respondentSolicitor1EmailAddress;
        return this;
    }

    public CaseDataBuilderUnspec respondentSolicitor2EmailAddress(String respondentSolicitor2EmailAddress) {
        this.respondentSolicitor2EmailAddress = respondentSolicitor2EmailAddress;
        return this;
    }

    public CaseDataBuilderUnspec isRespondent1(YesOrNo isRespondent1) {
        this.isRespondent1 = isRespondent1;
        return this;
    }

    public CaseDataBuilderUnspec respondentSolicitor1AgreedDeadlineExtension(LocalDate extensionDate) {
        this.respondentSolicitor1AgreedDeadlineExtension = extensionDate;
        return this;
    }

    public CaseDataBuilderUnspec respondentSolicitor2AgreedDeadlineExtension(LocalDate extensionDate) {
        this.respondentSolicitor2AgreedDeadlineExtension = extensionDate;
        return this;
    }

    public CaseDataBuilderUnspec respondent1TimeExtensionDate(LocalDateTime extensionDate) {
        this.respondent1TimeExtensionDate = extensionDate;
        return this;
    }

    public CaseDataBuilderUnspec respondent2TimeExtensionDate(LocalDateTime extensionDate) {
        this.respondent2TimeExtensionDate = extensionDate;
        return this;
    }

    public CaseDataBuilderUnspec respondent2(Party party) {
        this.respondent2 = party;
        return this;
    }

    public CaseDataBuilderUnspec caseNotes(CaseNote caseNote) {
        this.caseNotes = ElementUtils.wrapElements(caseNote);
        return this;
    }

    public CaseDataBuilderUnspec respondent1OrganisationIDCopy(String id) {
        this.respondent1OrganisationIDCopy = id;
        return this;
    }

    public CaseDataBuilderUnspec respondent2OrganisationIDCopy(String id) {
        this.respondent2OrganisationIDCopy = id;
        return this;
    }

    public CaseDataBuilderUnspec respondent1DQ() {
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
            .respondent1DQRequestedCourt(RequestedCourt.builder().requestHearingAtSpecificCourt(NO).build())
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

    public CaseDataBuilderUnspec respondent1DQ(Respondent1DQ respondent1DQ) {
        this.respondent1DQ = respondent1DQ;
        return this;
    }

    public CaseDataBuilderUnspec respondent2DQ() {
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
            .respondent2DQRequestedCourt(RequestedCourt.builder().requestHearingAtSpecificCourt(NO).build())
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

    public CaseDataBuilderUnspec respondent2DQ(Respondent2DQ respondent2DQ) {
        this.respondent2DQ = respondent2DQ;
        return this;
    }

    public CaseDataBuilderUnspec applicant1DQ() {
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
            .applicant1DQRequestedCourt(RequestedCourt.builder().requestHearingAtSpecificCourt(NO).build())
            .applicant1DQHearingSupport(HearingSupport.builder().requirements(List.of()).build())
            .applicant1DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .applicant1DQLanguage(WelshLanguageRequirements.builder().build())
            .applicant1DQVulnerabilityQuestions(VulnerabilityQuestions.builder()
                                                    .vulnerabilityAdjustmentsRequired(NO).build())
            .applicant1DQStatementOfTruth(StatementOfTruth.builder().name("Bob Jones").role("Solicitor").build())
            .build();
        return this;
    }

    public CaseDataBuilderUnspec applicant1DQ(Applicant1DQ applicant1DQ) {
        this.applicant1DQ = applicant1DQ;
        return this;
    }

    public CaseDataBuilderUnspec applicant2DQ() {
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
            .applicant2DQRequestedCourt(RequestedCourt.builder().requestHearingAtSpecificCourt(NO).build())
            .applicant2DQHearingSupport(HearingSupport.builder().requirements(List.of()).build())
            .applicant2DQFurtherInformation(FurtherInformation.builder().futureApplications(NO).build())
            .applicant2DQLanguage(WelshLanguageRequirements.builder().build())
            .applicant2DQStatementOfTruth(StatementOfTruth.builder().name("Bob Jones").role("Solicitor").build())
            .build();
        return this;
    }

    public CaseDataBuilderUnspec applicant2DQ(Applicant2DQ applicant2DQ) {
        this.applicant2DQ = applicant2DQ;
        return this;
    }

    public CaseDataBuilderUnspec respondentSolicitor1OrgDetails(SolicitorOrganisationDetails organisationDetails) {
        this.respondentSolicitor1OrganisationDetails = organisationDetails;
        return this;
    }

    public CaseDataBuilderUnspec respondentSolicitor2OrgDetails(SolicitorOrganisationDetails organisationDetails) {
        this.respondentSolicitor2OrganisationDetails = organisationDetails;
        return this;
    }

    public CaseDataBuilderUnspec applicant1ProceedWithClaim(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaim = yesOrNo;
        return this;
    }

    public CaseDataBuilderUnspec applicant1ProceedWithClaimSpec2v1(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimSpec2v1 = yesOrNo;
        return this;
    }

    public CaseDataBuilderUnspec applicant1ProceedWithClaimMultiParty2v1(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimMultiParty2v1 = yesOrNo;
        return this;
    }

    public CaseDataBuilderUnspec applicant2ProceedWithClaimMultiParty2v1(YesOrNo yesOrNo) {
        this.applicant2ProceedWithClaimMultiParty2v1 = yesOrNo;
        return this;
    }

    public CaseDataBuilderUnspec applicantsProceedIntention(YesOrNo yesOrNo) {
        this.applicantsProceedIntention = yesOrNo;
        return this;
    }

    public CaseDataBuilderUnspec applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = yesOrNo;
        return this;
    }

    public CaseDataBuilderUnspec applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = yesOrNo;
        return this;
    }

    public CaseDataBuilderUnspec respondent1ClaimResponseIntentionType(ResponseIntention responseIntention) {
        this.respondent1ClaimResponseIntentionType = responseIntention;
        return this;
    }

    public CaseDataBuilderUnspec respondent2ClaimResponseIntentionType(ResponseIntention responseIntention) {
        this.respondent2ClaimResponseIntentionType = responseIntention;
        return this;
    }

    public CaseDataBuilderUnspec claimValue(ClaimValue claimValue) {
        this.claimValue = claimValue;
        return this;
    }

    public CaseDataBuilderUnspec issueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    public CaseDataBuilderUnspec takenOfflineDate(LocalDateTime takenOfflineDate) {
        this.takenOfflineDate = takenOfflineDate;
        return this;
    }

    public CaseDataBuilderUnspec sysGeneratedCaseDocs(List<Element<CaseDocument>> systemGeneratedCaseDocuments) {
        this.systemGeneratedCaseDocuments = systemGeneratedCaseDocuments;
        return this;
    }

    public CaseDataBuilderUnspec applicant1(Party party) {
        this.applicant1 = party;
        return this;
    }

    public CaseDataBuilderUnspec applicant2(Party party) {
        this.applicant2 = party;
        return this;
    }

    public CaseDataBuilderUnspec respondent1(Party party) {
        this.respondent1 = party;
        return this;
    }

    public CaseDataBuilderUnspec legacyCaseReference(String legacyCaseReference) {
        this.legacyCaseReference = legacyCaseReference;
        return this;
    }

    public CaseDataBuilderUnspec respondent1Represented(YesOrNo isRepresented) {
        this.respondent1Represented = isRepresented;
        return this;
    }

    public CaseDataBuilderUnspec respondent2Represented(YesOrNo isRepresented) {
        this.respondent2Represented = isRepresented;
        return this;
    }

    public CaseDataBuilderUnspec respondent1OrgRegistered(YesOrNo respondent1OrgRegistered) {
        this.respondent1OrgRegistered = respondent1OrgRegistered;
        return this;
    }

    public CaseDataBuilderUnspec claimDetailsNotificationDate(LocalDateTime localDate) {
        this.claimDetailsNotificationDate = localDate;
        return this;
    }

    public CaseDataBuilderUnspec respondent2OrgRegistered(YesOrNo respondent2OrgRegistered) {
        this.respondent2OrgRegistered = respondent2OrgRegistered;
        return this;
    }

    public CaseDataBuilderUnspec claimProceedsInCaseman(ClaimProceedsInCaseman claimProceedsInCaseman) {
        this.claimProceedsInCaseman = claimProceedsInCaseman;
        return this;
    }

    public CaseDataBuilderUnspec applicant1OrganisationPolicy(OrganisationPolicy applicant1OrganisationPolicy) {
        this.applicant1OrganisationPolicy = applicant1OrganisationPolicy;
        return this;
    }

    public CaseDataBuilderUnspec respondent1OrganisationPolicy(OrganisationPolicy respondent1OrganisationPolicy) {
        this.respondent1OrganisationPolicy = respondent1OrganisationPolicy;
        return this;
    }

    public CaseDataBuilderUnspec respondent2OrganisationPolicy(OrganisationPolicy respondent2OrganisationPolicy) {
        this.respondent2OrganisationPolicy = respondent2OrganisationPolicy;
        return this;
    }

    public CaseDataBuilderUnspec addRespondent2(YesOrNo addRespondent2) {
        this.addRespondent2 = addRespondent2;
        return this;
    }

    public CaseDataBuilderUnspec respondent2SameLegalRepresentative(YesOrNo respondent2SameLegalRepresentative) {
        this.respondent2SameLegalRepresentative = respondent2SameLegalRepresentative;
        return this;
    }

    public CaseDataBuilderUnspec caseReference(Long ccdCaseReference) {
        this.ccdCaseReference = ccdCaseReference;
        return this;
    }

    public CaseDataBuilderUnspec claimNotificationDeadline(LocalDateTime deadline) {
        this.claimNotificationDeadline = deadline;
        return this;
    }

    public CaseDataBuilderUnspec claimDismissedDate(LocalDateTime date) {
        this.claimDismissedDate = date;
        return this;
    }

    public CaseDataBuilderUnspec addLegalRepDeadline(LocalDateTime date) {
        this.addLegalRepDeadline = date;
        return this;
    }

    public CaseDataBuilderUnspec takenOfflineByStaffDate(LocalDateTime takenOfflineByStaffDate) {
        this.takenOfflineByStaffDate = takenOfflineByStaffDate;
        return this;
    }

    public CaseDataBuilderUnspec extensionDate(LocalDate extensionDate) {
        this.respondentSolicitor1AgreedDeadlineExtension = extensionDate;
        return this;
    }

    public CaseDataBuilderUnspec uiStatementOfTruth(StatementOfTruth statementOfTruth) {
        this.uiStatementOfTruth = statementOfTruth;
        return this;
    }

    public CaseDataBuilderUnspec defendantSolicitorNotifyClaimOptions(String defaultValue) {
        this.defendantSolicitorNotifyClaimOptions = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .label(defaultValue)
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilderUnspec defendantSolicitorNotifyClaimDetailsOptions(String defaultValue) {
        this.defendantSolicitorNotifyClaimDetailsOptions = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .label(defaultValue)
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilderUnspec selectLitigationFriend(String defaultValue) {
        this.selectLitigationFriend = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .label(defaultValue)
                       .build())
            .build();
        return this;
    }

    public CaseDataBuilderUnspec atState(FlowState.Main flowState) {
        return atState(flowState, ONE_V_ONE);
    }

    public CaseDataBuilderUnspec atState(FlowState.Main flowState, MultiPartyScenario mpScenario) {
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
            default:
                throw new IllegalArgumentException("Invalid internal state: " + flowState);
        }
    }

    public CaseDataBuilderUnspec atStateClaimPastClaimNotificationDeadline() {
        atStateClaimIssued();
        ccdState = CASE_DISMISSED;
        claimNotificationDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimDismissedPastClaimNotificationDeadline() {
        atStateClaimPastClaimNotificationDeadline();
        ccdState = CASE_DISMISSED;
        claimDismissedDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimPastClaimDetailsNotificationDeadline() {
        atStateClaimNotified();
        claimDetailsNotificationDeadline = LocalDateTime.now().minusDays(5);
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimDismissedPastClaimDetailsNotificationDeadline() {
        atStateClaimPastClaimDetailsNotificationDeadline();
        ccdState = CASE_DISMISSED;
        claimDismissedDate = claimNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimIssuedUnrepresentedDefendants() {
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

    public CaseDataBuilderUnspec atStateClaimIssued1v1UnrepresentedDefendant() {
        atStateClaimIssuedUnrepresentedDefendants();
        addRespondent2 = NO;
        respondent2 = null;
        respondent2Represented = null;
        respondent2OrganisationPolicy = null;
        respondent2OrgRegistered = null;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimIssued1v1UnrepresentedDefendantSpec() {
        atStateClaimIssuedUnrepresentedDefendants();
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        addRespondent2 = NO;
        respondent2 = null;
        respondent2Represented = null;
        respondent2OrganisationPolicy = null;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimIssued1v2UnrepresentedDefendant() {
        atStateClaimIssuedUnrepresentedDefendants();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        takenOfflineDate = LocalDateTime.now();
        respondentSolicitor2OrganisationDetails = null;
        respondent2OrganisationPolicy = null;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimIssuedUnrepresentedDefendant1() {
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

    public CaseDataBuilderUnspec atStateClaimIssuedUnrepresentedDefendant2() {
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

    public CaseDataBuilderUnspec atStateProceedsOfflineUnregisteredDefendants() {
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

    public CaseDataBuilderUnspec atStateProceedsOffline1v1UnregisteredDefendant() {
        atStateProceedsOfflineUnregisteredDefendants();
        addRespondent2 = NO;
        respondent2 = null;
        respondent2Represented = null;
        return this;
    }

    public CaseDataBuilderUnspec atStateProceedsOfflineUnregisteredDefendant1() {
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

    public CaseDataBuilderUnspec atStateProceedsOfflineUnregisteredDefendant2() {
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

    public CaseDataBuilderUnspec atStateProceedsOfflineSameUnregisteredDefendant() {
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

    public CaseDataBuilderUnspec atStateProceedsOfflineUnrepresentedDefendant1UnregisteredDefendant2() {
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

    public CaseDataBuilderUnspec atStateProceedsOfflineUnregisteredDefendant1UnrepresentedDefendant2() {
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

    public CaseDataBuilderUnspec atStateClaimDiscontinued() {
        atStateClaimDetailsNotified();
        return discontinueClaim();
    }

    public CaseDataBuilderUnspec discontinueClaim() {
        this.ccdState = CASE_DISMISSED;
        this.discontinueClaim = CloseClaim.builder()
            .date(LocalDate.now())
            .reason("My reason")
            .build();
        return this;
    }

    public CaseDataBuilderUnspec discontinueClaim(CloseClaim closeClaim) {
        this.discontinueClaim = closeClaim;
        return this;
    }

    public CaseDataBuilderUnspec discontinueClaimFrom(FlowState.Main flowState) {
        atState(flowState);
        this.ccdState = CASE_DISMISSED;
        this.discontinueClaim = CloseClaim.builder()
            .date(LocalDate.now())
            .reason("My reason")
            .build();
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimWithdrawn() {
        atStateClaimDetailsNotified();
        return withdrawClaim();
    }

    public CaseDataBuilderUnspec withdrawClaim(CloseClaim closeClaim) {
        this.withdrawClaim = closeClaim;
        return this;
    }

    public CaseDataBuilderUnspec withdrawClaim() {
        this.ccdState = CASE_DISMISSED;
        this.withdrawClaim = CloseClaim.builder()
            .date(LocalDate.now())
            .reason("My reason")
            .build();
        return this;
    }

    public CaseDataBuilderUnspec withdrawClaimFrom(FlowState.Main flowState) {
        atState(flowState);
        this.ccdState = CASE_DISMISSED;
        this.withdrawClaim = CloseClaim.builder()
            .date(LocalDate.now())
            .reason("My reason")
            .build();
        return this;
    }

    public CaseDataBuilderUnspec claimDismissedDeadline(LocalDateTime date) {
        this.claimDismissedDeadline = date;
        return this;
    }

    public CaseDataBuilderUnspec courtLocation_old() {
        this.courtLocation = CourtLocation.builder()
            .applicantPreferredCourt("127").build();
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimDraft() {
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

    public CaseDataBuilderUnspec multiPartyClaimTwoDefendantSolicitorsUnregistered() {
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

    public CaseDataBuilderUnspec atStateClaimSubmitted() {
        atStateClaimDraft();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = FAST_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        submittedDate = SUBMITTED_DATE_TIME;
        claimIssuedPaymentDetails = PaymentDetails.builder().customerReference("12345").build();
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimSubmittedOneRespondentRepresentative() {
        atStateClaimSubmitted();
        addRespondent2 = NO;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimSubmittedTwoRespondentRepresentativesDiffLegalRepresentative() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        return this;
    }

    public CaseDataBuilderUnspec abc() {
        atStateClaimSubmitted();
        addLegalRepDeadline = LocalDateTime.now();
        addRespondent2 = YES;
        respondent1Represented = YES;
        respondent2Represented = YES;
        return this;
    }


    public CaseDataBuilderUnspec atStateClaimSubmittedTwoRespondentRepresentativesBothUnregistered() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent2Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimSubmittedRespondent1Unregistered() {
        atStateClaimSubmitted();
        respondent1OrgRegistered = NO;

        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimSubmittedNoRespondentRepresented() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent1Represented = NO;
        respondent2Represented = NO;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        respondent2Represented = NO;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent1Represented = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimIssued1v2AndSameRepresentative() {
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

    public CaseDataBuilderUnspec atStateClaimIssued1v2AndSameUnregisteredRepresentative() {
        atStateClaimIssued1v2AndSameRepresentative();
        respondent1OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimSubmitted2v1RespondentUnrepresented() {
        atStateClaimSubmitted();
        multiPartyClaimTwoApplicants();
        respondent1Represented = NO;
        respondent1OrgRegistered = null;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimSubmitted2v1RespondentRegistered() {
        atStateClaimSubmitted();
        multiPartyClaimTwoApplicants();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimSubmitted2v1RespondentUnregistered() {
        atStateClaimSubmitted();
        multiPartyClaimTwoApplicants();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimIssued1v2AndBothDefendantsDefaultJudgment() {
        defendantDetails = DynamicList.builder()
            .value(DynamicListElement.builder().label("Both Defendants").build())
            .build();
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimIssued1v2AndOneDefendantDefaultJudgment() {
        defendantDetails = DynamicList.builder()
            .value(DynamicListElement.builder().label("Mr. Sole Trader").build())
            .build();
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimIssuedDisposalHearing() {
        caseManagementOrderSelection = DISPOSAL_HEARING;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimIssuedTrialHearing() {
        caseManagementOrderSelection = "TRIAL_HEARING";
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimIssuedTrialSDOInPersonHearing() {
        trialHearingMethodDJ = DisposalHearingMethodDJ.disposalHearingMethodInPerson;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimIssuedTrialLocationInPerson() {
        trialHearingMethodInPersonDJ = DynamicList.builder().value(
            DynamicListElement.builder().label("Court 1").build()).build();
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimIssuedTrialHearingInfo() {
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
            .build();
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimIssuedDisposalHearingInPerson() {
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

    public CaseDataBuilderUnspec atStateClaimIssuedDisposalSDOVideoCall() {
        disposalHearingMethodDJ = DisposalHearingMethodDJ.disposalHearingMethodVideoConferenceHearing;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimSubmitted1v2Respondent2OrgNotRegistered() {
        atStateClaimIssued();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderUnspec atStatePaymentFailed() {
        atStateClaimSubmitted();

        claimIssuedPaymentDetails = PaymentDetails.builder()
            .status(FAILED)
            .errorMessage("Your account is deleted")
            .errorCode("CA-E0004")
            .build();
        return this;
    }

    public CaseDataBuilderUnspec atStatePaymentSuccessful() {
        atStateClaimSubmitted();
        claimIssuedPaymentDetails = PaymentDetails.builder()
            .status(SUCCESS)
            .reference("RC-1604-0739-2145-4711")
            .build();
        paymentReference = "12345";
        paymentSuccessfulDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderUnspec atStatePaymentSuccessfulWithCopyOrganisationOnly() {
        atStatePaymentSuccessful();
        respondent1OrganisationIDCopy = respondent1OrganisationPolicy.getOrganisation().getOrganisationID();
        respondent1OrganisationPolicy = respondent1OrganisationPolicy.toBuilder()
            .organisation(Organisation.builder().build()).build();
        return this;
    }

    public CaseDataBuilderUnspec atStatePendingClaimIssued() {
        atStatePaymentSuccessful();
        issueDate = CLAIM_ISSUED_DATE;
        return this;
    }

    public CaseDataBuilderUnspec atStatePendingClaimIssuedUnregisteredDefendant() {
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

    public CaseDataBuilderUnspec atStatePendingClaimIssuedUnrepresentedDefendant() {
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

    public CaseDataBuilderUnspec atStatePendingClaimIssuedUnrepresentedUnregisteredDefendant() {
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

    public CaseDataBuilderUnspec atStateClaimIssued() {
        atStatePendingClaimIssued();
        claimNotificationDeadline = NOTIFICATION_DEADLINE;
        ccdState = CASE_ISSUED;
        respondent1OrganisationIDCopy = "QWERTY R";
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimNotified() {
        atStateClaimIssued();
        claimNotificationDate = issueDate.plusDays(1).atStartOfDay();
        claimDetailsNotificationDeadline = DEADLINE;
        ccdState = AWAITING_CASE_DETAILS_NOTIFICATION;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimNotified_1v1() {
        atStateClaimNotified();
        defendantSolicitorNotifyClaimOptions = null;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimNotified_1v2_andNotifyBothSolicitors() {
        atStateClaimNotified();
        multiPartyClaimTwoDefendantSolicitors();
        defendantSolicitorNotifyClaimOptions("Both");
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor() {
        atStateClaimNotified();
        multiPartyClaimTwoDefendantSolicitors();
        defendantSolicitorNotifyClaimOptions("Defendant One: Solicitor A");
        return this;
    }

    public CaseDataBuilderUnspec atStateProceedsOfflineAfterClaimNotified() {
        atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        return this;
    }

    public CaseDataBuilderUnspec atStateProceedsOfflineAfterClaimDetailsNotified() {
        atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimDetailsNotified() {
        atStateClaimNotified();
        claimDetailsNotificationDate = claimNotificationDate.plusDays(1);
        claimDismissedDeadline = LocalDateTime.now().plusMonths(6);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimDetailsNotified1v1() {
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

    public CaseDataBuilderUnspec atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors() {
        atStateClaimDetailsNotified();
        multiPartyClaimTwoDefendantSolicitors();
        defendantSolicitorNotifyClaimDetailsOptions("Both");
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor() {
        atStateClaimDetailsNotified();
        multiPartyClaimTwoDefendantSolicitors();
        defendantSolicitorNotifyClaimDetailsOptions("Defendant One: Solicitor");
        return this;
    }

    public CaseDataBuilderUnspec atStateAwaitingResponseFullDefenceReceived() {
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

    public CaseDataBuilderUnspec atStateAwaitingResponseNotFullDefenceReceived() {
        atStateAwaitingResponseNotFullDefenceReceived(RespondentResponseType.FULL_ADMISSION);
        return this;
    }

    public CaseDataBuilderUnspec atStateAwaitingResponseNotFullDefenceReceived(RespondentResponseType responseType) {
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

    public CaseDataBuilderUnspec atStateAddLitigationFriend_1v2_SameSolicitor() {
        atStateClaimDetailsNotified();
        multiPartyClaimOneDefendantSolicitor();
        addGenericRespondentLitigationFriend();
        return this;
    }

    public CaseDataBuilderUnspec atStateAddRespondent1LitigationFriend_1v2_SameSolicitor() {
        atStateClaimDetailsNotified();
        multiPartyClaimOneDefendantSolicitor();
        addRespondent1LitigationFriend();
        return this;
    }

    public CaseDataBuilderUnspec atStateAddRespondent2LitigationFriend_1v2_SameSolicitor() {
        atStateClaimDetailsNotified();
        multiPartyClaimOneDefendantSolicitor();
        addRespondent2LitigationFriend();
        return this;
    }

    public CaseDataBuilderUnspec atStateAddRespondent1LitigationFriend_1v2_DiffSolicitor() {
        return atStateAddRespondent1LitigationFriend_1v2_SameSolicitor()
            .respondent2SameLegalRepresentative(NO);
    }

    public CaseDataBuilderUnspec atStateAddRespondent2LitigationFriend_1v2_DiffSolicitor() {
        return atStateAddRespondent2LitigationFriend_1v2_SameSolicitor()
            .respondent2SameLegalRepresentative(NO);
    }

    public CaseDataBuilderUnspec atStateClaimDetailsNotifiedTimeExtension() {
        atStateClaimDetailsNotified();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondent1TimeExtensionDate = claimDetailsNotificationDate.plusDays(1);
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDate = null;
        return this;
    }

    public CaseDataBuilderUnspec atState1v2DifferentSolicitorClaimDetailsRespondent1NotifiedTimeExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2SameLegalRepresentative = NO;
        return this;
    }

    public CaseDataBuilderUnspec atState1v2DifferentSolicitorClaimDetailsRespondent2NotifiedTimeExtension() {
        atStateClaimDetailsNotified();
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2SameLegalRepresentative = NO;
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        respondent2TimeExtensionDate = claimDetailsNotificationDate.plusDays(1);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        return this;
    }

    public CaseDataBuilderUnspec atState1v2SameSolicitorClaimDetailsRespondentNotifiedTimeExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2SameLegalRepresentative = YES;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimDetailsNotifiedTimeExtension_Defendent2() {
        atStateClaimDetailsNotified();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        respondent2TimeExtensionDate = claimDetailsNotificationDate.plusDays(1);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        return this;
    }

    public CaseDataBuilderUnspec atStateTakenOfflineByStaff() {
        atStateClaimIssued();
        takenOfflineByStaff();
        return this;
    }

    public CaseDataBuilderUnspec atStateTakenOfflineByStaffAfterClaimNotified() {
        atStateClaimNotified();
        takenOfflineByStaff();
        takenOfflineByStaffDate = claimNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atStateTakenOfflineByStaffAfterClaimDetailsNotified() {
        atStateClaimDetailsNotified();
        takenOfflineByStaff();
        return this;
    }

    public CaseDataBuilderUnspec atStateTakenOfflineByStaffAfterClaimDetailsNotifiedExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        takenOfflineByStaff();
        takenOfflineByStaffDate = respondent1TimeExtensionDate.plusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atStateTakenOfflineByStaffAfterNotificationAcknowledged() {
        atStateNotificationAcknowledged();
        takenOfflineByStaff();
        takenOfflineByStaffDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atStateTakenOfflineByStaffAfterDefendantResponse() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        takenOfflineByStaff();
        takenOfflineByStaffDate = respondent1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension() {
        atStateNotificationAcknowledgedRespondent1TimeExtension();
        takenOfflineByStaff();
        takenOfflineByStaffDate = respondent1TimeExtensionDate.plusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec takenOfflineByStaff() {
        claimProceedsInCaseman = ClaimProceedsInCaseman.builder()
            .date(LocalDate.now())
            .reason(ReasonForProceedingOnPaper.APPLICATION)
            .build();
        takenOfflineByStaffDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderUnspec atStateSpec1v1ClaimSubmitted() {
        submittedDate = LocalDateTime.now().plusDays(1);
        addRespondent2 = NO;
        atStatePaymentSuccessful();
        atStatePendingClaimIssued();
        atStateClaimIssued();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderUnspec atStateSpec2v1ClaimSubmitted() {
        submittedDate = LocalDateTime.now().plusDays(1);
        addRespondent2 = NO;
        atStatePaymentSuccessful();
        atStatePendingClaimIssued();
        atStateClaimIssued();
        multiPartyClaimTwoApplicants();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderUnspec atStateSpec1v2ClaimSubmitted() {
        submittedDate = LocalDateTime.now().plusDays(1);
        atStatePaymentSuccessful();
        atStatePendingClaimIssued();
        atStateClaimIssued();
        multiPartyClaimOneDefendantSolicitor();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent1v1FullAdmissionSpec() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent1v1FullDefenceSpec() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent2v1FullAdmission() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent2v1FullDefence() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent2v1PartAdmission() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent2v1CounterClaim() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.COUNTER_CLAIM;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.COUNTER_CLAIM;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent2v1SecondFullDefence_FirstPartAdmission() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent2v1FirstFullDefence_SecondPartAdmission() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent2v1BothNotFullDefence_PartAdmissionX2() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilderUnspec atStateBothClaimantv1BothNotFullDefence_PartAdmissionX2() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent2v1BothNotFullDefence_CounterClaimX2() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.COUNTER_CLAIM;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.COUNTER_CLAIM;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent1v2FullAdmission() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent1v2AdmitAll_AdmitPart() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent1v2FullDefence_AdmitPart() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent1v2FullDefence_AdmitFull() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent1v2AdmintPart_FullDefence() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentFullDefence() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant1-defence.pdf").build())
            .build();
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentFullDefenceRespondent2() {
        atStateRespondent2RespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent2DQ();
        respondent2ResponseDate = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondent2RespondToClaim(RespondentResponseType respondentResponseType) {
        atStateNotificationAcknowledged();
        respondent2ClaimResponseType = respondentResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses() {
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

    public CaseDataBuilderUnspec atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim() {
        atStateRespondentFullDefence();
        respondent2ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent2DQ();
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentFullDefence_1v2_Resp1CounterClaimAndResp2FullDefence() {
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

    public CaseDataBuilderUnspec atStateDivergentResponseWithFullDefence1v2SameSol_NotSingleDQ() {
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

    public CaseDataBuilderUnspec atStateDivergentResponse_1v2_Resp1FullAdmissionAndResp2CounterClaim() {
        atStateRespondentFullAdmission();
        respondent2ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilderUnspec atStateFullAdmission_1v2_BothRespondentSolicitorsSubmitFullAdmissionResponse() {
        atStateRespondentFullAdmission();
        respondent2ClaimResponseType = RespondentResponseType.FULL_ADMISSION;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilderUnspec atStateApplicantRespondToDefenceAndNotProceed_1v2_DiffSol() {
        atStateApplicantRespondToDefenceAndNotProceed_1v2();
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentFullDefenceAfterNotificationAcknowledgement() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent1DQ();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentFullDefenceAfterAcknowledgementTimeExtension() {
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

    public CaseDataBuilderUnspec atStateRespondentFullDefenceAfterNotifyDetails() {
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

    public CaseDataBuilderUnspec atStateRespondentFullDefenceAfterNotifyClaimDetails() {
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

    public CaseDataBuilderUnspec atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse() {
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

    public CaseDataBuilderUnspec atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse() {
        atStateClaimDetailsNotified();
        respondent2ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        respondent2DQ();
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentFullDefenceAfterNotifyClaimDetailsTimeExtension() {
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

    public CaseDataBuilderUnspec atStateRespondentFullAdmission() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_ADMISSION);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderUnspec atStateBothRespondentsSameResponse(RespondentResponseType respondentResponseType) {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = respondentResponseType;
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        respondent2Responds(respondentResponseType);
        respondent2ResponseDate = LocalDateTime.now().plusDays(2);
        return this;
    }

    public CaseDataBuilderUnspec atState1v2SameSolicitorDivergentResponse(RespondentResponseType respondent1Response,
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

    public CaseDataBuilderUnspec atState1v2DivergentResponse(RespondentResponseType respondent1Response,
                                                             RespondentResponseType respondent2Response) {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = respondent1Response;
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        respondent2Responds(respondent2Response);
        respondent2ResponseDate = LocalDateTime.now().plusDays(2);
        return this;
    }

    public CaseDataBuilderUnspec atState1v2DivergentResponseSpec(RespondentResponseTypeSpec respondent1Response,
                                                                 RespondentResponseTypeSpec respondent2Response) {
        respondent1ClaimResponseTypeForSpec = respondent1Response;
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        respondent2RespondsSpec(respondent2Response);
        respondent2ResponseDate = LocalDateTime.now().plusDays(2);
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentFullAdmissionAfterNotificationAcknowledged() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_ADMISSION);
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderUnspec addEnterBreathingSpace() {
        this.enter = BreathingSpaceEnterInfo.builder()
                    .type(BreathingSpaceType.STANDARD)
                    .reference("12345")
                    .start(LocalDate.now())
                    .build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).build();

        return this;
    }

    public CaseDataBuilderUnspec addEnterMentalHealthBreathingSpace() {
        this.enter = BreathingSpaceEnterInfo.builder()
                    .type(BreathingSpaceType.MENTAL_HEALTH)
                    .reference("12345")
                    .start(LocalDate.now())
                    .build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).build();

        return this;
    }

    public CaseDataBuilderUnspec addEnterMentalHealthBreathingSpaceNoOptionalData() {
        this.enter = BreathingSpaceEnterInfo.builder()
            .type(BreathingSpaceType.MENTAL_HEALTH)
            .reference(null)
            .start(null)
            .build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).build();

        return this;
    }

    public CaseDataBuilderUnspec addLiftBreathingSpace() {
        this.enter = BreathingSpaceEnterInfo.builder()
            .type(BreathingSpaceType.STANDARD)
            .reference("12345")
            .start(LocalDate.now())
            .build();
        this.lift = BreathingSpaceLiftInfo.builder().expectedEnd(LocalDate.now()).build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).lift(this.lift).build();

        return this;
    }

    public CaseDataBuilderUnspec addLiftBreathingSpaceWithoutOptionalData() {
        this.enter = BreathingSpaceEnterInfo.builder()
            .type(BreathingSpaceType.STANDARD)
            .reference(null)
            .start(null)
            .build();
        this.lift = BreathingSpaceLiftInfo.builder().expectedEnd(null).build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).lift(this.lift).build();

        return this;
    }

    public CaseDataBuilderUnspec addLiftMentalBreathingSpace() {
        this.enter = BreathingSpaceEnterInfo.builder()
            .type(BreathingSpaceType.MENTAL_HEALTH)
            .reference("12345")
            .start(LocalDate.now())
            .build();
        this.lift = BreathingSpaceLiftInfo.builder().expectedEnd(LocalDate.now()).build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).lift(this.lift).build();

        return this;
    }

    public CaseDataBuilderUnspec addLiftMentalBreathingSpaceNoOptionalData() {
        this.enter = BreathingSpaceEnterInfo.builder()
            .type(BreathingSpaceType.MENTAL_HEALTH)
            .reference(null)
            .start(null)
            .build();
        this.lift = BreathingSpaceLiftInfo.builder().expectedEnd(null).build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).lift(this.lift).build();

        return this;
    }

    public CaseDataBuilderUnspec addEnterBreathingSpaceWithoutOptionalData() {
        this.enter = BreathingSpaceEnterInfo.builder()
            .type(BreathingSpaceType.STANDARD)
            .reference(null)
            .start(null)
            .build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).build();

        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentFullAdmissionAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.FULL_ADMISSION;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderUnspec addEnterBreathingSpaceWithOnlyReferenceInfo() {
        this.enter = BreathingSpaceEnterInfo.builder()
            .type(BreathingSpaceType.STANDARD)
            .reference("12345")
            .start(null)
            .build();

        this.breathing = BreathingSpaceInfo.builder().enter(this.enter).build();

        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentPartAdmission() {
        atStateRespondentRespondToClaim(RespondentResponseType.PART_ADMISSION);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentFullAdmissionAfterAcknowledgementTimeExtension() {
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

    public CaseDataBuilderUnspec atStateRespondentPartAdmissionAfterNotificationAcknowledgement() {
        atStateRespondentRespondToClaim(RespondentResponseType.PART_ADMISSION);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentPartAdmissionAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.PART_ADMISSION;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentPartAdmissionAfterAcknowledgementTimeExtension() {
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

    public CaseDataBuilderUnspec atStateRespondentCounterClaim() {
        atStateRespondentRespondToClaim(RespondentResponseType.COUNTER_CLAIM);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentCounterClaimAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderUnspec atStateRespondentCounterClaimAfterAcknowledgementTimeExtension() {
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

    public CaseDataBuilderUnspec atStateRespondentRespondToClaim(RespondentResponseType respondentResponseType) {
        atStateNotificationAcknowledged();
        respondent1ClaimResponseType = respondentResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataBuilderUnspec atStateProceedsOfflineAdmissionOrCounterClaim() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        return this;
    }

    public CaseDataBuilderUnspec atStateResRespondToClaimFastTrack(RespondentResponseType respondentResponseType) {
        atStateNotificationAcknowledged();
        respondToClaim = RespondToClaim.builder().howMuchWasPaid(FAST_TRACK_CLAIM_AMOUNT).build();
        totalClaimAmount = FAST_TRACK_CLAIM_AMOUNT;
        respondent1ClaimResponseType = respondentResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = LocalDateTime.now();
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataBuilderUnspec atStatePastClaimDismissedDeadline() {
        atStateClaimDetailsNotified();
        claimDismissedDeadline = LocalDateTime.now().minusDays(5);
        return this;
    }

    public CaseDataBuilderUnspec atStatePastClaimDismissedDeadline_1v2() {
        atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors();
        claimDismissedDeadline = LocalDateTime.now().minusDays(5);
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimDismissed() {
        atStatePastClaimDismissedDeadline();
        ccdState = CASE_DISMISSED;
        claimDismissedDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderUnspec atStateApplicantRespondToDefenceAndProceed() {
        return atStateApplicantRespondToDefenceAndProceed(ONE_V_ONE);
    }

    public CaseDataBuilderUnspec atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario mpScenario) {
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

    public CaseDataBuilderUnspec atStateApplicantRespondToDefenceAndNotProceed() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ProceedWithClaim = NO;
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = StatementOfTruth.builder().name("John Smith").role("Solicitor").build();
        return this;
    }

    public CaseDataBuilderUnspec respondent2RespondsSpec(RespondentResponseTypeSpec responseType) {
        this.respondent2ClaimResponseTypeForSpec = responseType;
        this.respondent2ResponseDate = LocalDateTime.now().plusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2() {
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

    public CaseDataBuilderUnspec atStateApplicantRespondToDefenceAndProceedVsDefendant1Only_1v2() {
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

    public CaseDataBuilderUnspec atStateApplicantRespondToDefenceAndProceedVsDefendant2Only_1v2() {
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

    public CaseDataBuilderUnspec atStateApplicantRespondToDefenceAndNotProceed_1v2() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = NO;
        applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = NO;
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = StatementOfTruth.builder().name("John Smith").role("Solicitor").build();
        return this;
    }

    public CaseDataBuilderUnspec atStateBothApplicantsRespondToDefenceAndProceed_2v1() {
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

    public CaseDataBuilderUnspec atStateApplicant1RespondToDefenceAndProceed_2v1() {
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

    public CaseDataBuilderUnspec atStateApplicant2RespondToDefenceAndProceed_2v1() {
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

    public CaseDataBuilderUnspec atStateApplicantRespondToDefenceAndNotProceed_2v1() {
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

    public CaseDataBuilderUnspec atStateNotificationAcknowledged1v2SameSolicitor() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseIntentionType = FULL_DEFENCE;
        respondent1AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        respondent2AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilderUnspec atStateNotificationAcknowledged() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseIntentionType = FULL_DEFENCE;
        respondent1AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilderUnspec atStateNotificationAcknowledged_1v2_BothDefendants() {
        atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors();
        respondent1ClaimResponseIntentionType = FULL_DEFENCE;
        respondent1AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        respondent2AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilderUnspec atStateNotificationAcknowledgedRespondent2() {
        atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors();
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        respondent2AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilderUnspec atStateNotificationAcknowledgedRespondent2Only() {
        atStateNotificationAcknowledgedRespondent2();
        respondent1AcknowledgeNotificationDate = null;
        return this;
    }

    public CaseDataBuilderUnspec atDeadlinePassedAfterStateNotificationAcknowledged() {
        atStateNotificationAcknowledged();
        this.claimDismissedDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        this.claimDismissedDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atDeadlinePassedAfterStateNotificationAcknowledgedTimeExtension() {
        atStateNotificationAcknowledgedRespondent1TimeExtension();
        this.claimDismissedDate = respondent1TimeExtensionDate.plusDays(1);
        this.claimDismissedDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atDeadlinePassedAfterStateClaimDetailsNotifiedExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        this.claimDismissedDate = respondent1TimeExtensionDate.plusDays(1);
        this.claimDismissedDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atDeadlinePassedAfterStateClaimDetailsNotified() {
        atStateClaimDismissedPastClaimDetailsNotificationDeadline();
        this.claimDismissedDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atStateNotificationAcknowledgedRespondent1TimeExt(int numberOfHoursAfterCurrentDate) {
        atStateNotificationAcknowledged();
        respondent1TimeExtensionDate = respondent1AcknowledgeNotificationDate.plusHours(numberOfHoursAfterCurrentDate);
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilderUnspec atStateNotificationAcknowledgedRespondent1TimeExtension() {
        return atStateNotificationAcknowledgedRespondent1TimeExt(1);
    }

    public CaseDataBuilderUnspec atStateNotificationAcknowledgedTimeExtension_1v2DS() {
        atStateNotificationAcknowledged_1v2_BothDefendants();
        respondent1TimeExtensionDate = respondent1AcknowledgeNotificationDate.plusHours(1);
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondent2TimeExtensionDate = respondent2AcknowledgeNotificationDate.plusHours(1);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilderUnspec atStateNotificationAckRespondent2TimeExtension(int numberOfHoursAfterCurrentDate) {
        atStateNotificationAcknowledged();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2TimeExtensionDate = respondent1AcknowledgeNotificationDate.plusHours(numberOfHoursAfterCurrentDate);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilderUnspec atStateNotificationAcknowledgedRespondent2TimeExtension() {
        return atStateNotificationAckRespondent2TimeExtension(5);
    }

    public CaseDataBuilderUnspec atStatePastApplicantResponseDeadline() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ResponseDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec atStateTakenOfflinePastApplicantResponseDeadline() {
        atStatePastApplicantResponseDeadline();
        takenOfflineDate = respondent1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec businessProcess(BusinessProcess businessProcess) {
        this.businessProcess = businessProcess;
        return this;
    }

    public CaseDataBuilderUnspec applicant2ResponseDate(LocalDateTime applicant2ResponseDate) {
        this.applicant2ResponseDate = applicant2ResponseDate;
        return this;
    }

    public CaseDataBuilderUnspec caseBundles(List<IdValue<Bundle>> caseBundles) {
        this.caseBundles = caseBundles;
        return this;
    }

    public CaseDataBuilderUnspec applicantSolicitor1UserDetails(IdamUserDetails applicantSolicitor1UserDetails) {
        this.applicantSolicitor1UserDetails = applicantSolicitor1UserDetails;
        return this;
    }

    public CaseDataBuilderUnspec addRespondent1LitigationFriend() {
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

    public CaseDataBuilderUnspec addGenericRespondentLitigationFriend() {
        this.genericLitigationFriend = LitigationFriend.builder()
            .fullName("Mr Litigation Friend")
            .build();
        return this;
    }

    public CaseDataBuilderUnspec addRespondent2LitigationFriend() {
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

    public CaseDataBuilderUnspec addBothRespondent1LitigationFriend() {
        this.respondent1LitigationFriend = LitigationFriend.builder()
            .fullName("Mr Litigation Friend")
            .build();
        this.respondent2LitigationFriend = LitigationFriend.builder()
            .fullName("Mr Litigation Friend 2")
            .build();
        return this;
    }

    public CaseDataBuilderUnspec multiPartyClaimTwoDefendantSolicitors() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build();
        this.respondent2SameLegalRepresentative = NO;
        this.respondentSolicitor2Reference = "01234";
        return this;
    }

    public CaseDataBuilderUnspec multiPartyClaimOneDefendantSolicitor() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build();
        this.respondent2SameLegalRepresentative = YES;
        return this;
    }

    public CaseDataBuilderUnspec multiPartyClaimTwoDefendantSolicitorsSpec() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build();
        this.respondent2SameLegalRepresentative = NO;
        this.respondentSolicitor2Reference = "01234";
        this.specRespondent1Represented = YES;
        this.specRespondent2Represented = YES;
        return this;
    }

    public CaseDataBuilderUnspec multiPartyClaimOneClaimant1ClaimResponseType() {
        this.claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilderUnspec respondent1ClaimResponseTypeToApplicant2Spec() {
        this.respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilderUnspec multiPartyClaimTwoApplicants() {
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

    public CaseDataBuilderUnspec setSuperClaimTypeToSpecClaim() {
        this.superClaimType = SPEC_CLAIM;
        return this;
    }

    public CaseDataBuilderUnspec respondent2Responds(RespondentResponseType responseType) {
        this.respondent2ClaimResponseType = responseType;
        this.respondent2ResponseDate = LocalDateTime.now().plusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec respondent2Responds1v2SameSol(RespondentResponseType responseType) {
        this.respondent2ClaimResponseType = responseType;
        this.respondent2ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec respondent2Responds1v2DiffSol(RespondentResponseType responseType) {
        this.respondent2ClaimResponseType = responseType;
        this.respondent2ResponseDate = respondent1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilderUnspec respondent1ClaimResponseTypeToApplicant2(RespondentResponseType responseType) {
        this.respondent1ClaimResponseTypeToApplicant2 = responseType;
        return this;
    }

    public CaseDataBuilderUnspec respondent1ClaimResponseTypeToApplicant1(RespondentResponseType responseType) {
        this.respondent1ClaimResponseType = responseType;
        respondent1DQ();
        return this;
    }

    public CaseDataBuilderUnspec respondentResponseIsSame(YesOrNo isSame) {
        this.respondentResponseIsSame = isSame;
        return this;
    }

    public CaseDataBuilderUnspec respondent1Copy(Party party) {
        this.respondent1Copy = party;
        return this;
    }

    public CaseDataBuilderUnspec respondent2Copy(Party party) {
        this.respondent2Copy = party;
        return this;
    }

    public CaseDataBuilderUnspec atSpecAoSApplicantCorrespondenceAddressRequired(
        YesOrNo specAoSApplicantCorrespondenceAddressRequired) {
        this.specAoSApplicantCorrespondenceAddressRequired = specAoSApplicantCorrespondenceAddressRequired;
        return this;
    }

    public CaseDataBuilderUnspec atSpecAoSApplicantCorrespondenceAddressDetails(
        Address specAoSApplicantCorrespondenceAddressDetails) {
        this.specAoSApplicantCorrespondenceAddressDetails = specAoSApplicantCorrespondenceAddressDetails;
        return this;
    }

    public CaseDataBuilderUnspec removeSolicitorReferences() {
        this.solicitorReferences = null;
        this.respondentSolicitor2Reference = null;
        return this;
    }

    public static CaseDataBuilderUnspec builder() {
        return new CaseDataBuilderUnspec();
    }

    public CaseData build() {
        return CaseData.builder()
            // Create Claim
            .legacyCaseReference(legacyCaseReference)
            .allocatedTrack(allocatedTrack)
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
            .claimDismissedDate(claimDismissedDate)
            .addLegalRepDeadline(addLegalRepDeadline)
            .applicantSolicitor1ServiceAddress(applicantSolicitor1ServiceAddress)
            .respondentSolicitor1ServiceAddress(respondentSolicitor1ServiceAddress)
            .respondentSolicitor2ServiceAddress(respondentSolicitor2ServiceAddress)
            .isRespondent1(isRespondent1)
            .defendantSolicitorNotifyClaimOptions(defendantSolicitorNotifyClaimOptions)
            .defendantSolicitorNotifyClaimDetailsOptions(defendantSolicitorNotifyClaimDetailsOptions)
            .selectLitigationFriend(selectLitigationFriend)
            .caseNotes(caseNotes)
            //ui field
            .uiStatementOfTruth(uiStatementOfTruth)
            .superClaimType(superClaimType == null ? UNSPEC_CLAIM : superClaimType)
            .caseBundles(caseBundles)
            .respondToClaim(respondToClaim)
            //spec route
            .respondent1ClaimResponseTypeForSpec(respondent1ClaimResponseTypeForSpec)
            .respondToAdmittedClaim(respondToClaim)
            .responseClaimAdmitPartEmployer(responseClaimAdmitPartEmployer)
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
            .breathing(breathing)
            .caseManagementOrderSelection(caseManagementOrderSelection)
            .respondent1PinToPostLRspec(respondent1PinToPostLRspec)
            .trialHearingMethodDJ(trialHearingMethodDJ)
            .disposalHearingMethodDJ(disposalHearingMethodDJ)
            .trialHearingMethodInPersonDJ(trialHearingMethodInPersonDJ)
            .disposalHearingBundleDJ(disposalHearingBundleDJ)
            .disposalHearingFinalDisposalHearingDJ(disposalHearingFinalDisposalHearingDJ)
            .trialHearingTrialDJ(trialHearingTrialDJ)
            .build();
    }
}
