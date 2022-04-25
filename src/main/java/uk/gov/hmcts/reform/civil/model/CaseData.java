package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.EmploymentTypeCheckboxFixedListLRspec;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.PersonalInjuryType;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.ExpertRequirements;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;

import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.FINISHED;

@Data
@SuppressWarnings("checkstyle:LineLength")
public class CaseData implements MappableObject {

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
    private final List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors;
    private final List<Element<Document>> generalAppEvidenceDocument;
    private final List<Element<GeneralApplication>> generalApplications;
    private final List<Element<GeneralApplicationsDetails>> generalApplicationsDetails;
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
    private final List<Element<CaseDocument>> systemGeneratedCaseDocuments;
    private final Document specClaimTemplateDocumentFiles;
    private final Document specClaimDetailsDocumentFiles;
    private final List<Evidence> speclistYourEvidenceList;
    private final YesOrNo specApplicantCorrespondenceAddressRequired;
    private final Address specApplicantCorrespondenceAddressdetails;
    private final YesOrNo specRespondentCorrespondenceAddressRequired;
    private final Address specRespondentCorrespondenceAddressdetails;
    //private final YesOrNo specAoSRespondent2HomeAddressRequired;
    //private final Address specAoSRespondent2HomeAddressDetails;

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
    private final List<Element<CaseDocument>> defendantResponseDocuments;

    private final YesOrNo applicant1ProceedWithClaim;
    private final YesOrNo applicant1ProceedWithClaimMultiParty2v1;
    private final YesOrNo applicant2ProceedWithClaimMultiParty2v1;
    private final YesOrNo applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2;
    private final YesOrNo applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2;
    private final YesOrNo applicant1ProceedWithClaimRespondent2;
    private final ResponseDocument applicant1DefenceResponseDocument;
    private final ResponseDocument claimantDefenceResDocToDefendant2;
    private final List<Element<CaseDocument>> claimantResponseDocuments;
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
    //private final YesOrNo specAoSApplicantCorrespondenceAddressRequired;
    //private final Address specAoSApplicantCorrespondenceAddressdetails;
    //private final YesOrNo specAoSRespondentCorrespondenceAddressRequired;
    //private final Address specAoSRespondentCorrespondenceAddressdetails;
    private final YesOrNo specRespondent1Represented;
    private final YesOrNo specRespondent2Represented;
    private final List<TimelineOfEvents> specResponseTimelineOfEvents;
    private final String specClaimResponseTimelineList;
    private final ResponseDocument specResponseTimelineDocumentFiles;
    private final List<Evidence> specResponselistYourEvidenceList;

    private final String detailsOfWhyDoesYouDisputeTheClaim;

    private final ResponseDocument respondent1SpecDefenceResponseDocument;

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
    private final PaymentUponCourtOrder respondent2CourtOrderPayment;
    private final RepaymentPlanLRspec respondent1RepaymentPlan;
    private final RepaymentPlanLRspec respondent2RepaymentPlan;
    private final RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec;
    private final UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec;
    private final Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer;
    private final Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployerRespondent2;
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

    CaseData(Long ccdCaseReference, CaseState ccdState, GAApplicationType generalAppType, GARespondentOrderAgreement generalAppRespondentAgreement, GAPbaDetails generalAppPBADetails, String generalAppDetailsOfOrder, String generalAppReasonsOfOrder, GAInformOtherParty generalAppInformOtherParty, GAUrgencyRequirement generalAppUrgencyRequirement, GAStatementOfTruth generalAppStatementOfTruth, GAHearingDetails generalAppHearingDetails, GASolicitorDetailsGAspec generalAppApplnSolicitor, List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors, List<Element<Document>> generalAppEvidenceDocument, List<Element<GeneralApplication>> generalApplications, List<Element<GeneralApplicationsDetails>> generalApplicationsDetails, SolicitorReferences solicitorReferences, SolicitorReferences solicitorReferencesCopy, String respondentSolicitor2Reference, CourtLocation courtLocation, Party applicant1, Party applicant2, CorrectEmail applicantSolicitor1CheckEmail, IdamUserDetails applicantSolicitor1UserDetails, YesOrNo addApplicant2, YesOrNo addRespondent2, YesOrNo respondent2SameLegalRepresentative, Party respondent1, Party respondent1Copy, Party respondent2, Party respondent2Copy, Party respondent1DetailsForClaimDetailsTab, Party respondent2DetailsForClaimDetailsTab, YesOrNo respondent1Represented, YesOrNo respondent2Represented, YesOrNo respondent1OrgRegistered, YesOrNo respondent2OrgRegistered, String respondentSolicitor1EmailAddress, String respondentSolicitor2EmailAddress, YesOrNo uploadParticularsOfClaim, String detailsOfClaim, ClaimValue claimValue, Fee claimFee, String paymentReference, DynamicList applicantSolicitor1PbaAccounts, ClaimType claimType, SuperClaimType superClaimType, String claimTypeOther, PersonalInjuryType personalInjuryType, String personalInjuryTypeOther, StatementOfTruth applicantSolicitor1ClaimStatementOfTruth, StatementOfTruth uiStatementOfTruth, String legacyCaseReference, AllocatedTrack allocatedTrack, PaymentDetails paymentDetails, PaymentDetails claimIssuedPaymentDetails, OrganisationPolicy applicant1OrganisationPolicy, OrganisationPolicy applicant2OrganisationPolicy, OrganisationPolicy respondent1OrganisationPolicy, OrganisationPolicy respondent2OrganisationPolicy, SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails, SolicitorOrganisationDetails respondentSolicitor2OrganisationDetails, YesOrNo applicantSolicitor1ServiceAddressRequired, Address applicantSolicitor1ServiceAddress, YesOrNo respondentSolicitor1ServiceAddressRequired, Address respondentSolicitor1ServiceAddress, YesOrNo respondentSolicitor2ServiceAddressRequired, Address respondentSolicitor2ServiceAddress, StatementOfTruth applicant1ServiceStatementOfTruthToRespondentSolicitor1, List<Element<CaseDocument>> systemGeneratedCaseDocuments, Document specClaimTemplateDocumentFiles, Document specClaimDetailsDocumentFiles, List<Evidence> speclistYourEvidenceList, YesOrNo specApplicantCorrespondenceAddressRequired, Address specApplicantCorrespondenceAddressdetails, YesOrNo specRespondentCorrespondenceAddressRequired, Address specRespondentCorrespondenceAddressdetails, LocalDate respondentSolicitor1AgreedDeadlineExtension, LocalDate respondentSolicitor2AgreedDeadlineExtension, ResponseIntention respondent1ClaimResponseIntentionType, ResponseIntention respondent2ClaimResponseIntentionType, ResponseIntention respondent1ClaimResponseIntentionTypeApplicant2, ServedDocumentFiles servedDocumentFiles, YesOrNo respondentResponseIsSame, YesOrNo defendantSingleResponseToBothClaimants, RespondentResponseType respondent1ClaimResponseType, RespondentResponseType respondent2ClaimResponseType, RespondentResponseType respondent1ClaimResponseTypeToApplicant2, ResponseDocument respondent1ClaimResponseDocument, ResponseDocument respondent2ClaimResponseDocument, ResponseDocument respondentSharedClaimResponseDocument, CaseDocument respondent1GeneratedResponseDocument, CaseDocument respondent2GeneratedResponseDocument, List<Element<CaseDocument>> defendantResponseDocuments, YesOrNo applicant1ProceedWithClaim, YesOrNo applicant1ProceedWithClaimMultiParty2v1, YesOrNo applicant2ProceedWithClaimMultiParty2v1, YesOrNo applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2, YesOrNo applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2, YesOrNo applicant1ProceedWithClaimRespondent2, ResponseDocument applicant1DefenceResponseDocument, ResponseDocument claimantDefenceResDocToDefendant2, List<Element<CaseDocument>> claimantResponseDocuments, List<ClaimAmountBreakup> claimAmountBreakup, List<TimelineOfEvents> timelineOfEvents, BigDecimal totalClaimAmount, BigDecimal totalInterest, YesOrNo claimInterest, InterestClaimOptions interestClaimOptions, SameRateInterestSelection sameRateInterestSelection, BigDecimal breakDownInterestTotal, String breakDownInterestDescription, InterestClaimFromType interestClaimFrom, InterestClaimUntilType interestClaimUntil, LocalDate interestFromSpecificDate, String interestFromSpecificDateDescription, String calculatedInterest, String specRespondentSolicitor1EmailAddress, YesOrNo specRespondent1Represented, YesOrNo specRespondent2Represented, List<TimelineOfEvents> specResponseTimelineOfEvents, String specClaimResponseTimelineList, ResponseDocument specResponseTimelineDocumentFiles, List<Evidence> specResponselistYourEvidenceList, String detailsOfWhyDoesYouDisputeTheClaim, ResponseDocument respondent1SpecDefenceResponseDocument, RespondentResponseTypeSpec respondent1ClaimResponseTypeForSpec, RespondentResponseTypeSpec respondent2ClaimResponseTypeForSpec, RespondentResponseTypeSpec claimant1ClaimResponseTypeForSpec, RespondentResponseTypeSpec claimant2ClaimResponseTypeForSpec, RespondentResponseTypeSpecPaidStatus respondent1ClaimResponsePaymentAdmissionForSpec, RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteRequired, String defenceRouteRequired, String responseClaimTrack, RespondToClaim respondToClaim, RespondToClaim respondToAdmittedClaim, BigDecimal respondToAdmittedClaimOwingAmount, BigDecimal respondToAdmittedClaimOwingAmountPounds, YesOrNo specDefenceFullAdmittedRequired, PaymentUponCourtOrder respondent1CourtOrderPayment, PaymentUponCourtOrder respondent2CourtOrderPayment, RepaymentPlanLRspec respondent1RepaymentPlan, RepaymentPlanLRspec respondent2RepaymentPlan, RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec, UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec, Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer, Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployerRespondent2, String responseToClaimAdmitPartWhyNotPayLRspec, YesOrNo responseClaimMediationSpecRequired, SmallClaimMedicalLRspec applicant1ClaimMediationSpecRequired, YesOrNo defenceAdmitPartEmploymentTypeRequired, YesOrNo responseClaimExpertSpecRequired, YesOrNo applicant1ClaimExpertSpecRequired, String responseClaimWitnesses, String applicant1ClaimWitnesses, YesOrNo smallClaimHearingInterpreterRequired, String smallClaimHearingInterpreterDescription, List<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspec, YesOrNo specDefenceAdmittedRequired, String additionalInformationForJudge, String applicantAdditionalInformationForJudge, ExpertRequirements respondToClaimExperts, String caseNote, List<Element<CaseNote>> caseNotes, @Valid CloseClaim withdrawClaim, @Valid CloseClaim discontinueClaim, BusinessProcess businessProcess, Respondent1DQ respondent1DQ, Respondent2DQ respondent2DQ, Applicant1DQ applicant1DQ, Applicant2DQ applicant2DQ, LitigationFriend genericLitigationFriend, LitigationFriend respondent1LitigationFriend, LitigationFriend respondent2LitigationFriend, YesOrNo applicant1LitigationFriendRequired, LitigationFriend applicant1LitigationFriend, YesOrNo applicant2LitigationFriendRequired, LitigationFriend applicant2LitigationFriend, DynamicList defendantSolicitorNotifyClaimOptions, DynamicList defendantSolicitorNotifyClaimDetailsOptions, DynamicList selectLitigationFriend, String litigantFriendSelection, @Valid ClaimProceedsInCaseman claimProceedsInCaseman, YesOrNo applicantSolicitor1PbaAccountsIsEmpty, MultiPartyResponseTypeFlags multiPartyResponseTypeFlags, YesOrNo applicantsProceedIntention, MultiPartyScenario claimantResponseScenarioFlag, YesOrNo claimantResponseDocumentToDefendant2Flag, YesOrNo claimant2ResponseFlag, RespondentResponseTypeSpec atLeastOneClaimResponseTypeForSpecIsFullDefence, YesOrNo specFullAdmissionOrPartAdmission, YesOrNo sameSolicitorSameResponse, YesOrNo specPaidLessAmountOrDisputesOrPartAdmission, YesOrNo specFullDefenceOrPartAdmission1V1, YesOrNo specFullDefenceOrPartAdmission, YesOrNo specDisputesOrPartAdmission, LocalDateTime submittedDate, LocalDateTime paymentSuccessfulDate, LocalDate issueDate, LocalDateTime claimNotificationDeadline, LocalDateTime claimNotificationDate, LocalDateTime claimDetailsNotificationDeadline, LocalDateTime claimDetailsNotificationDate, LocalDateTime respondent1ResponseDeadline, LocalDateTime respondent2ResponseDeadline, LocalDateTime claimDismissedDeadline, LocalDateTime respondent1TimeExtensionDate, LocalDateTime respondent2TimeExtensionDate, LocalDateTime respondent1AcknowledgeNotificationDate, LocalDateTime respondent2AcknowledgeNotificationDate, LocalDateTime respondent1ResponseDate, LocalDateTime respondent2ResponseDate, LocalDateTime applicant1ResponseDeadline, LocalDateTime applicant1ResponseDate, LocalDateTime applicant2ResponseDate, LocalDateTime takenOfflineDate, LocalDateTime takenOfflineByStaffDate, LocalDateTime claimDismissedDate, String claimAmountBreakupSummaryObject, LocalDateTime respondent1LitigationFriendDate, LocalDateTime respondent2LitigationFriendDate, LocalDateTime respondent1LitigationFriendCreatedDate, LocalDateTime respondent2LitigationFriendCreatedDate, List<IdValue<Bundle>> caseBundles, Respondent1DebtLRspec specDefendant1Debts, Respondent1DebtLRspec specDefendant2Debts, Respondent1SelfEmploymentLRspec specDefendant1SelfEmploymentDetails, Respondent1SelfEmploymentLRspec specDefendant2SelfEmploymentDetails, String detailsOfDirectionDisposal, String detailsOfDirectionTrial, HearingSupportRequirementsDJ hearingSupportRequirementsDJ, DynamicList defendantDetailsSpec, DynamicList defendantDetails, String bothDefendants, String partialPaymentAmount, YesOrNo partialPayment, LocalDate paymentSetDate, String repaymentSummaryObject, YesOrNo paymentConfirmationDecisionSpec, String repaymentDue, String repaymentSuggestion, String currentDatebox, LocalDate repaymentDate, List<Element<CaseDocument>> defaultJudgmentDocuments, String hearingSelection, DJPaymentTypeSelection paymentTypeSelection, RepaymentFrequencyDJ repaymentFrequency, YesOrNo isRespondent1, YesOrNo isRespondent2, YesOrNo isApplicant1, YesOrNo claimStarted, CaseDataExtension caseDataExtension) {
        this.ccdCaseReference = ccdCaseReference;
        this.ccdState = ccdState;
        this.generalAppType = generalAppType;
        this.generalAppRespondentAgreement = generalAppRespondentAgreement;
        this.generalAppPBADetails = generalAppPBADetails;
        this.generalAppDetailsOfOrder = generalAppDetailsOfOrder;
        this.generalAppReasonsOfOrder = generalAppReasonsOfOrder;
        this.generalAppInformOtherParty = generalAppInformOtherParty;
        this.generalAppUrgencyRequirement = generalAppUrgencyRequirement;
        this.generalAppStatementOfTruth = generalAppStatementOfTruth;
        this.generalAppHearingDetails = generalAppHearingDetails;
        this.generalAppApplnSolicitor = generalAppApplnSolicitor;
        this.generalAppRespondentSolicitors = generalAppRespondentSolicitors;
        this.generalAppEvidenceDocument = generalAppEvidenceDocument;
        this.generalApplications = generalApplications;
        this.generalApplicationsDetails = generalApplicationsDetails;
        this.solicitorReferences = solicitorReferences;
        this.solicitorReferencesCopy = solicitorReferencesCopy;
        this.respondentSolicitor2Reference = respondentSolicitor2Reference;
        this.courtLocation = courtLocation;
        this.applicant1 = applicant1;
        this.applicant2 = applicant2;
        this.applicantSolicitor1CheckEmail = applicantSolicitor1CheckEmail;
        this.applicantSolicitor1UserDetails = applicantSolicitor1UserDetails;
        this.addApplicant2 = addApplicant2;
        this.addRespondent2 = addRespondent2;
        this.respondent2SameLegalRepresentative = respondent2SameLegalRepresentative;
        this.respondent1 = respondent1;
        this.respondent1Copy = respondent1Copy;
        this.respondent2 = respondent2;
        this.respondent2Copy = respondent2Copy;
        this.respondent1DetailsForClaimDetailsTab = respondent1DetailsForClaimDetailsTab;
        this.respondent2DetailsForClaimDetailsTab = respondent2DetailsForClaimDetailsTab;
        this.respondent1Represented = respondent1Represented;
        this.respondent2Represented = respondent2Represented;
        this.respondent1OrgRegistered = respondent1OrgRegistered;
        this.respondent2OrgRegistered = respondent2OrgRegistered;
        this.respondentSolicitor1EmailAddress = respondentSolicitor1EmailAddress;
        this.respondentSolicitor2EmailAddress = respondentSolicitor2EmailAddress;
        this.uploadParticularsOfClaim = uploadParticularsOfClaim;
        this.detailsOfClaim = detailsOfClaim;
        this.claimValue = claimValue;
        this.claimFee = claimFee;
        this.paymentReference = paymentReference;
        this.applicantSolicitor1PbaAccounts = applicantSolicitor1PbaAccounts;
        this.claimType = claimType;
        this.superClaimType = superClaimType;
        this.claimTypeOther = claimTypeOther;
        this.personalInjuryType = personalInjuryType;
        this.personalInjuryTypeOther = personalInjuryTypeOther;
        this.applicantSolicitor1ClaimStatementOfTruth = applicantSolicitor1ClaimStatementOfTruth;
        this.uiStatementOfTruth = uiStatementOfTruth;
        this.legacyCaseReference = legacyCaseReference;
        this.allocatedTrack = allocatedTrack;
        this.paymentDetails = paymentDetails;
        this.claimIssuedPaymentDetails = claimIssuedPaymentDetails;
        this.applicant1OrganisationPolicy = applicant1OrganisationPolicy;
        this.applicant2OrganisationPolicy = applicant2OrganisationPolicy;
        this.respondent1OrganisationPolicy = respondent1OrganisationPolicy;
        this.respondent2OrganisationPolicy = respondent2OrganisationPolicy;
        this.respondentSolicitor1OrganisationDetails = respondentSolicitor1OrganisationDetails;
        this.respondentSolicitor2OrganisationDetails = respondentSolicitor2OrganisationDetails;
        this.applicantSolicitor1ServiceAddressRequired = applicantSolicitor1ServiceAddressRequired;
        this.applicantSolicitor1ServiceAddress = applicantSolicitor1ServiceAddress;
        this.respondentSolicitor1ServiceAddressRequired = respondentSolicitor1ServiceAddressRequired;
        this.respondentSolicitor1ServiceAddress = respondentSolicitor1ServiceAddress;
        this.respondentSolicitor2ServiceAddressRequired = respondentSolicitor2ServiceAddressRequired;
        this.respondentSolicitor2ServiceAddress = respondentSolicitor2ServiceAddress;
        this.applicant1ServiceStatementOfTruthToRespondentSolicitor1 = applicant1ServiceStatementOfTruthToRespondentSolicitor1;
        this.systemGeneratedCaseDocuments = systemGeneratedCaseDocuments;
        this.specClaimTemplateDocumentFiles = specClaimTemplateDocumentFiles;
        this.specClaimDetailsDocumentFiles = specClaimDetailsDocumentFiles;
        this.speclistYourEvidenceList = speclistYourEvidenceList;
        this.specApplicantCorrespondenceAddressRequired = specApplicantCorrespondenceAddressRequired;
        this.specApplicantCorrespondenceAddressdetails = specApplicantCorrespondenceAddressdetails;
        this.specRespondentCorrespondenceAddressRequired = specRespondentCorrespondenceAddressRequired;
        this.specRespondentCorrespondenceAddressdetails = specRespondentCorrespondenceAddressdetails;
        this.respondentSolicitor1AgreedDeadlineExtension = respondentSolicitor1AgreedDeadlineExtension;
        this.respondentSolicitor2AgreedDeadlineExtension = respondentSolicitor2AgreedDeadlineExtension;
        this.respondent1ClaimResponseIntentionType = respondent1ClaimResponseIntentionType;
        this.respondent2ClaimResponseIntentionType = respondent2ClaimResponseIntentionType;
        this.respondent1ClaimResponseIntentionTypeApplicant2 = respondent1ClaimResponseIntentionTypeApplicant2;
        this.servedDocumentFiles = servedDocumentFiles;
        this.respondentResponseIsSame = respondentResponseIsSame;
        this.defendantSingleResponseToBothClaimants = defendantSingleResponseToBothClaimants;
        this.respondent1ClaimResponseType = respondent1ClaimResponseType;
        this.respondent2ClaimResponseType = respondent2ClaimResponseType;
        this.respondent1ClaimResponseTypeToApplicant2 = respondent1ClaimResponseTypeToApplicant2;
        this.respondent1ClaimResponseDocument = respondent1ClaimResponseDocument;
        this.respondent2ClaimResponseDocument = respondent2ClaimResponseDocument;
        this.respondentSharedClaimResponseDocument = respondentSharedClaimResponseDocument;
        this.respondent1GeneratedResponseDocument = respondent1GeneratedResponseDocument;
        this.respondent2GeneratedResponseDocument = respondent2GeneratedResponseDocument;
        this.defendantResponseDocuments = defendantResponseDocuments;
        this.applicant1ProceedWithClaim = applicant1ProceedWithClaim;
        this.applicant1ProceedWithClaimMultiParty2v1 = applicant1ProceedWithClaimMultiParty2v1;
        this.applicant2ProceedWithClaimMultiParty2v1 = applicant2ProceedWithClaimMultiParty2v1;
        this.applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2;
        this.applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2;
        this.applicant1ProceedWithClaimRespondent2 = applicant1ProceedWithClaimRespondent2;
        this.applicant1DefenceResponseDocument = applicant1DefenceResponseDocument;
        this.claimantDefenceResDocToDefendant2 = claimantDefenceResDocToDefendant2;
        this.claimantResponseDocuments = claimantResponseDocuments;
        this.claimAmountBreakup = claimAmountBreakup;
        this.timelineOfEvents = timelineOfEvents;
        this.totalClaimAmount = totalClaimAmount;
        this.totalInterest = totalInterest;
        this.claimInterest = claimInterest;
        this.interestClaimOptions = interestClaimOptions;
        this.sameRateInterestSelection = sameRateInterestSelection;
        this.breakDownInterestTotal = breakDownInterestTotal;
        this.breakDownInterestDescription = breakDownInterestDescription;
        this.interestClaimFrom = interestClaimFrom;
        this.interestClaimUntil = interestClaimUntil;
        this.interestFromSpecificDate = interestFromSpecificDate;
        this.interestFromSpecificDateDescription = interestFromSpecificDateDescription;
        this.calculatedInterest = calculatedInterest;
        this.specRespondentSolicitor1EmailAddress = specRespondentSolicitor1EmailAddress;
        //this.specAoSApplicantCorrespondenceAddressRequired = specAoSApplicantCorrespondenceAddressRequired;
        //this.specAoSApplicantCorrespondenceAddressdetails = specAoSApplicantCorrespondenceAddressdetails;
        //this.specAoSRespondentCorrespondenceAddressRequired = specAoSRespondentCorrespondenceAddressRequired;
        //this.specAoSRespondentCorrespondenceAddressdetails = specAoSRespondentCorrespondenceAddressdetails;
        this.specRespondent1Represented = specRespondent1Represented;
        this.specRespondent2Represented = specRespondent2Represented;
        this.specResponseTimelineOfEvents = specResponseTimelineOfEvents;
        this.specClaimResponseTimelineList = specClaimResponseTimelineList;
        this.specResponseTimelineDocumentFiles = specResponseTimelineDocumentFiles;
        this.specResponselistYourEvidenceList = specResponselistYourEvidenceList;
        this.detailsOfWhyDoesYouDisputeTheClaim = detailsOfWhyDoesYouDisputeTheClaim;
        this.respondent1SpecDefenceResponseDocument = respondent1SpecDefenceResponseDocument;
        this.respondent1ClaimResponseTypeForSpec = respondent1ClaimResponseTypeForSpec;
        this.respondent2ClaimResponseTypeForSpec = respondent2ClaimResponseTypeForSpec;
        this.claimant1ClaimResponseTypeForSpec = claimant1ClaimResponseTypeForSpec;
        this.claimant2ClaimResponseTypeForSpec = claimant2ClaimResponseTypeForSpec;
        this.respondent1ClaimResponsePaymentAdmissionForSpec = respondent1ClaimResponsePaymentAdmissionForSpec;
        this.defenceAdmitPartPaymentTimeRouteRequired = defenceAdmitPartPaymentTimeRouteRequired;
        this.defenceRouteRequired = defenceRouteRequired;
        this.responseClaimTrack = responseClaimTrack;
        this.respondToClaim = respondToClaim;
        this.respondToAdmittedClaim = respondToAdmittedClaim;
        this.respondToAdmittedClaimOwingAmount = respondToAdmittedClaimOwingAmount;
        this.respondToAdmittedClaimOwingAmountPounds = respondToAdmittedClaimOwingAmountPounds;
        this.specDefenceFullAdmittedRequired = specDefenceFullAdmittedRequired;
        this.respondent1CourtOrderPayment = respondent1CourtOrderPayment;
        this.respondent2CourtOrderPayment = respondent2CourtOrderPayment;
        this.respondent1RepaymentPlan = respondent1RepaymentPlan;
        this.respondent2RepaymentPlan = respondent2RepaymentPlan;
        this.respondToClaimAdmitPartLRspec = respondToClaimAdmitPartLRspec;
        this.respondToClaimAdmitPartUnemployedLRspec = respondToClaimAdmitPartUnemployedLRspec;
        this.responseClaimAdmitPartEmployer = responseClaimAdmitPartEmployer;
        this.responseClaimAdmitPartEmployerRespondent2 = responseClaimAdmitPartEmployerRespondent2;
        this.responseToClaimAdmitPartWhyNotPayLRspec = responseToClaimAdmitPartWhyNotPayLRspec;
        this.responseClaimMediationSpecRequired = responseClaimMediationSpecRequired;
        this.applicant1ClaimMediationSpecRequired = applicant1ClaimMediationSpecRequired;
        this.defenceAdmitPartEmploymentTypeRequired = defenceAdmitPartEmploymentTypeRequired;
        this.responseClaimExpertSpecRequired = responseClaimExpertSpecRequired;
        this.applicant1ClaimExpertSpecRequired = applicant1ClaimExpertSpecRequired;
        this.responseClaimWitnesses = responseClaimWitnesses;
        this.applicant1ClaimWitnesses = applicant1ClaimWitnesses;
        this.smallClaimHearingInterpreterRequired = smallClaimHearingInterpreterRequired;
        this.smallClaimHearingInterpreterDescription = smallClaimHearingInterpreterDescription;
        this.respondToClaimAdmitPartEmploymentTypeLRspec = respondToClaimAdmitPartEmploymentTypeLRspec;
        this.specDefenceAdmittedRequired = specDefenceAdmittedRequired;
        this.additionalInformationForJudge = additionalInformationForJudge;
        this.applicantAdditionalInformationForJudge = applicantAdditionalInformationForJudge;
        this.respondToClaimExperts = respondToClaimExperts;
        this.caseNote = caseNote;
        this.caseNotes = caseNotes;
        this.withdrawClaim = withdrawClaim;
        this.discontinueClaim = discontinueClaim;
        this.businessProcess = businessProcess;
        this.respondent1DQ = respondent1DQ;
        this.respondent2DQ = respondent2DQ;
        this.applicant1DQ = applicant1DQ;
        this.applicant2DQ = applicant2DQ;
        this.genericLitigationFriend = genericLitigationFriend;
        this.respondent1LitigationFriend = respondent1LitigationFriend;
        this.respondent2LitigationFriend = respondent2LitigationFriend;
        this.applicant1LitigationFriendRequired = applicant1LitigationFriendRequired;
        this.applicant1LitigationFriend = applicant1LitigationFriend;
        this.applicant2LitigationFriendRequired = applicant2LitigationFriendRequired;
        this.applicant2LitigationFriend = applicant2LitigationFriend;
        this.defendantSolicitorNotifyClaimOptions = defendantSolicitorNotifyClaimOptions;
        this.defendantSolicitorNotifyClaimDetailsOptions = defendantSolicitorNotifyClaimDetailsOptions;
        this.selectLitigationFriend = selectLitigationFriend;
        this.litigantFriendSelection = litigantFriendSelection;
        this.claimProceedsInCaseman = claimProceedsInCaseman;
        this.applicantSolicitor1PbaAccountsIsEmpty = applicantSolicitor1PbaAccountsIsEmpty;
        this.multiPartyResponseTypeFlags = multiPartyResponseTypeFlags;
        this.applicantsProceedIntention = applicantsProceedIntention;
        this.claimantResponseScenarioFlag = claimantResponseScenarioFlag;
        this.claimantResponseDocumentToDefendant2Flag = claimantResponseDocumentToDefendant2Flag;
        this.claimant2ResponseFlag = claimant2ResponseFlag;
        this.atLeastOneClaimResponseTypeForSpecIsFullDefence = atLeastOneClaimResponseTypeForSpecIsFullDefence;
        this.specFullAdmissionOrPartAdmission = specFullAdmissionOrPartAdmission;
        this.sameSolicitorSameResponse = sameSolicitorSameResponse;
        this.specPaidLessAmountOrDisputesOrPartAdmission = specPaidLessAmountOrDisputesOrPartAdmission;
        this.specFullDefenceOrPartAdmission1V1 = specFullDefenceOrPartAdmission1V1;
        this.specFullDefenceOrPartAdmission = specFullDefenceOrPartAdmission;
        this.specDisputesOrPartAdmission = specDisputesOrPartAdmission;
        this.submittedDate = submittedDate;
        this.paymentSuccessfulDate = paymentSuccessfulDate;
        this.issueDate = issueDate;
        this.claimNotificationDeadline = claimNotificationDeadline;
        this.claimNotificationDate = claimNotificationDate;
        this.claimDetailsNotificationDeadline = claimDetailsNotificationDeadline;
        this.claimDetailsNotificationDate = claimDetailsNotificationDate;
        this.respondent1ResponseDeadline = respondent1ResponseDeadline;
        this.respondent2ResponseDeadline = respondent2ResponseDeadline;
        this.claimDismissedDeadline = claimDismissedDeadline;
        this.respondent1TimeExtensionDate = respondent1TimeExtensionDate;
        this.respondent2TimeExtensionDate = respondent2TimeExtensionDate;
        this.respondent1AcknowledgeNotificationDate = respondent1AcknowledgeNotificationDate;
        this.respondent2AcknowledgeNotificationDate = respondent2AcknowledgeNotificationDate;
        this.respondent1ResponseDate = respondent1ResponseDate;
        this.respondent2ResponseDate = respondent2ResponseDate;
        this.applicant1ResponseDeadline = applicant1ResponseDeadline;
        this.applicant1ResponseDate = applicant1ResponseDate;
        this.applicant2ResponseDate = applicant2ResponseDate;
        this.takenOfflineDate = takenOfflineDate;
        this.takenOfflineByStaffDate = takenOfflineByStaffDate;
        this.claimDismissedDate = claimDismissedDate;
        this.claimAmountBreakupSummaryObject = claimAmountBreakupSummaryObject;
        this.respondent1LitigationFriendDate = respondent1LitigationFriendDate;
        this.respondent2LitigationFriendDate = respondent2LitigationFriendDate;
        this.respondent1LitigationFriendCreatedDate = respondent1LitigationFriendCreatedDate;
        this.respondent2LitigationFriendCreatedDate = respondent2LitigationFriendCreatedDate;
        this.caseBundles = caseBundles;
        this.specDefendant1Debts = specDefendant1Debts;
        this.specDefendant2Debts = specDefendant2Debts;
        this.specDefendant1SelfEmploymentDetails = specDefendant1SelfEmploymentDetails;
        this.specDefendant2SelfEmploymentDetails = specDefendant2SelfEmploymentDetails;
        this.detailsOfDirectionDisposal = detailsOfDirectionDisposal;
        this.detailsOfDirectionTrial = detailsOfDirectionTrial;
        this.hearingSupportRequirementsDJ = hearingSupportRequirementsDJ;
        this.defendantDetailsSpec = defendantDetailsSpec;
        this.defendantDetails = defendantDetails;
        this.bothDefendants = bothDefendants;
        this.partialPaymentAmount = partialPaymentAmount;
        this.partialPayment = partialPayment;
        this.paymentSetDate = paymentSetDate;
        this.repaymentSummaryObject = repaymentSummaryObject;
        this.paymentConfirmationDecisionSpec = paymentConfirmationDecisionSpec;
        this.repaymentDue = repaymentDue;
        this.repaymentSuggestion = repaymentSuggestion;
        this.currentDatebox = currentDatebox;
        this.repaymentDate = repaymentDate;
        this.defaultJudgmentDocuments = defaultJudgmentDocuments;
        this.hearingSelection = hearingSelection;
        this.paymentTypeSelection = paymentTypeSelection;
        this.repaymentFrequency = repaymentFrequency;
        this.isRespondent1 = isRespondent1;
        this.isRespondent2 = isRespondent2;
        this.isApplicant1 = isApplicant1;
        this.claimStarted = claimStarted;
        this.caseDataExtension = caseDataExtension;
    }

    public static CaseDataBuilder builder() {
        return new CaseDataBuilder();
    }

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

    //CCD UI flag
    private final YesOrNo applicantSolicitor1PbaAccountsIsEmpty;
    private MultiPartyResponseTypeFlags multiPartyResponseTypeFlags;
    private YesOrNo applicantsProceedIntention;
    private final MultiPartyScenario claimantResponseScenarioFlag;
    private YesOrNo claimantResponseDocumentToDefendant2Flag;
    private YesOrNo claimant2ResponseFlag;
    private RespondentResponseTypeSpec atLeastOneClaimResponseTypeForSpecIsFullDefence;
    private YesOrNo specFullAdmissionOrPartAdmission;
    private YesOrNo sameSolicitorSameResponse;
    private YesOrNo specPaidLessAmountOrDisputesOrPartAdmission;
    private YesOrNo specFullDefenceOrPartAdmission1V1;
    private YesOrNo specFullDefenceOrPartAdmission;
    private YesOrNo specDisputesOrPartAdmission;

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
    private final LocalDateTime claimDismissedDate;
    private final String claimAmountBreakupSummaryObject;
    private final LocalDateTime respondent1LitigationFriendDate;
    private final LocalDateTime respondent2LitigationFriendDate;

    private final LocalDateTime respondent1LitigationFriendCreatedDate;
    private final LocalDateTime respondent2LitigationFriendCreatedDate;
    private final List<IdValue<Bundle>> caseBundles;

    private final Respondent1DebtLRspec specDefendant1Debts;
    private final Respondent1DebtLRspec specDefendant2Debts;
    private final Respondent1SelfEmploymentLRspec specDefendant1SelfEmploymentDetails;
    private final Respondent1SelfEmploymentLRspec specDefendant2SelfEmploymentDetails;

    private final String detailsOfDirectionDisposal;
    private final String detailsOfDirectionTrial;
    private final HearingSupportRequirementsDJ hearingSupportRequirementsDJ;
    private final DynamicList defendantDetailsSpec;
    private final DynamicList defendantDetails;
    private final String bothDefendants;
    private final String partialPaymentAmount;
    private final YesOrNo partialPayment;
    private final LocalDate paymentSetDate;
    private final String repaymentSummaryObject;
    private final YesOrNo paymentConfirmationDecisionSpec;
    private final String repaymentDue;
    private final String repaymentSuggestion;
    private final String currentDatebox;
    private final LocalDate repaymentDate;
    private final List<Element<CaseDocument>> defaultJudgmentDocuments;
    private final String hearingSelection;
    // for default judgment specified tab
    private final DJPaymentTypeSelection paymentTypeSelection;
    private final RepaymentFrequencyDJ repaymentFrequency;
    // for default judgment specified tab
    private final YesOrNo isRespondent1;
    private final YesOrNo isRespondent2;
    private final YesOrNo isApplicant1;

    private final YesOrNo claimStarted;

    private final CaseDataExtension caseDataExtension;

    public CaseDataBuilder toBuilder() {
        return new CaseDataBuilder().ccdCaseReference(this.ccdCaseReference).ccdState(this.ccdState).generalAppType(this.generalAppType).generalAppRespondentAgreement(
            this.generalAppRespondentAgreement).generalAppPBADetails(this.generalAppPBADetails).generalAppDetailsOfOrder(
            this.generalAppDetailsOfOrder).generalAppReasonsOfOrder(this.generalAppReasonsOfOrder).generalAppInformOtherParty(
            this.generalAppInformOtherParty).generalAppUrgencyRequirement(this.generalAppUrgencyRequirement).generalAppStatementOfTruth(
            this.generalAppStatementOfTruth).generalAppHearingDetails(this.generalAppHearingDetails).generalAppApplnSolicitor(
            this.generalAppApplnSolicitor).generalAppRespondentSolicitors(this.generalAppRespondentSolicitors).generalAppEvidenceDocument(
            this.generalAppEvidenceDocument).generalApplications(this.generalApplications).generalApplicationsDetails(
            this.generalApplicationsDetails).solicitorReferences(this.solicitorReferences).solicitorReferencesCopy(this.solicitorReferencesCopy).respondentSolicitor2Reference(
            this.respondentSolicitor2Reference).courtLocation(this.courtLocation).applicant1(this.applicant1).applicant2(
            this.applicant2).applicantSolicitor1CheckEmail(this.applicantSolicitor1CheckEmail).applicantSolicitor1UserDetails(
            this.applicantSolicitor1UserDetails).addApplicant2(this.addApplicant2).addRespondent2(this.addRespondent2).respondent2SameLegalRepresentative(
            this.respondent2SameLegalRepresentative).respondent1(this.respondent1).respondent1Copy(this.respondent1Copy).respondent2(
            this.respondent2).respondent2Copy(this.respondent2Copy).respondent1DetailsForClaimDetailsTab(this.respondent1DetailsForClaimDetailsTab).respondent2DetailsForClaimDetailsTab(
            this.respondent2DetailsForClaimDetailsTab).respondent1Represented(this.respondent1Represented).respondent2Represented(
            this.respondent2Represented).respondent1OrgRegistered(this.respondent1OrgRegistered).respondent2OrgRegistered(
            this.respondent2OrgRegistered).respondentSolicitor1EmailAddress(this.respondentSolicitor1EmailAddress).respondentSolicitor2EmailAddress(
            this.respondentSolicitor2EmailAddress).uploadParticularsOfClaim(this.uploadParticularsOfClaim).detailsOfClaim(
            this.detailsOfClaim).claimValue(this.claimValue).claimFee(this.claimFee).paymentReference(this.paymentReference).applicantSolicitor1PbaAccounts(
            this.applicantSolicitor1PbaAccounts).claimType(this.claimType).superClaimType(this.superClaimType).claimTypeOther(
            this.claimTypeOther).personalInjuryType(this.personalInjuryType).personalInjuryTypeOther(this.personalInjuryTypeOther).applicantSolicitor1ClaimStatementOfTruth(
            this.applicantSolicitor1ClaimStatementOfTruth).uiStatementOfTruth(this.uiStatementOfTruth).legacyCaseReference(
            this.legacyCaseReference).allocatedTrack(this.allocatedTrack).paymentDetails(this.paymentDetails).claimIssuedPaymentDetails(
            this.claimIssuedPaymentDetails).applicant1OrganisationPolicy(this.applicant1OrganisationPolicy).applicant2OrganisationPolicy(
            this.applicant2OrganisationPolicy).respondent1OrganisationPolicy(this.respondent1OrganisationPolicy).respondent2OrganisationPolicy(
            this.respondent2OrganisationPolicy).respondentSolicitor1OrganisationDetails(this.respondentSolicitor1OrganisationDetails).respondentSolicitor2OrganisationDetails(
            this.respondentSolicitor2OrganisationDetails).applicantSolicitor1ServiceAddressRequired(this.applicantSolicitor1ServiceAddressRequired).applicantSolicitor1ServiceAddress(
            this.applicantSolicitor1ServiceAddress).respondentSolicitor1ServiceAddressRequired(this.respondentSolicitor1ServiceAddressRequired).respondentSolicitor1ServiceAddress(
            this.respondentSolicitor1ServiceAddress).respondentSolicitor2ServiceAddressRequired(this.respondentSolicitor2ServiceAddressRequired).respondentSolicitor2ServiceAddress(
            this.respondentSolicitor2ServiceAddress).applicant1ServiceStatementOfTruthToRespondentSolicitor1(this.applicant1ServiceStatementOfTruthToRespondentSolicitor1).systemGeneratedCaseDocuments(
            this.systemGeneratedCaseDocuments).specClaimTemplateDocumentFiles(this.specClaimTemplateDocumentFiles).specClaimDetailsDocumentFiles(
            this.specClaimDetailsDocumentFiles).speclistYourEvidenceList(this.speclistYourEvidenceList).specApplicantCorrespondenceAddressRequired(
            this.specApplicantCorrespondenceAddressRequired).specApplicantCorrespondenceAddressdetails(this.specApplicantCorrespondenceAddressdetails).specRespondentCorrespondenceAddressRequired(
            this.specRespondentCorrespondenceAddressRequired).specRespondentCorrespondenceAddressdetails(this.specRespondentCorrespondenceAddressdetails).respondentSolicitor1AgreedDeadlineExtension(
            this.respondentSolicitor1AgreedDeadlineExtension).respondentSolicitor2AgreedDeadlineExtension(this.respondentSolicitor2AgreedDeadlineExtension).respondent1ClaimResponseIntentionType(
            this.respondent1ClaimResponseIntentionType).respondent2ClaimResponseIntentionType(this.respondent2ClaimResponseIntentionType).respondent1ClaimResponseIntentionTypeApplicant2(
            this.respondent1ClaimResponseIntentionTypeApplicant2).servedDocumentFiles(this.servedDocumentFiles).respondentResponseIsSame(
            this.respondentResponseIsSame).defendantSingleResponseToBothClaimants(this.defendantSingleResponseToBothClaimants).respondent1ClaimResponseType(
            this.respondent1ClaimResponseType).respondent2ClaimResponseType(this.respondent2ClaimResponseType).respondent1ClaimResponseTypeToApplicant2(
            this.respondent1ClaimResponseTypeToApplicant2).respondent1ClaimResponseDocument(this.respondent1ClaimResponseDocument).respondent2ClaimResponseDocument(
            this.respondent2ClaimResponseDocument).respondentSharedClaimResponseDocument(this.respondentSharedClaimResponseDocument).respondent1GeneratedResponseDocument(
            this.respondent1GeneratedResponseDocument).respondent2GeneratedResponseDocument(this.respondent2GeneratedResponseDocument).defendantResponseDocuments(
            this.defendantResponseDocuments).applicant1ProceedWithClaim(this.applicant1ProceedWithClaim).applicant1ProceedWithClaimMultiParty2v1(
            this.applicant1ProceedWithClaimMultiParty2v1).applicant2ProceedWithClaimMultiParty2v1(this.applicant2ProceedWithClaimMultiParty2v1).applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(
            this.applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2).applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(
            this.applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2).applicant1ProceedWithClaimRespondent2(this.applicant1ProceedWithClaimRespondent2).applicant1DefenceResponseDocument(
            this.applicant1DefenceResponseDocument).claimantDefenceResDocToDefendant2(this.claimantDefenceResDocToDefendant2).claimantResponseDocuments(
            this.claimantResponseDocuments).claimAmountBreakup(this.claimAmountBreakup).timelineOfEvents(this.timelineOfEvents).totalClaimAmount(
            this.totalClaimAmount).totalInterest(this.totalInterest).claimInterest(this.claimInterest).interestClaimOptions(
            this.interestClaimOptions).sameRateInterestSelection(this.sameRateInterestSelection).breakDownInterestTotal(
            this.breakDownInterestTotal).breakDownInterestDescription(this.breakDownInterestDescription).interestClaimFrom(
            this.interestClaimFrom).interestClaimUntil(this.interestClaimUntil).interestFromSpecificDate(this.interestFromSpecificDate).interestFromSpecificDateDescription(
            this.interestFromSpecificDateDescription).calculatedInterest(this.calculatedInterest).specRespondentSolicitor1EmailAddress(
            this.specRespondentSolicitor1EmailAddress).specRespondent1Represented(this.specRespondent1Represented).specRespondent2Represented(
            this.specRespondent2Represented).specResponseTimelineOfEvents(this.specResponseTimelineOfEvents).specClaimResponseTimelineList(
            this.specClaimResponseTimelineList).specResponseTimelineDocumentFiles(this.specResponseTimelineDocumentFiles).specResponselistYourEvidenceList(
            this.specResponselistYourEvidenceList).detailsOfWhyDoesYouDisputeTheClaim(this.detailsOfWhyDoesYouDisputeTheClaim).respondent1SpecDefenceResponseDocument(
            this.respondent1SpecDefenceResponseDocument).respondent1ClaimResponseTypeForSpec(this.respondent1ClaimResponseTypeForSpec).respondent2ClaimResponseTypeForSpec(
            this.respondent2ClaimResponseTypeForSpec).claimant1ClaimResponseTypeForSpec(this.claimant1ClaimResponseTypeForSpec).claimant2ClaimResponseTypeForSpec(
            this.claimant2ClaimResponseTypeForSpec).respondent1ClaimResponsePaymentAdmissionForSpec(this.respondent1ClaimResponsePaymentAdmissionForSpec).defenceAdmitPartPaymentTimeRouteRequired(
            this.defenceAdmitPartPaymentTimeRouteRequired).defenceRouteRequired(this.defenceRouteRequired).responseClaimTrack(
            this.responseClaimTrack).respondToClaim(this.respondToClaim).respondToAdmittedClaim(this.respondToAdmittedClaim).respondToAdmittedClaimOwingAmount(
            this.respondToAdmittedClaimOwingAmount).respondToAdmittedClaimOwingAmountPounds(this.respondToAdmittedClaimOwingAmountPounds).specDefenceFullAdmittedRequired(
            this.specDefenceFullAdmittedRequired).respondent1CourtOrderPayment(this.respondent1CourtOrderPayment).respondent2CourtOrderPayment(
            this.respondent2CourtOrderPayment).respondent1RepaymentPlan(this.respondent1RepaymentPlan).respondent2RepaymentPlan(
            this.respondent2RepaymentPlan).respondToClaimAdmitPartLRspec(this.respondToClaimAdmitPartLRspec).respondToClaimAdmitPartUnemployedLRspec(
            this.respondToClaimAdmitPartUnemployedLRspec).responseClaimAdmitPartEmployer(this.responseClaimAdmitPartEmployer).responseClaimAdmitPartEmployerRespondent2(
            this.responseClaimAdmitPartEmployerRespondent2).responseToClaimAdmitPartWhyNotPayLRspec(this.responseToClaimAdmitPartWhyNotPayLRspec).responseClaimMediationSpecRequired(
            this.responseClaimMediationSpecRequired).applicant1ClaimMediationSpecRequired(this.applicant1ClaimMediationSpecRequired).defenceAdmitPartEmploymentTypeRequired(
            this.defenceAdmitPartEmploymentTypeRequired).responseClaimExpertSpecRequired(this.responseClaimExpertSpecRequired).applicant1ClaimExpertSpecRequired(
            this.applicant1ClaimExpertSpecRequired).responseClaimWitnesses(this.responseClaimWitnesses).applicant1ClaimWitnesses(
            this.applicant1ClaimWitnesses).smallClaimHearingInterpreterRequired(this.smallClaimHearingInterpreterRequired).smallClaimHearingInterpreterDescription(
            this.smallClaimHearingInterpreterDescription).respondToClaimAdmitPartEmploymentTypeLRspec(this.respondToClaimAdmitPartEmploymentTypeLRspec).specDefenceAdmittedRequired(
            this.specDefenceAdmittedRequired).additionalInformationForJudge(this.additionalInformationForJudge).applicantAdditionalInformationForJudge(
            this.applicantAdditionalInformationForJudge).respondToClaimExperts(this.respondToClaimExperts).caseNote(this.caseNote).caseNotes(
            this.caseNotes).withdrawClaim(this.withdrawClaim).discontinueClaim(this.discontinueClaim).businessProcess(
            this.businessProcess).respondent1DQ(this.respondent1DQ).respondent2DQ(this.respondent2DQ).applicant1DQ(this.applicant1DQ).applicant2DQ(
            this.applicant2DQ).genericLitigationFriend(this.genericLitigationFriend).respondent1LitigationFriend(this.respondent1LitigationFriend).respondent2LitigationFriend(
            this.respondent2LitigationFriend).applicant1LitigationFriendRequired(this.applicant1LitigationFriendRequired).applicant1LitigationFriend(
            this.applicant1LitigationFriend).applicant2LitigationFriendRequired(this.applicant2LitigationFriendRequired).applicant2LitigationFriend(
            this.applicant2LitigationFriend).defendantSolicitorNotifyClaimOptions(this.defendantSolicitorNotifyClaimOptions).defendantSolicitorNotifyClaimDetailsOptions(
            this.defendantSolicitorNotifyClaimDetailsOptions).selectLitigationFriend(this.selectLitigationFriend).litigantFriendSelection(
            this.litigantFriendSelection).claimProceedsInCaseman(this.claimProceedsInCaseman).applicantSolicitor1PbaAccountsIsEmpty(
            this.applicantSolicitor1PbaAccountsIsEmpty).multiPartyResponseTypeFlags(this.multiPartyResponseTypeFlags).applicantsProceedIntention(
            this.applicantsProceedIntention).claimantResponseScenarioFlag(this.claimantResponseScenarioFlag).claimantResponseDocumentToDefendant2Flag(
            this.claimantResponseDocumentToDefendant2Flag).claimant2ResponseFlag(this.claimant2ResponseFlag).atLeastOneClaimResponseTypeForSpecIsFullDefence(
            this.atLeastOneClaimResponseTypeForSpecIsFullDefence).specFullAdmissionOrPartAdmission(this.specFullAdmissionOrPartAdmission).sameSolicitorSameResponse(
            this.sameSolicitorSameResponse).specPaidLessAmountOrDisputesOrPartAdmission(this.specPaidLessAmountOrDisputesOrPartAdmission).specFullDefenceOrPartAdmission1V1(
            this.specFullDefenceOrPartAdmission1V1).specFullDefenceOrPartAdmission(this.specFullDefenceOrPartAdmission).specDisputesOrPartAdmission(
            this.specDisputesOrPartAdmission).submittedDate(this.submittedDate).paymentSuccessfulDate(this.paymentSuccessfulDate).issueDate(
            this.issueDate).claimNotificationDeadline(this.claimNotificationDeadline).claimNotificationDate(this.claimNotificationDate).claimDetailsNotificationDeadline(
            this.claimDetailsNotificationDeadline).claimDetailsNotificationDate(this.claimDetailsNotificationDate).respondent1ResponseDeadline(
            this.respondent1ResponseDeadline).respondent2ResponseDeadline(this.respondent2ResponseDeadline).claimDismissedDeadline(
            this.claimDismissedDeadline).respondent1TimeExtensionDate(this.respondent1TimeExtensionDate).respondent2TimeExtensionDate(
            this.respondent2TimeExtensionDate).respondent1AcknowledgeNotificationDate(this.respondent1AcknowledgeNotificationDate).respondent2AcknowledgeNotificationDate(
            this.respondent2AcknowledgeNotificationDate).respondent1ResponseDate(this.respondent1ResponseDate).respondent2ResponseDate(
            this.respondent2ResponseDate).applicant1ResponseDeadline(this.applicant1ResponseDeadline).applicant1ResponseDate(
            this.applicant1ResponseDate).applicant2ResponseDate(this.applicant2ResponseDate).takenOfflineDate(this.takenOfflineDate).takenOfflineByStaffDate(
            this.takenOfflineByStaffDate).claimDismissedDate(this.claimDismissedDate).claimAmountBreakupSummaryObject(
            this.claimAmountBreakupSummaryObject).respondent1LitigationFriendDate(this.respondent1LitigationFriendDate).respondent2LitigationFriendDate(
            this.respondent2LitigationFriendDate).respondent1LitigationFriendCreatedDate(this.respondent1LitigationFriendCreatedDate).respondent2LitigationFriendCreatedDate(
            this.respondent2LitigationFriendCreatedDate).caseBundles(this.caseBundles).specDefendant1Debts(this.specDefendant1Debts).specDefendant2Debts(
            this.specDefendant2Debts).specDefendant1SelfEmploymentDetails(this.specDefendant1SelfEmploymentDetails).specDefendant2SelfEmploymentDetails(
            this.specDefendant2SelfEmploymentDetails).detailsOfDirectionDisposal(this.detailsOfDirectionDisposal).detailsOfDirectionTrial(
            this.detailsOfDirectionTrial).hearingSupportRequirementsDJ(this.hearingSupportRequirementsDJ).defendantDetailsSpec(
            this.defendantDetailsSpec).defendantDetails(this.defendantDetails).bothDefendants(this.bothDefendants).partialPaymentAmount(
            this.partialPaymentAmount).partialPayment(this.partialPayment).paymentSetDate(this.paymentSetDate).repaymentSummaryObject(
            this.repaymentSummaryObject).paymentConfirmationDecisionSpec(this.paymentConfirmationDecisionSpec).repaymentDue(
            this.repaymentDue).repaymentSuggestion(this.repaymentSuggestion).currentDatebox(this.currentDatebox).repaymentDate(
            this.repaymentDate).defaultJudgmentDocuments(this.defaultJudgmentDocuments).hearingSelection(this.hearingSelection).paymentTypeSelection(
            this.paymentTypeSelection).repaymentFrequency(this.repaymentFrequency).isRespondent1(this.isRespondent1).isRespondent2(
            this.isRespondent2).isApplicant1(this.isApplicant1).claimStarted(this.claimStarted).caseDataExtension(this.caseDataExtension);
    }

    public static class CaseDataBuilder {
        private Long ccdCaseReference;
        private CaseState ccdState;
        private GAApplicationType generalAppType;
        private GARespondentOrderAgreement generalAppRespondentAgreement;
        private GAPbaDetails generalAppPBADetails;
        private String generalAppDetailsOfOrder;
        private String generalAppReasonsOfOrder;
        private GAInformOtherParty generalAppInformOtherParty;
        private GAUrgencyRequirement generalAppUrgencyRequirement;
        private GAStatementOfTruth generalAppStatementOfTruth;
        private GAHearingDetails generalAppHearingDetails;
        private GASolicitorDetailsGAspec generalAppApplnSolicitor;
        private List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors;
        private List<Element<Document>> generalAppEvidenceDocument;
        private List<Element<GeneralApplication>> generalApplications;
        private List<Element<GeneralApplicationsDetails>> generalApplicationsDetails;
        private SolicitorReferences solicitorReferences;
        private SolicitorReferences solicitorReferencesCopy;
        private String respondentSolicitor2Reference;
        private CourtLocation courtLocation;
        private Party applicant1;
        private Party applicant2;
        private CorrectEmail applicantSolicitor1CheckEmail;
        private IdamUserDetails applicantSolicitor1UserDetails;
        private YesOrNo addApplicant2;
        private YesOrNo addRespondent2;
        private YesOrNo respondent2SameLegalRepresentative;
        private Party respondent1;
        private Party respondent1Copy;
        private Party respondent2;
        private Party respondent2Copy;
        private Party respondent1DetailsForClaimDetailsTab;
        private Party respondent2DetailsForClaimDetailsTab;
        private YesOrNo respondent1Represented;
        private YesOrNo respondent2Represented;
        private YesOrNo respondent1OrgRegistered;
        private YesOrNo respondent2OrgRegistered;
        private String respondentSolicitor1EmailAddress;
        private String respondentSolicitor2EmailAddress;
        private YesOrNo uploadParticularsOfClaim;
        private String detailsOfClaim;
        private ClaimValue claimValue;
        private Fee claimFee;
        private String paymentReference;
        private DynamicList applicantSolicitor1PbaAccounts;
        private ClaimType claimType;
        private SuperClaimType superClaimType;
        private String claimTypeOther;
        private PersonalInjuryType personalInjuryType;
        private String personalInjuryTypeOther;
        private StatementOfTruth applicantSolicitor1ClaimStatementOfTruth;
        private StatementOfTruth uiStatementOfTruth;
        private String legacyCaseReference;
        private AllocatedTrack allocatedTrack;
        private PaymentDetails paymentDetails;
        private PaymentDetails claimIssuedPaymentDetails;
        private OrganisationPolicy applicant1OrganisationPolicy;
        private OrganisationPolicy applicant2OrganisationPolicy;
        private OrganisationPolicy respondent1OrganisationPolicy;
        private OrganisationPolicy respondent2OrganisationPolicy;
        private SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails;
        private SolicitorOrganisationDetails respondentSolicitor2OrganisationDetails;
        private YesOrNo applicantSolicitor1ServiceAddressRequired;
        private Address applicantSolicitor1ServiceAddress;
        private YesOrNo respondentSolicitor1ServiceAddressRequired;
        private Address respondentSolicitor1ServiceAddress;
        private YesOrNo respondentSolicitor2ServiceAddressRequired;
        private Address respondentSolicitor2ServiceAddress;
        private StatementOfTruth applicant1ServiceStatementOfTruthToRespondentSolicitor1;
        private List<Element<CaseDocument>> systemGeneratedCaseDocuments;
        private Document specClaimTemplateDocumentFiles;
        private Document specClaimDetailsDocumentFiles;
        private List<Evidence> speclistYourEvidenceList;
        private YesOrNo specApplicantCorrespondenceAddressRequired;
        private Address specApplicantCorrespondenceAddressdetails;
        private YesOrNo specRespondentCorrespondenceAddressRequired;
        private Address specRespondentCorrespondenceAddressdetails;
        //private YesOrNo specAoSRespondent2HomeAddressRequired;
        //private Address specAoSRespondent2HomeAddressDetails;
        private LocalDate respondentSolicitor1AgreedDeadlineExtension;
        private LocalDate respondentSolicitor2AgreedDeadlineExtension;
        private ResponseIntention respondent1ClaimResponseIntentionType;
        private ResponseIntention respondent2ClaimResponseIntentionType;
        private ResponseIntention respondent1ClaimResponseIntentionTypeApplicant2;
        private ServedDocumentFiles servedDocumentFiles;
        private YesOrNo respondentResponseIsSame;
        private YesOrNo defendantSingleResponseToBothClaimants;
        private RespondentResponseType respondent1ClaimResponseType;
        private RespondentResponseType respondent2ClaimResponseType;
        private RespondentResponseType respondent1ClaimResponseTypeToApplicant2;
        private ResponseDocument respondent1ClaimResponseDocument;
        private ResponseDocument respondent2ClaimResponseDocument;
        private ResponseDocument respondentSharedClaimResponseDocument;
        private CaseDocument respondent1GeneratedResponseDocument;
        private CaseDocument respondent2GeneratedResponseDocument;
        private List<Element<CaseDocument>> defendantResponseDocuments;
        private YesOrNo applicant1ProceedWithClaim;
        private YesOrNo applicant1ProceedWithClaimMultiParty2v1;
        private YesOrNo applicant2ProceedWithClaimMultiParty2v1;
        private YesOrNo applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2;
        private YesOrNo applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2;
        private YesOrNo applicant1ProceedWithClaimRespondent2;
        private ResponseDocument applicant1DefenceResponseDocument;
        private ResponseDocument claimantDefenceResDocToDefendant2;
        private List<Element<CaseDocument>> claimantResponseDocuments;
        private List<ClaimAmountBreakup> claimAmountBreakup;
        private List<TimelineOfEvents> timelineOfEvents;
        private BigDecimal totalClaimAmount;
        private BigDecimal totalInterest;
        private YesOrNo claimInterest;
        private InterestClaimOptions interestClaimOptions;
        private SameRateInterestSelection sameRateInterestSelection;
        private BigDecimal breakDownInterestTotal;
        private String breakDownInterestDescription;
        private InterestClaimFromType interestClaimFrom;
        private InterestClaimUntilType interestClaimUntil;
        private LocalDate interestFromSpecificDate;
        private String interestFromSpecificDateDescription;
        private String calculatedInterest;
        private String specRespondentSolicitor1EmailAddress;
        //private YesOrNo specAoSApplicantCorrespondenceAddressRequired;
        //private Address specAoSApplicantCorrespondenceAddressdetails;
        //private YesOrNo specAoSRespondentCorrespondenceAddressRequired;
        //private Address specAoSRespondentCorrespondenceAddressdetails;
        private YesOrNo specRespondent1Represented;
        private YesOrNo specRespondent2Represented;
        private List<TimelineOfEvents> specResponseTimelineOfEvents;
        private String specClaimResponseTimelineList;
        private ResponseDocument specResponseTimelineDocumentFiles;
        private List<Evidence> specResponselistYourEvidenceList;
        private String detailsOfWhyDoesYouDisputeTheClaim;
        private ResponseDocument respondent1SpecDefenceResponseDocument;
        private RespondentResponseTypeSpec respondent1ClaimResponseTypeForSpec;
        private RespondentResponseTypeSpec respondent2ClaimResponseTypeForSpec;
        private RespondentResponseTypeSpec claimant1ClaimResponseTypeForSpec;
        private RespondentResponseTypeSpec claimant2ClaimResponseTypeForSpec;
        private RespondentResponseTypeSpecPaidStatus respondent1ClaimResponsePaymentAdmissionForSpec;
        private RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteRequired;
        private String defenceRouteRequired;
        private String responseClaimTrack;
        private RespondToClaim respondToClaim;
        private RespondToClaim respondToAdmittedClaim;
        private BigDecimal respondToAdmittedClaimOwingAmount;
        private BigDecimal respondToAdmittedClaimOwingAmountPounds;
        private YesOrNo specDefenceFullAdmittedRequired;
        private PaymentUponCourtOrder respondent1CourtOrderPayment;
        private PaymentUponCourtOrder respondent2CourtOrderPayment;
        private RepaymentPlanLRspec respondent1RepaymentPlan;
        private RepaymentPlanLRspec respondent2RepaymentPlan;
        private RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec;
        private UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec;
        private Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer;
        private Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployerRespondent2;
        private String responseToClaimAdmitPartWhyNotPayLRspec;
        private YesOrNo responseClaimMediationSpecRequired;
        private SmallClaimMedicalLRspec applicant1ClaimMediationSpecRequired;
        private YesOrNo defenceAdmitPartEmploymentTypeRequired;
        private YesOrNo responseClaimExpertSpecRequired;
        private YesOrNo applicant1ClaimExpertSpecRequired;
        private String responseClaimWitnesses;
        private String applicant1ClaimWitnesses;
        private YesOrNo smallClaimHearingInterpreterRequired;
        private String smallClaimHearingInterpreterDescription;
        private List<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspec;
        private YesOrNo specDefenceAdmittedRequired;
        private String additionalInformationForJudge;
        private String applicantAdditionalInformationForJudge;
        private ExpertRequirements respondToClaimExperts;
        private String caseNote;
        private List<Element<CaseNote>> caseNotes;
        private @Valid CloseClaim withdrawClaim;
        private @Valid CloseClaim discontinueClaim;
        private BusinessProcess businessProcess;
        private Respondent1DQ respondent1DQ;
        private Respondent2DQ respondent2DQ;
        private Applicant1DQ applicant1DQ;
        private Applicant2DQ applicant2DQ;
        private LitigationFriend genericLitigationFriend;
        private LitigationFriend respondent1LitigationFriend;
        private LitigationFriend respondent2LitigationFriend;
        private YesOrNo applicant1LitigationFriendRequired;
        private LitigationFriend applicant1LitigationFriend;
        private YesOrNo applicant2LitigationFriendRequired;
        private LitigationFriend applicant2LitigationFriend;
        private DynamicList defendantSolicitorNotifyClaimOptions;
        private DynamicList defendantSolicitorNotifyClaimDetailsOptions;
        private DynamicList selectLitigationFriend;
        private String litigantFriendSelection;
        private @Valid ClaimProceedsInCaseman claimProceedsInCaseman;
        private YesOrNo applicantSolicitor1PbaAccountsIsEmpty;
        private MultiPartyResponseTypeFlags multiPartyResponseTypeFlags;
        private YesOrNo applicantsProceedIntention;
        private MultiPartyScenario claimantResponseScenarioFlag;
        private YesOrNo claimantResponseDocumentToDefendant2Flag;
        private YesOrNo claimant2ResponseFlag;
        private RespondentResponseTypeSpec atLeastOneClaimResponseTypeForSpecIsFullDefence;
        private YesOrNo specFullAdmissionOrPartAdmission;
        private YesOrNo sameSolicitorSameResponse;
        private YesOrNo specPaidLessAmountOrDisputesOrPartAdmission;
        private YesOrNo specFullDefenceOrPartAdmission1V1;
        private YesOrNo specFullDefenceOrPartAdmission;
        private YesOrNo specDisputesOrPartAdmission;
        private LocalDateTime submittedDate;
        private LocalDateTime paymentSuccessfulDate;
        private LocalDate issueDate;
        private LocalDateTime claimNotificationDeadline;
        private LocalDateTime claimNotificationDate;
        private LocalDateTime claimDetailsNotificationDeadline;
        private LocalDateTime claimDetailsNotificationDate;
        private LocalDateTime respondent1ResponseDeadline;
        private LocalDateTime respondent2ResponseDeadline;
        private LocalDateTime claimDismissedDeadline;
        private LocalDateTime respondent1TimeExtensionDate;
        private LocalDateTime respondent2TimeExtensionDate;
        private LocalDateTime respondent1AcknowledgeNotificationDate;
        private LocalDateTime respondent2AcknowledgeNotificationDate;
        private LocalDateTime respondent1ResponseDate;
        private LocalDateTime respondent2ResponseDate;
        private LocalDateTime applicant1ResponseDeadline;
        private LocalDateTime applicant1ResponseDate;
        private LocalDateTime applicant2ResponseDate;
        private LocalDateTime takenOfflineDate;
        private LocalDateTime takenOfflineByStaffDate;
        private LocalDateTime claimDismissedDate;
        private String claimAmountBreakupSummaryObject;
        private LocalDateTime respondent1LitigationFriendDate;
        private LocalDateTime respondent2LitigationFriendDate;
        private LocalDateTime respondent1LitigationFriendCreatedDate;
        private LocalDateTime respondent2LitigationFriendCreatedDate;
        private List<IdValue<Bundle>> caseBundles;
        private Respondent1DebtLRspec specDefendant1Debts;
        private Respondent1DebtLRspec specDefendant2Debts;
        private Respondent1SelfEmploymentLRspec specDefendant1SelfEmploymentDetails;
        private Respondent1SelfEmploymentLRspec specDefendant2SelfEmploymentDetails;
        private String detailsOfDirectionDisposal;
        private String detailsOfDirectionTrial;
        private HearingSupportRequirementsDJ hearingSupportRequirementsDJ;
        private DynamicList defendantDetailsSpec;
        private DynamicList defendantDetails;
        private String bothDefendants;
        private String partialPaymentAmount;
        private YesOrNo partialPayment;
        private LocalDate paymentSetDate;
        private String repaymentSummaryObject;
        private YesOrNo paymentConfirmationDecisionSpec;
        private String repaymentDue;
        private String repaymentSuggestion;
        private String currentDatebox;
        private LocalDate repaymentDate;
        private List<Element<CaseDocument>> defaultJudgmentDocuments;
        private String hearingSelection;
        private DJPaymentTypeSelection paymentTypeSelection;
        private RepaymentFrequencyDJ repaymentFrequency;
        private YesOrNo isRespondent1;
        private YesOrNo isRespondent2;
        private YesOrNo isApplicant1;
        private YesOrNo claimStarted;
        private CaseDataExtension caseDataExtension;

        CaseDataBuilder() {
        }

        public CaseDataBuilder ccdCaseReference(Long ccdCaseReference) {
            this.ccdCaseReference = ccdCaseReference;
            return this;
        }

        public CaseDataBuilder ccdState(CaseState ccdState) {
            this.ccdState = ccdState;
            return this;
        }

        public CaseDataBuilder generalAppType(GAApplicationType generalAppType) {
            this.generalAppType = generalAppType;
            return this;
        }

        public CaseDataBuilder generalAppRespondentAgreement(GARespondentOrderAgreement generalAppRespondentAgreement) {
            this.generalAppRespondentAgreement = generalAppRespondentAgreement;
            return this;
        }

        public CaseDataBuilder generalAppPBADetails(GAPbaDetails generalAppPBADetails) {
            this.generalAppPBADetails = generalAppPBADetails;
            return this;
        }

        public CaseDataBuilder generalAppDetailsOfOrder(String generalAppDetailsOfOrder) {
            this.generalAppDetailsOfOrder = generalAppDetailsOfOrder;
            return this;
        }

        public CaseDataBuilder generalAppReasonsOfOrder(String generalAppReasonsOfOrder) {
            this.generalAppReasonsOfOrder = generalAppReasonsOfOrder;
            return this;
        }

        public CaseDataBuilder generalAppInformOtherParty(GAInformOtherParty generalAppInformOtherParty) {
            this.generalAppInformOtherParty = generalAppInformOtherParty;
            return this;
        }

        public CaseDataBuilder generalAppUrgencyRequirement(GAUrgencyRequirement generalAppUrgencyRequirement) {
            this.generalAppUrgencyRequirement = generalAppUrgencyRequirement;
            return this;
        }

        public CaseDataBuilder generalAppStatementOfTruth(GAStatementOfTruth generalAppStatementOfTruth) {
            this.generalAppStatementOfTruth = generalAppStatementOfTruth;
            return this;
        }

        public CaseDataBuilder generalAppHearingDetails(GAHearingDetails generalAppHearingDetails) {
            this.generalAppHearingDetails = generalAppHearingDetails;
            return this;
        }

        public CaseDataBuilder generalAppApplnSolicitor(GASolicitorDetailsGAspec generalAppApplnSolicitor) {
            this.generalAppApplnSolicitor = generalAppApplnSolicitor;
            return this;
        }

        public CaseDataBuilder generalAppRespondentSolicitors(List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors) {
            this.generalAppRespondentSolicitors = generalAppRespondentSolicitors;
            return this;
        }

        public CaseDataBuilder generalAppEvidenceDocument(List<Element<Document>> generalAppEvidenceDocument) {
            this.generalAppEvidenceDocument = generalAppEvidenceDocument;
            return this;
        }

        public CaseDataBuilder generalApplications(List<Element<GeneralApplication>> generalApplications) {
            this.generalApplications = generalApplications;
            return this;
        }

        public CaseDataBuilder generalApplicationsDetails(List<Element<GeneralApplicationsDetails>> generalApplicationsDetails) {
            this.generalApplicationsDetails = generalApplicationsDetails;
            return this;
        }

        public CaseDataBuilder solicitorReferences(SolicitorReferences solicitorReferences) {
            this.solicitorReferences = solicitorReferences;
            return this;
        }

        public CaseDataBuilder solicitorReferencesCopy(SolicitorReferences solicitorReferencesCopy) {
            this.solicitorReferencesCopy = solicitorReferencesCopy;
            return this;
        }

        public CaseDataBuilder respondentSolicitor2Reference(String respondentSolicitor2Reference) {
            this.respondentSolicitor2Reference = respondentSolicitor2Reference;
            return this;
        }

        public CaseDataBuilder courtLocation(CourtLocation courtLocation) {
            this.courtLocation = courtLocation;
            return this;
        }

        public CaseDataBuilder applicant1(Party applicant1) {
            this.applicant1 = applicant1;
            return this;
        }

        public CaseDataBuilder applicant2(Party applicant2) {
            this.applicant2 = applicant2;
            return this;
        }

        public CaseDataBuilder applicantSolicitor1CheckEmail(CorrectEmail applicantSolicitor1CheckEmail) {
            this.applicantSolicitor1CheckEmail = applicantSolicitor1CheckEmail;
            return this;
        }

        public CaseDataBuilder applicantSolicitor1UserDetails(IdamUserDetails applicantSolicitor1UserDetails) {
            this.applicantSolicitor1UserDetails = applicantSolicitor1UserDetails;
            return this;
        }

        public CaseDataBuilder addApplicant2(YesOrNo addApplicant2) {
            this.addApplicant2 = addApplicant2;
            return this;
        }

        public CaseDataBuilder addRespondent2(YesOrNo addRespondent2) {
            this.addRespondent2 = addRespondent2;
            return this;
        }

        public CaseDataBuilder respondent2SameLegalRepresentative(YesOrNo respondent2SameLegalRepresentative) {
            this.respondent2SameLegalRepresentative = respondent2SameLegalRepresentative;
            return this;
        }

        public CaseDataBuilder respondent1(Party respondent1) {
            this.respondent1 = respondent1;
            return this;
        }

        public CaseDataBuilder respondent1Copy(Party respondent1Copy) {
            this.respondent1Copy = respondent1Copy;
            return this;
        }

        public CaseDataBuilder respondent2(Party respondent2) {
            this.respondent2 = respondent2;
            return this;
        }

        public CaseDataBuilder respondent2Copy(Party respondent2Copy) {
            this.respondent2Copy = respondent2Copy;
            return this;
        }

        public CaseDataBuilder respondent1DetailsForClaimDetailsTab(Party respondent1DetailsForClaimDetailsTab) {
            this.respondent1DetailsForClaimDetailsTab = respondent1DetailsForClaimDetailsTab;
            return this;
        }

        public CaseDataBuilder respondent2DetailsForClaimDetailsTab(Party respondent2DetailsForClaimDetailsTab) {
            this.respondent2DetailsForClaimDetailsTab = respondent2DetailsForClaimDetailsTab;
            return this;
        }

        public CaseDataBuilder respondent1Represented(YesOrNo respondent1Represented) {
            this.respondent1Represented = respondent1Represented;
            return this;
        }

        public CaseDataBuilder respondent2Represented(YesOrNo respondent2Represented) {
            this.respondent2Represented = respondent2Represented;
            return this;
        }

        public CaseDataBuilder respondent1OrgRegistered(YesOrNo respondent1OrgRegistered) {
            this.respondent1OrgRegistered = respondent1OrgRegistered;
            return this;
        }

        public CaseDataBuilder respondent2OrgRegistered(YesOrNo respondent2OrgRegistered) {
            this.respondent2OrgRegistered = respondent2OrgRegistered;
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

        public CaseDataBuilder uploadParticularsOfClaim(YesOrNo uploadParticularsOfClaim) {
            this.uploadParticularsOfClaim = uploadParticularsOfClaim;
            return this;
        }

        public CaseDataBuilder detailsOfClaim(String detailsOfClaim) {
            this.detailsOfClaim = detailsOfClaim;
            return this;
        }

        public CaseDataBuilder claimValue(ClaimValue claimValue) {
            this.claimValue = claimValue;
            return this;
        }

        public CaseDataBuilder claimFee(Fee claimFee) {
            this.claimFee = claimFee;
            return this;
        }

        public CaseDataBuilder paymentReference(String paymentReference) {
            this.paymentReference = paymentReference;
            return this;
        }

        public CaseDataBuilder applicantSolicitor1PbaAccounts(DynamicList applicantSolicitor1PbaAccounts) {
            this.applicantSolicitor1PbaAccounts = applicantSolicitor1PbaAccounts;
            return this;
        }

        public CaseDataBuilder claimType(ClaimType claimType) {
            this.claimType = claimType;
            return this;
        }

        public CaseDataBuilder superClaimType(SuperClaimType superClaimType) {
            this.superClaimType = superClaimType;
            return this;
        }

        public CaseDataBuilder claimTypeOther(String claimTypeOther) {
            this.claimTypeOther = claimTypeOther;
            return this;
        }

        public CaseDataBuilder personalInjuryType(PersonalInjuryType personalInjuryType) {
            this.personalInjuryType = personalInjuryType;
            return this;
        }

        public CaseDataBuilder personalInjuryTypeOther(String personalInjuryTypeOther) {
            this.personalInjuryTypeOther = personalInjuryTypeOther;
            return this;
        }

        public CaseDataBuilder applicantSolicitor1ClaimStatementOfTruth(StatementOfTruth applicantSolicitor1ClaimStatementOfTruth) {
            this.applicantSolicitor1ClaimStatementOfTruth = applicantSolicitor1ClaimStatementOfTruth;
            return this;
        }

        public CaseDataBuilder uiStatementOfTruth(StatementOfTruth uiStatementOfTruth) {
            this.uiStatementOfTruth = uiStatementOfTruth;
            return this;
        }

        public CaseDataBuilder legacyCaseReference(String legacyCaseReference) {
            this.legacyCaseReference = legacyCaseReference;
            return this;
        }

        public CaseDataBuilder allocatedTrack(AllocatedTrack allocatedTrack) {
            this.allocatedTrack = allocatedTrack;
            return this;
        }

        public CaseDataBuilder paymentDetails(PaymentDetails paymentDetails) {
            this.paymentDetails = paymentDetails;
            return this;
        }

        public CaseDataBuilder claimIssuedPaymentDetails(PaymentDetails claimIssuedPaymentDetails) {
            this.claimIssuedPaymentDetails = claimIssuedPaymentDetails;
            return this;
        }

        public CaseDataBuilder applicant1OrganisationPolicy(OrganisationPolicy applicant1OrganisationPolicy) {
            this.applicant1OrganisationPolicy = applicant1OrganisationPolicy;
            return this;
        }

        public CaseDataBuilder applicant2OrganisationPolicy(OrganisationPolicy applicant2OrganisationPolicy) {
            this.applicant2OrganisationPolicy = applicant2OrganisationPolicy;
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

        public CaseDataBuilder respondentSolicitor1OrganisationDetails(SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails) {
            this.respondentSolicitor1OrganisationDetails = respondentSolicitor1OrganisationDetails;
            return this;
        }

        public CaseDataBuilder respondentSolicitor2OrganisationDetails(SolicitorOrganisationDetails respondentSolicitor2OrganisationDetails) {
            this.respondentSolicitor2OrganisationDetails = respondentSolicitor2OrganisationDetails;
            return this;
        }

        public CaseDataBuilder applicantSolicitor1ServiceAddressRequired(YesOrNo applicantSolicitor1ServiceAddressRequired) {
            this.applicantSolicitor1ServiceAddressRequired = applicantSolicitor1ServiceAddressRequired;
            return this;
        }

        public CaseDataBuilder applicantSolicitor1ServiceAddress(Address applicantSolicitor1ServiceAddress) {
            this.applicantSolicitor1ServiceAddress = applicantSolicitor1ServiceAddress;
            return this;
        }

        public CaseDataBuilder respondentSolicitor1ServiceAddressRequired(YesOrNo respondentSolicitor1ServiceAddressRequired) {
            this.respondentSolicitor1ServiceAddressRequired = respondentSolicitor1ServiceAddressRequired;
            return this;
        }

        public CaseDataBuilder respondentSolicitor1ServiceAddress(Address respondentSolicitor1ServiceAddress) {
            this.respondentSolicitor1ServiceAddress = respondentSolicitor1ServiceAddress;
            return this;
        }

        public CaseDataBuilder respondentSolicitor2ServiceAddressRequired(YesOrNo respondentSolicitor2ServiceAddressRequired) {
            this.respondentSolicitor2ServiceAddressRequired = respondentSolicitor2ServiceAddressRequired;
            return this;
        }

        public CaseDataBuilder respondentSolicitor2ServiceAddress(Address respondentSolicitor2ServiceAddress) {
            this.respondentSolicitor2ServiceAddress = respondentSolicitor2ServiceAddress;
            return this;
        }

        public CaseDataBuilder applicant1ServiceStatementOfTruthToRespondentSolicitor1(StatementOfTruth applicant1ServiceStatementOfTruthToRespondentSolicitor1) {
            this.applicant1ServiceStatementOfTruthToRespondentSolicitor1 = applicant1ServiceStatementOfTruthToRespondentSolicitor1;
            return this;
        }

        public CaseDataBuilder systemGeneratedCaseDocuments(List<Element<CaseDocument>> systemGeneratedCaseDocuments) {
            this.systemGeneratedCaseDocuments = systemGeneratedCaseDocuments;
            return this;
        }

        public CaseDataBuilder specClaimTemplateDocumentFiles(Document specClaimTemplateDocumentFiles) {
            this.specClaimTemplateDocumentFiles = specClaimTemplateDocumentFiles;
            return this;
        }

        public CaseDataBuilder specClaimDetailsDocumentFiles(Document specClaimDetailsDocumentFiles) {
            this.specClaimDetailsDocumentFiles = specClaimDetailsDocumentFiles;
            return this;
        }

        public CaseDataBuilder speclistYourEvidenceList(List<Evidence> speclistYourEvidenceList) {
            this.speclistYourEvidenceList = speclistYourEvidenceList;
            return this;
        }

        public CaseDataBuilder specApplicantCorrespondenceAddressRequired(YesOrNo specApplicantCorrespondenceAddressRequired) {
            this.specApplicantCorrespondenceAddressRequired = specApplicantCorrespondenceAddressRequired;
            return this;
        }

        public CaseDataBuilder specApplicantCorrespondenceAddressdetails(Address specApplicantCorrespondenceAddressdetails) {
            this.specApplicantCorrespondenceAddressdetails = specApplicantCorrespondenceAddressdetails;
            return this;
        }

        public CaseDataBuilder specRespondentCorrespondenceAddressRequired(YesOrNo specRespondentCorrespondenceAddressRequired) {
            this.specRespondentCorrespondenceAddressRequired = specRespondentCorrespondenceAddressRequired;
            return this;
        }

        public CaseDataBuilder specRespondentCorrespondenceAddressdetails(Address specRespondentCorrespondenceAddressdetails) {
            this.specRespondentCorrespondenceAddressdetails = specRespondentCorrespondenceAddressdetails;
            return this;
        }

        /*  public CaseDataBuilder specAoSRespondent2HomeAddressRequired(YesOrNo specAoSRespondent2HomeAddressRequired) {
            this.specAoSRespondent2HomeAddressRequired = specAoSRespondent2HomeAddressRequired;
            return this;
        }

        public CaseDataBuilder specAoSRespondent2HomeAddressDetails(Address specAoSRespondent2HomeAddressDetails) {
            this.specAoSRespondent2HomeAddressDetails = specAoSRespondent2HomeAddressDetails;
            return this;
        }*/

        public CaseDataBuilder respondentSolicitor1AgreedDeadlineExtension(LocalDate respondentSolicitor1AgreedDeadlineExtension) {
            this.respondentSolicitor1AgreedDeadlineExtension = respondentSolicitor1AgreedDeadlineExtension;
            return this;
        }

        public CaseDataBuilder respondentSolicitor2AgreedDeadlineExtension(LocalDate respondentSolicitor2AgreedDeadlineExtension) {
            this.respondentSolicitor2AgreedDeadlineExtension = respondentSolicitor2AgreedDeadlineExtension;
            return this;
        }

        public CaseDataBuilder respondent1ClaimResponseIntentionType(ResponseIntention respondent1ClaimResponseIntentionType) {
            this.respondent1ClaimResponseIntentionType = respondent1ClaimResponseIntentionType;
            return this;
        }

        public CaseDataBuilder respondent2ClaimResponseIntentionType(ResponseIntention respondent2ClaimResponseIntentionType) {
            this.respondent2ClaimResponseIntentionType = respondent2ClaimResponseIntentionType;
            return this;
        }

        public CaseDataBuilder respondent1ClaimResponseIntentionTypeApplicant2(ResponseIntention respondent1ClaimResponseIntentionTypeApplicant2) {
            this.respondent1ClaimResponseIntentionTypeApplicant2 = respondent1ClaimResponseIntentionTypeApplicant2;
            return this;
        }

        public CaseDataBuilder servedDocumentFiles(ServedDocumentFiles servedDocumentFiles) {
            this.servedDocumentFiles = servedDocumentFiles;
            return this;
        }

        public CaseDataBuilder respondentResponseIsSame(YesOrNo respondentResponseIsSame) {
            this.respondentResponseIsSame = respondentResponseIsSame;
            return this;
        }

        public CaseDataBuilder defendantSingleResponseToBothClaimants(YesOrNo defendantSingleResponseToBothClaimants) {
            this.defendantSingleResponseToBothClaimants = defendantSingleResponseToBothClaimants;
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

        public CaseDataBuilder respondent1ClaimResponseTypeToApplicant2(RespondentResponseType respondent1ClaimResponseTypeToApplicant2) {
            this.respondent1ClaimResponseTypeToApplicant2 = respondent1ClaimResponseTypeToApplicant2;
            return this;
        }

        public CaseDataBuilder respondent1ClaimResponseDocument(ResponseDocument respondent1ClaimResponseDocument) {
            this.respondent1ClaimResponseDocument = respondent1ClaimResponseDocument;
            return this;
        }

        public CaseDataBuilder respondent2ClaimResponseDocument(ResponseDocument respondent2ClaimResponseDocument) {
            this.respondent2ClaimResponseDocument = respondent2ClaimResponseDocument;
            return this;
        }

        public CaseDataBuilder respondentSharedClaimResponseDocument(ResponseDocument respondentSharedClaimResponseDocument) {
            this.respondentSharedClaimResponseDocument = respondentSharedClaimResponseDocument;
            return this;
        }

        public CaseDataBuilder respondent1GeneratedResponseDocument(CaseDocument respondent1GeneratedResponseDocument) {
            this.respondent1GeneratedResponseDocument = respondent1GeneratedResponseDocument;
            return this;
        }

        public CaseDataBuilder respondent2GeneratedResponseDocument(CaseDocument respondent2GeneratedResponseDocument) {
            this.respondent2GeneratedResponseDocument = respondent2GeneratedResponseDocument;
            return this;
        }

        public CaseDataBuilder defendantResponseDocuments(List<Element<CaseDocument>> defendantResponseDocuments) {
            this.defendantResponseDocuments = defendantResponseDocuments;
            return this;
        }

        public CaseDataBuilder applicant1ProceedWithClaim(YesOrNo applicant1ProceedWithClaim) {
            this.applicant1ProceedWithClaim = applicant1ProceedWithClaim;
            return this;
        }

        public CaseDataBuilder applicant1ProceedWithClaimMultiParty2v1(YesOrNo applicant1ProceedWithClaimMultiParty2v1) {
            this.applicant1ProceedWithClaimMultiParty2v1 = applicant1ProceedWithClaimMultiParty2v1;
            return this;
        }

        public CaseDataBuilder applicant2ProceedWithClaimMultiParty2v1(YesOrNo applicant2ProceedWithClaimMultiParty2v1) {
            this.applicant2ProceedWithClaimMultiParty2v1 = applicant2ProceedWithClaimMultiParty2v1;
            return this;
        }

        public CaseDataBuilder applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2) {
            this.applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2;
            return this;
        }

        public CaseDataBuilder applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2) {
            this.applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2;
            return this;
        }

        public CaseDataBuilder applicant1ProceedWithClaimRespondent2(YesOrNo applicant1ProceedWithClaimRespondent2) {
            this.applicant1ProceedWithClaimRespondent2 = applicant1ProceedWithClaimRespondent2;
            return this;
        }

        public CaseDataBuilder applicant1DefenceResponseDocument(ResponseDocument applicant1DefenceResponseDocument) {
            this.applicant1DefenceResponseDocument = applicant1DefenceResponseDocument;
            return this;
        }

        public CaseDataBuilder claimantDefenceResDocToDefendant2(ResponseDocument claimantDefenceResDocToDefendant2) {
            this.claimantDefenceResDocToDefendant2 = claimantDefenceResDocToDefendant2;
            return this;
        }

        public CaseDataBuilder claimantResponseDocuments(List<Element<CaseDocument>> claimantResponseDocuments) {
            this.claimantResponseDocuments = claimantResponseDocuments;
            return this;
        }

        public CaseDataBuilder claimAmountBreakup(List<ClaimAmountBreakup> claimAmountBreakup) {
            this.claimAmountBreakup = claimAmountBreakup;
            return this;
        }

        public CaseDataBuilder timelineOfEvents(List<TimelineOfEvents> timelineOfEvents) {
            this.timelineOfEvents = timelineOfEvents;
            return this;
        }

        public CaseDataBuilder totalClaimAmount(BigDecimal totalClaimAmount) {
            this.totalClaimAmount = totalClaimAmount;
            return this;
        }

        public CaseDataBuilder totalInterest(BigDecimal totalInterest) {
            this.totalInterest = totalInterest;
            return this;
        }

        public CaseDataBuilder claimInterest(YesOrNo claimInterest) {
            this.claimInterest = claimInterest;
            return this;
        }

        public CaseDataBuilder interestClaimOptions(InterestClaimOptions interestClaimOptions) {
            this.interestClaimOptions = interestClaimOptions;
            return this;
        }

        public CaseDataBuilder sameRateInterestSelection(SameRateInterestSelection sameRateInterestSelection) {
            this.sameRateInterestSelection = sameRateInterestSelection;
            return this;
        }

        public CaseDataBuilder breakDownInterestTotal(BigDecimal breakDownInterestTotal) {
            this.breakDownInterestTotal = breakDownInterestTotal;
            return this;
        }

        public CaseDataBuilder breakDownInterestDescription(String breakDownInterestDescription) {
            this.breakDownInterestDescription = breakDownInterestDescription;
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

        public CaseDataBuilder interestFromSpecificDate(LocalDate interestFromSpecificDate) {
            this.interestFromSpecificDate = interestFromSpecificDate;
            return this;
        }

        public CaseDataBuilder interestFromSpecificDateDescription(String interestFromSpecificDateDescription) {
            this.interestFromSpecificDateDescription = interestFromSpecificDateDescription;
            return this;
        }

        public CaseDataBuilder calculatedInterest(String calculatedInterest) {
            this.calculatedInterest = calculatedInterest;
            return this;
        }

        public CaseDataBuilder specRespondentSolicitor1EmailAddress(String specRespondentSolicitor1EmailAddress) {
            this.specRespondentSolicitor1EmailAddress = specRespondentSolicitor1EmailAddress;
            return this;
        }

        /*public CaseDataBuilder specAoSApplicantCorrespondenceAddressRequired(YesOrNo specAoSApplicantCorrespondenceAddressRequired) {
            this.specAoSApplicantCorrespondenceAddressRequired = specAoSApplicantCorrespondenceAddressRequired;
            return this;
        }

        public CaseDataBuilder specAoSApplicantCorrespondenceAddressdetails(Address specAoSApplicantCorrespondenceAddressdetails) {
            this.specAoSApplicantCorrespondenceAddressdetails = specAoSApplicantCorrespondenceAddressdetails;
            return this;
        }

        public CaseDataBuilder specAoSRespondentCorrespondenceAddressRequired(YesOrNo specAoSRespondentCorrespondenceAddressRequired) {
            this.specAoSRespondentCorrespondenceAddressRequired = specAoSRespondentCorrespondenceAddressRequired;
            return this;
        }

        public CaseDataBuilder specAoSRespondentCorrespondenceAddressdetails(Address specAoSRespondentCorrespondenceAddressdetails) {
            this.specAoSRespondentCorrespondenceAddressdetails = specAoSRespondentCorrespondenceAddressdetails;
            return this;
        }*/

        public CaseDataBuilder specRespondent1Represented(YesOrNo specRespondent1Represented) {
            this.specRespondent1Represented = specRespondent1Represented;
            return this;
        }

        public CaseDataBuilder specRespondent2Represented(YesOrNo specRespondent2Represented) {
            this.specRespondent2Represented = specRespondent2Represented;
            return this;
        }

        public CaseDataBuilder specResponseTimelineOfEvents(List<TimelineOfEvents> specResponseTimelineOfEvents) {
            this.specResponseTimelineOfEvents = specResponseTimelineOfEvents;
            return this;
        }

        public CaseDataBuilder specClaimResponseTimelineList(String specClaimResponseTimelineList) {
            this.specClaimResponseTimelineList = specClaimResponseTimelineList;
            return this;
        }

        public CaseDataBuilder specResponseTimelineDocumentFiles(ResponseDocument specResponseTimelineDocumentFiles) {
            this.specResponseTimelineDocumentFiles = specResponseTimelineDocumentFiles;
            return this;
        }

        public CaseDataBuilder specResponselistYourEvidenceList(List<Evidence> specResponselistYourEvidenceList) {
            this.specResponselistYourEvidenceList = specResponselistYourEvidenceList;
            return this;
        }

        public CaseDataBuilder detailsOfWhyDoesYouDisputeTheClaim(String detailsOfWhyDoesYouDisputeTheClaim) {
            this.detailsOfWhyDoesYouDisputeTheClaim = detailsOfWhyDoesYouDisputeTheClaim;
            return this;
        }

        public CaseDataBuilder respondent1SpecDefenceResponseDocument(ResponseDocument respondent1SpecDefenceResponseDocument) {
            this.respondent1SpecDefenceResponseDocument = respondent1SpecDefenceResponseDocument;
            return this;
        }

        public CaseDataBuilder respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec respondent1ClaimResponseTypeForSpec) {
            this.respondent1ClaimResponseTypeForSpec = respondent1ClaimResponseTypeForSpec;
            return this;
        }

        public CaseDataBuilder respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec respondent2ClaimResponseTypeForSpec) {
            this.respondent2ClaimResponseTypeForSpec = respondent2ClaimResponseTypeForSpec;
            return this;
        }

        public CaseDataBuilder claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec claimant1ClaimResponseTypeForSpec) {
            this.claimant1ClaimResponseTypeForSpec = claimant1ClaimResponseTypeForSpec;
            return this;
        }

        public CaseDataBuilder claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec claimant2ClaimResponseTypeForSpec) {
            this.claimant2ClaimResponseTypeForSpec = claimant2ClaimResponseTypeForSpec;
            return this;
        }

        public CaseDataBuilder respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus respondent1ClaimResponsePaymentAdmissionForSpec) {
            this.respondent1ClaimResponsePaymentAdmissionForSpec = respondent1ClaimResponsePaymentAdmissionForSpec;
            return this;
        }

        public CaseDataBuilder defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteRequired) {
            this.defenceAdmitPartPaymentTimeRouteRequired = defenceAdmitPartPaymentTimeRouteRequired;
            return this;
        }

        public CaseDataBuilder defenceRouteRequired(String defenceRouteRequired) {
            this.defenceRouteRequired = defenceRouteRequired;
            return this;
        }

        public CaseDataBuilder responseClaimTrack(String responseClaimTrack) {
            this.responseClaimTrack = responseClaimTrack;
            return this;
        }

        public CaseDataBuilder respondToClaim(RespondToClaim respondToClaim) {
            this.respondToClaim = respondToClaim;
            return this;
        }

        public CaseDataBuilder respondToAdmittedClaim(RespondToClaim respondToAdmittedClaim) {
            this.respondToAdmittedClaim = respondToAdmittedClaim;
            return this;
        }

        public CaseDataBuilder respondToAdmittedClaimOwingAmount(BigDecimal respondToAdmittedClaimOwingAmount) {
            this.respondToAdmittedClaimOwingAmount = respondToAdmittedClaimOwingAmount;
            return this;
        }

        public CaseDataBuilder respondToAdmittedClaimOwingAmountPounds(BigDecimal respondToAdmittedClaimOwingAmountPounds) {
            this.respondToAdmittedClaimOwingAmountPounds = respondToAdmittedClaimOwingAmountPounds;
            return this;
        }

        public CaseDataBuilder specDefenceFullAdmittedRequired(YesOrNo specDefenceFullAdmittedRequired) {
            this.specDefenceFullAdmittedRequired = specDefenceFullAdmittedRequired;
            return this;
        }

        public CaseDataBuilder respondent1CourtOrderPayment(PaymentUponCourtOrder respondent1CourtOrderPayment) {
            this.respondent1CourtOrderPayment = respondent1CourtOrderPayment;
            return this;
        }

        public CaseDataBuilder respondent2CourtOrderPayment(PaymentUponCourtOrder respondent2CourtOrderPayment) {
            this.respondent2CourtOrderPayment = respondent2CourtOrderPayment;
            return this;
        }

        public CaseDataBuilder respondent1RepaymentPlan(RepaymentPlanLRspec respondent1RepaymentPlan) {
            this.respondent1RepaymentPlan = respondent1RepaymentPlan;
            return this;
        }

        public CaseDataBuilder respondent2RepaymentPlan(RepaymentPlanLRspec respondent2RepaymentPlan) {
            this.respondent2RepaymentPlan = respondent2RepaymentPlan;
            return this;
        }

        public CaseDataBuilder respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec) {
            this.respondToClaimAdmitPartLRspec = respondToClaimAdmitPartLRspec;
            return this;
        }

        public CaseDataBuilder respondToClaimAdmitPartUnemployedLRspec(UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec) {
            this.respondToClaimAdmitPartUnemployedLRspec = respondToClaimAdmitPartUnemployedLRspec;
            return this;
        }

        public CaseDataBuilder responseClaimAdmitPartEmployer(Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer) {
            this.responseClaimAdmitPartEmployer = responseClaimAdmitPartEmployer;
            return this;
        }

        public CaseDataBuilder responseClaimAdmitPartEmployerRespondent2(Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployerRespondent2) {
            this.responseClaimAdmitPartEmployerRespondent2 = responseClaimAdmitPartEmployerRespondent2;
            return this;
        }

        public CaseDataBuilder responseToClaimAdmitPartWhyNotPayLRspec(String responseToClaimAdmitPartWhyNotPayLRspec) {
            this.responseToClaimAdmitPartWhyNotPayLRspec = responseToClaimAdmitPartWhyNotPayLRspec;
            return this;
        }

        public CaseDataBuilder responseClaimMediationSpecRequired(YesOrNo responseClaimMediationSpecRequired) {
            this.responseClaimMediationSpecRequired = responseClaimMediationSpecRequired;
            return this;
        }

        public CaseDataBuilder applicant1ClaimMediationSpecRequired(SmallClaimMedicalLRspec applicant1ClaimMediationSpecRequired) {
            this.applicant1ClaimMediationSpecRequired = applicant1ClaimMediationSpecRequired;
            return this;
        }

        public CaseDataBuilder defenceAdmitPartEmploymentTypeRequired(YesOrNo defenceAdmitPartEmploymentTypeRequired) {
            this.defenceAdmitPartEmploymentTypeRequired = defenceAdmitPartEmploymentTypeRequired;
            return this;
        }

        public CaseDataBuilder responseClaimExpertSpecRequired(YesOrNo responseClaimExpertSpecRequired) {
            this.responseClaimExpertSpecRequired = responseClaimExpertSpecRequired;
            return this;
        }

        public CaseDataBuilder applicant1ClaimExpertSpecRequired(YesOrNo applicant1ClaimExpertSpecRequired) {
            this.applicant1ClaimExpertSpecRequired = applicant1ClaimExpertSpecRequired;
            return this;
        }

        public CaseDataBuilder responseClaimWitnesses(String responseClaimWitnesses) {
            this.responseClaimWitnesses = responseClaimWitnesses;
            return this;
        }

        public CaseDataBuilder applicant1ClaimWitnesses(String applicant1ClaimWitnesses) {
            this.applicant1ClaimWitnesses = applicant1ClaimWitnesses;
            return this;
        }

        public CaseDataBuilder smallClaimHearingInterpreterRequired(YesOrNo smallClaimHearingInterpreterRequired) {
            this.smallClaimHearingInterpreterRequired = smallClaimHearingInterpreterRequired;
            return this;
        }

        public CaseDataBuilder smallClaimHearingInterpreterDescription(String smallClaimHearingInterpreterDescription) {
            this.smallClaimHearingInterpreterDescription = smallClaimHearingInterpreterDescription;
            return this;
        }

        public CaseDataBuilder respondToClaimAdmitPartEmploymentTypeLRspec(List<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspec) {
            this.respondToClaimAdmitPartEmploymentTypeLRspec = respondToClaimAdmitPartEmploymentTypeLRspec;
            return this;
        }

        public CaseDataBuilder specDefenceAdmittedRequired(YesOrNo specDefenceAdmittedRequired) {
            this.specDefenceAdmittedRequired = specDefenceAdmittedRequired;
            return this;
        }

        public CaseDataBuilder additionalInformationForJudge(String additionalInformationForJudge) {
            this.additionalInformationForJudge = additionalInformationForJudge;
            return this;
        }

        public CaseDataBuilder applicantAdditionalInformationForJudge(String applicantAdditionalInformationForJudge) {
            this.applicantAdditionalInformationForJudge = applicantAdditionalInformationForJudge;
            return this;
        }

        public CaseDataBuilder respondToClaimExperts(ExpertRequirements respondToClaimExperts) {
            this.respondToClaimExperts = respondToClaimExperts;
            return this;
        }

        public CaseDataBuilder caseNote(String caseNote) {
            this.caseNote = caseNote;
            return this;
        }

        public CaseDataBuilder caseNotes(List<Element<CaseNote>> caseNotes) {
            this.caseNotes = caseNotes;
            return this;
        }

        public CaseDataBuilder withdrawClaim(@Valid CloseClaim withdrawClaim) {
            this.withdrawClaim = withdrawClaim;
            return this;
        }

        public CaseDataBuilder discontinueClaim(@Valid CloseClaim discontinueClaim) {
            this.discontinueClaim = discontinueClaim;
            return this;
        }

        public CaseDataBuilder businessProcess(BusinessProcess businessProcess) {
            this.businessProcess = businessProcess;
            return this;
        }

        public CaseDataBuilder respondent1DQ(Respondent1DQ respondent1DQ) {
            this.respondent1DQ = respondent1DQ;
            return this;
        }

        public CaseDataBuilder respondent2DQ(Respondent2DQ respondent2DQ) {
            this.respondent2DQ = respondent2DQ;
            return this;
        }

        public CaseDataBuilder applicant1DQ(Applicant1DQ applicant1DQ) {
            this.applicant1DQ = applicant1DQ;
            return this;
        }

        public CaseDataBuilder applicant2DQ(Applicant2DQ applicant2DQ) {
            this.applicant2DQ = applicant2DQ;
            return this;
        }

        public CaseDataBuilder genericLitigationFriend(LitigationFriend genericLitigationFriend) {
            this.genericLitigationFriend = genericLitigationFriend;
            return this;
        }

        public CaseDataBuilder respondent1LitigationFriend(LitigationFriend respondent1LitigationFriend) {
            this.respondent1LitigationFriend = respondent1LitigationFriend;
            return this;
        }

        public CaseDataBuilder respondent2LitigationFriend(LitigationFriend respondent2LitigationFriend) {
            this.respondent2LitigationFriend = respondent2LitigationFriend;
            return this;
        }

        public CaseDataBuilder applicant1LitigationFriendRequired(YesOrNo applicant1LitigationFriendRequired) {
            this.applicant1LitigationFriendRequired = applicant1LitigationFriendRequired;
            return this;
        }

        public CaseDataBuilder applicant1LitigationFriend(LitigationFriend applicant1LitigationFriend) {
            this.applicant1LitigationFriend = applicant1LitigationFriend;
            return this;
        }

        public CaseDataBuilder applicant2LitigationFriendRequired(YesOrNo applicant2LitigationFriendRequired) {
            this.applicant2LitigationFriendRequired = applicant2LitigationFriendRequired;
            return this;
        }

        public CaseDataBuilder applicant2LitigationFriend(LitigationFriend applicant2LitigationFriend) {
            this.applicant2LitigationFriend = applicant2LitigationFriend;
            return this;
        }

        public CaseDataBuilder defendantSolicitorNotifyClaimOptions(DynamicList defendantSolicitorNotifyClaimOptions) {
            this.defendantSolicitorNotifyClaimOptions = defendantSolicitorNotifyClaimOptions;
            return this;
        }

        public CaseDataBuilder defendantSolicitorNotifyClaimDetailsOptions(DynamicList defendantSolicitorNotifyClaimDetailsOptions) {
            this.defendantSolicitorNotifyClaimDetailsOptions = defendantSolicitorNotifyClaimDetailsOptions;
            return this;
        }

        public CaseDataBuilder selectLitigationFriend(DynamicList selectLitigationFriend) {
            this.selectLitigationFriend = selectLitigationFriend;
            return this;
        }

        public CaseDataBuilder litigantFriendSelection(String litigantFriendSelection) {
            this.litigantFriendSelection = litigantFriendSelection;
            return this;
        }

        public CaseDataBuilder claimProceedsInCaseman(@Valid ClaimProceedsInCaseman claimProceedsInCaseman) {
            this.claimProceedsInCaseman = claimProceedsInCaseman;
            return this;
        }

        public CaseDataBuilder applicantSolicitor1PbaAccountsIsEmpty(YesOrNo applicantSolicitor1PbaAccountsIsEmpty) {
            this.applicantSolicitor1PbaAccountsIsEmpty = applicantSolicitor1PbaAccountsIsEmpty;
            return this;
        }

        public CaseDataBuilder multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags multiPartyResponseTypeFlags) {
            this.multiPartyResponseTypeFlags = multiPartyResponseTypeFlags;
            return this;
        }

        public CaseDataBuilder applicantsProceedIntention(YesOrNo applicantsProceedIntention) {
            this.applicantsProceedIntention = applicantsProceedIntention;
            return this;
        }

        public CaseDataBuilder claimantResponseScenarioFlag(MultiPartyScenario claimantResponseScenarioFlag) {
            this.claimantResponseScenarioFlag = claimantResponseScenarioFlag;
            return this;
        }

        public CaseDataBuilder claimantResponseDocumentToDefendant2Flag(YesOrNo claimantResponseDocumentToDefendant2Flag) {
            this.claimantResponseDocumentToDefendant2Flag = claimantResponseDocumentToDefendant2Flag;
            return this;
        }

        public CaseDataBuilder claimant2ResponseFlag(YesOrNo claimant2ResponseFlag) {
            this.claimant2ResponseFlag = claimant2ResponseFlag;
            return this;
        }

        public CaseDataBuilder atLeastOneClaimResponseTypeForSpecIsFullDefence(RespondentResponseTypeSpec atLeastOneClaimResponseTypeForSpecIsFullDefence) {
            this.atLeastOneClaimResponseTypeForSpecIsFullDefence = atLeastOneClaimResponseTypeForSpecIsFullDefence;
            return this;
        }

        public CaseDataBuilder specFullAdmissionOrPartAdmission(YesOrNo specFullAdmissionOrPartAdmission) {
            this.specFullAdmissionOrPartAdmission = specFullAdmissionOrPartAdmission;
            return this;
        }

        public CaseDataBuilder sameSolicitorSameResponse(YesOrNo sameSolicitorSameResponse) {
            this.sameSolicitorSameResponse = sameSolicitorSameResponse;
            return this;
        }

        public CaseDataBuilder specPaidLessAmountOrDisputesOrPartAdmission(YesOrNo specPaidLessAmountOrDisputesOrPartAdmission) {
            this.specPaidLessAmountOrDisputesOrPartAdmission = specPaidLessAmountOrDisputesOrPartAdmission;
            return this;
        }

        public CaseDataBuilder specFullDefenceOrPartAdmission1V1(YesOrNo specFullDefenceOrPartAdmission1V1) {
            this.specFullDefenceOrPartAdmission1V1 = specFullDefenceOrPartAdmission1V1;
            return this;
        }

        public CaseDataBuilder specFullDefenceOrPartAdmission(YesOrNo specFullDefenceOrPartAdmission) {
            this.specFullDefenceOrPartAdmission = specFullDefenceOrPartAdmission;
            return this;
        }

        public CaseDataBuilder specDisputesOrPartAdmission(YesOrNo specDisputesOrPartAdmission) {
            this.specDisputesOrPartAdmission = specDisputesOrPartAdmission;
            return this;
        }

        public CaseDataBuilder submittedDate(LocalDateTime submittedDate) {
            this.submittedDate = submittedDate;
            return this;
        }

        public CaseDataBuilder paymentSuccessfulDate(LocalDateTime paymentSuccessfulDate) {
            this.paymentSuccessfulDate = paymentSuccessfulDate;
            return this;
        }

        public CaseDataBuilder issueDate(LocalDate issueDate) {
            this.issueDate = issueDate;
            return this;
        }

        public CaseDataBuilder claimNotificationDeadline(LocalDateTime claimNotificationDeadline) {
            this.claimNotificationDeadline = claimNotificationDeadline;
            return this;
        }

        public CaseDataBuilder claimNotificationDate(LocalDateTime claimNotificationDate) {
            this.claimNotificationDate = claimNotificationDate;
            return this;
        }

        public CaseDataBuilder claimDetailsNotificationDeadline(LocalDateTime claimDetailsNotificationDeadline) {
            this.claimDetailsNotificationDeadline = claimDetailsNotificationDeadline;
            return this;
        }

        public CaseDataBuilder claimDetailsNotificationDate(LocalDateTime claimDetailsNotificationDate) {
            this.claimDetailsNotificationDate = claimDetailsNotificationDate;
            return this;
        }

        public CaseDataBuilder respondent1ResponseDeadline(LocalDateTime respondent1ResponseDeadline) {
            this.respondent1ResponseDeadline = respondent1ResponseDeadline;
            return this;
        }

        public CaseDataBuilder respondent2ResponseDeadline(LocalDateTime respondent2ResponseDeadline) {
            this.respondent2ResponseDeadline = respondent2ResponseDeadline;
            return this;
        }

        public CaseDataBuilder claimDismissedDeadline(LocalDateTime claimDismissedDeadline) {
            this.claimDismissedDeadline = claimDismissedDeadline;
            return this;
        }

        public CaseDataBuilder respondent1TimeExtensionDate(LocalDateTime respondent1TimeExtensionDate) {
            this.respondent1TimeExtensionDate = respondent1TimeExtensionDate;
            return this;
        }

        public CaseDataBuilder respondent2TimeExtensionDate(LocalDateTime respondent2TimeExtensionDate) {
            this.respondent2TimeExtensionDate = respondent2TimeExtensionDate;
            return this;
        }

        public CaseDataBuilder respondent1AcknowledgeNotificationDate(LocalDateTime respondent1AcknowledgeNotificationDate) {
            this.respondent1AcknowledgeNotificationDate = respondent1AcknowledgeNotificationDate;
            return this;
        }

        public CaseDataBuilder respondent2AcknowledgeNotificationDate(LocalDateTime respondent2AcknowledgeNotificationDate) {
            this.respondent2AcknowledgeNotificationDate = respondent2AcknowledgeNotificationDate;
            return this;
        }

        public CaseDataBuilder respondent1ResponseDate(LocalDateTime respondent1ResponseDate) {
            this.respondent1ResponseDate = respondent1ResponseDate;
            return this;
        }

        public CaseDataBuilder respondent2ResponseDate(LocalDateTime respondent2ResponseDate) {
            this.respondent2ResponseDate = respondent2ResponseDate;
            return this;
        }

        public CaseDataBuilder applicant1ResponseDeadline(LocalDateTime applicant1ResponseDeadline) {
            this.applicant1ResponseDeadline = applicant1ResponseDeadline;
            return this;
        }

        public CaseDataBuilder applicant1ResponseDate(LocalDateTime applicant1ResponseDate) {
            this.applicant1ResponseDate = applicant1ResponseDate;
            return this;
        }

        public CaseDataBuilder applicant2ResponseDate(LocalDateTime applicant2ResponseDate) {
            this.applicant2ResponseDate = applicant2ResponseDate;
            return this;
        }

        public CaseDataBuilder takenOfflineDate(LocalDateTime takenOfflineDate) {
            this.takenOfflineDate = takenOfflineDate;
            return this;
        }

        public CaseDataBuilder takenOfflineByStaffDate(LocalDateTime takenOfflineByStaffDate) {
            this.takenOfflineByStaffDate = takenOfflineByStaffDate;
            return this;
        }

        public CaseDataBuilder claimDismissedDate(LocalDateTime claimDismissedDate) {
            this.claimDismissedDate = claimDismissedDate;
            return this;
        }

        public CaseDataBuilder claimAmountBreakupSummaryObject(String claimAmountBreakupSummaryObject) {
            this.claimAmountBreakupSummaryObject = claimAmountBreakupSummaryObject;
            return this;
        }

        public CaseDataBuilder respondent1LitigationFriendDate(LocalDateTime respondent1LitigationFriendDate) {
            this.respondent1LitigationFriendDate = respondent1LitigationFriendDate;
            return this;
        }

        public CaseDataBuilder respondent2LitigationFriendDate(LocalDateTime respondent2LitigationFriendDate) {
            this.respondent2LitigationFriendDate = respondent2LitigationFriendDate;
            return this;
        }

        public CaseDataBuilder respondent1LitigationFriendCreatedDate(LocalDateTime respondent1LitigationFriendCreatedDate) {
            this.respondent1LitigationFriendCreatedDate = respondent1LitigationFriendCreatedDate;
            return this;
        }

        public CaseDataBuilder respondent2LitigationFriendCreatedDate(LocalDateTime respondent2LitigationFriendCreatedDate) {
            this.respondent2LitigationFriendCreatedDate = respondent2LitigationFriendCreatedDate;
            return this;
        }

        public CaseDataBuilder caseBundles(List<IdValue<Bundle>> caseBundles) {
            this.caseBundles = caseBundles;
            return this;
        }

        public CaseDataBuilder specDefendant1Debts(Respondent1DebtLRspec specDefendant1Debts) {
            this.specDefendant1Debts = specDefendant1Debts;
            return this;
        }

        public CaseDataBuilder specDefendant2Debts(Respondent1DebtLRspec specDefendant2Debts) {
            this.specDefendant2Debts = specDefendant2Debts;
            return this;
        }

        public CaseDataBuilder specDefendant1SelfEmploymentDetails(Respondent1SelfEmploymentLRspec specDefendant1SelfEmploymentDetails) {
            this.specDefendant1SelfEmploymentDetails = specDefendant1SelfEmploymentDetails;
            return this;
        }

        public CaseDataBuilder specDefendant2SelfEmploymentDetails(Respondent1SelfEmploymentLRspec specDefendant2SelfEmploymentDetails) {
            this.specDefendant2SelfEmploymentDetails = specDefendant2SelfEmploymentDetails;
            return this;
        }

        public CaseDataBuilder detailsOfDirectionDisposal(String detailsOfDirectionDisposal) {
            this.detailsOfDirectionDisposal = detailsOfDirectionDisposal;
            return this;
        }

        public CaseDataBuilder detailsOfDirectionTrial(String detailsOfDirectionTrial) {
            this.detailsOfDirectionTrial = detailsOfDirectionTrial;
            return this;
        }

        public CaseDataBuilder hearingSupportRequirementsDJ(HearingSupportRequirementsDJ hearingSupportRequirementsDJ) {
            this.hearingSupportRequirementsDJ = hearingSupportRequirementsDJ;
            return this;
        }

        public CaseDataBuilder defendantDetailsSpec(DynamicList defendantDetailsSpec) {
            this.defendantDetailsSpec = defendantDetailsSpec;
            return this;
        }

        public CaseDataBuilder defendantDetails(DynamicList defendantDetails) {
            this.defendantDetails = defendantDetails;
            return this;
        }

        public CaseDataBuilder bothDefendants(String bothDefendants) {
            this.bothDefendants = bothDefendants;
            return this;
        }

        public CaseDataBuilder partialPaymentAmount(String partialPaymentAmount) {
            this.partialPaymentAmount = partialPaymentAmount;
            return this;
        }

        public CaseDataBuilder partialPayment(YesOrNo partialPayment) {
            this.partialPayment = partialPayment;
            return this;
        }

        public CaseDataBuilder paymentSetDate(LocalDate paymentSetDate) {
            this.paymentSetDate = paymentSetDate;
            return this;
        }

        public CaseDataBuilder repaymentSummaryObject(String repaymentSummaryObject) {
            this.repaymentSummaryObject = repaymentSummaryObject;
            return this;
        }

        public CaseDataBuilder paymentConfirmationDecisionSpec(YesOrNo paymentConfirmationDecisionSpec) {
            this.paymentConfirmationDecisionSpec = paymentConfirmationDecisionSpec;
            return this;
        }

        public CaseDataBuilder repaymentDue(String repaymentDue) {
            this.repaymentDue = repaymentDue;
            return this;
        }

        public CaseDataBuilder repaymentSuggestion(String repaymentSuggestion) {
            this.repaymentSuggestion = repaymentSuggestion;
            return this;
        }

        public CaseDataBuilder currentDatebox(String currentDatebox) {
            this.currentDatebox = currentDatebox;
            return this;
        }

        public CaseDataBuilder repaymentDate(LocalDate repaymentDate) {
            this.repaymentDate = repaymentDate;
            return this;
        }

        public CaseDataBuilder defaultJudgmentDocuments(List<Element<CaseDocument>> defaultJudgmentDocuments) {
            this.defaultJudgmentDocuments = defaultJudgmentDocuments;
            return this;
        }

        public CaseDataBuilder hearingSelection(String hearingSelection) {
            this.hearingSelection = hearingSelection;
            return this;
        }

        public CaseDataBuilder paymentTypeSelection(DJPaymentTypeSelection paymentTypeSelection) {
            this.paymentTypeSelection = paymentTypeSelection;
            return this;
        }

        public CaseDataBuilder repaymentFrequency(RepaymentFrequencyDJ repaymentFrequency) {
            this.repaymentFrequency = repaymentFrequency;
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

        public CaseDataBuilder isApplicant1(YesOrNo isApplicant1) {
            this.isApplicant1 = isApplicant1;
            return this;
        }

        public CaseDataBuilder claimStarted(YesOrNo claimStarted) {
            this.claimStarted = claimStarted;
            return this;
        }

        public CaseDataBuilder caseDataExtension(CaseDataExtension caseDataExtension) {
            this.caseDataExtension = caseDataExtension;
            return this;
        }

        public CaseData build() {
            return new CaseData(
                ccdCaseReference,
                ccdState,
                generalAppType,
                generalAppRespondentAgreement,
                generalAppPBADetails,
                generalAppDetailsOfOrder,
                generalAppReasonsOfOrder,
                generalAppInformOtherParty,
                generalAppUrgencyRequirement,
                generalAppStatementOfTruth,
                generalAppHearingDetails,
                generalAppApplnSolicitor,
                generalAppRespondentSolicitors,
                generalAppEvidenceDocument,
                generalApplications,
                generalApplicationsDetails,
                solicitorReferences,
                solicitorReferencesCopy,
                respondentSolicitor2Reference,
                courtLocation,
                applicant1,
                applicant2,
                applicantSolicitor1CheckEmail,
                applicantSolicitor1UserDetails,
                addApplicant2,
                addRespondent2,
                respondent2SameLegalRepresentative,
                respondent1,
                respondent1Copy,
                respondent2,
                respondent2Copy,
                respondent1DetailsForClaimDetailsTab,
                respondent2DetailsForClaimDetailsTab,
                respondent1Represented,
                respondent2Represented,
                respondent1OrgRegistered,
                respondent2OrgRegistered,
                respondentSolicitor1EmailAddress,
                respondentSolicitor2EmailAddress,
                uploadParticularsOfClaim,
                detailsOfClaim,
                claimValue,
                claimFee,
                paymentReference,
                applicantSolicitor1PbaAccounts,
                claimType,
                superClaimType,
                claimTypeOther,
                personalInjuryType,
                personalInjuryTypeOther,
                applicantSolicitor1ClaimStatementOfTruth,
                uiStatementOfTruth,
                legacyCaseReference,
                allocatedTrack,
                paymentDetails,
                claimIssuedPaymentDetails,
                applicant1OrganisationPolicy,
                applicant2OrganisationPolicy,
                respondent1OrganisationPolicy,
                respondent2OrganisationPolicy,
                respondentSolicitor1OrganisationDetails,
                respondentSolicitor2OrganisationDetails,
                applicantSolicitor1ServiceAddressRequired,
                applicantSolicitor1ServiceAddress,
                respondentSolicitor1ServiceAddressRequired,
                respondentSolicitor1ServiceAddress,
                respondentSolicitor2ServiceAddressRequired,
                respondentSolicitor2ServiceAddress,
                applicant1ServiceStatementOfTruthToRespondentSolicitor1,
                systemGeneratedCaseDocuments,
                specClaimTemplateDocumentFiles,
                specClaimDetailsDocumentFiles,
                speclistYourEvidenceList,
                specApplicantCorrespondenceAddressRequired,
                specApplicantCorrespondenceAddressdetails,
                specRespondentCorrespondenceAddressRequired,
                specRespondentCorrespondenceAddressdetails,
                respondentSolicitor1AgreedDeadlineExtension,
                respondentSolicitor2AgreedDeadlineExtension,
                respondent1ClaimResponseIntentionType,
                respondent2ClaimResponseIntentionType,
                respondent1ClaimResponseIntentionTypeApplicant2,
                servedDocumentFiles,
                respondentResponseIsSame,
                defendantSingleResponseToBothClaimants,
                respondent1ClaimResponseType,
                respondent2ClaimResponseType,
                respondent1ClaimResponseTypeToApplicant2,
                respondent1ClaimResponseDocument,
                respondent2ClaimResponseDocument,
                respondentSharedClaimResponseDocument,
                respondent1GeneratedResponseDocument,
                respondent2GeneratedResponseDocument,
                defendantResponseDocuments,
                applicant1ProceedWithClaim,
                applicant1ProceedWithClaimMultiParty2v1,
                applicant2ProceedWithClaimMultiParty2v1,
                applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2,
                applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2,
                applicant1ProceedWithClaimRespondent2,
                applicant1DefenceResponseDocument,
                claimantDefenceResDocToDefendant2,
                claimantResponseDocuments,
                claimAmountBreakup,
                timelineOfEvents,
                totalClaimAmount,
                totalInterest,
                claimInterest,
                interestClaimOptions,
                sameRateInterestSelection,
                breakDownInterestTotal,
                breakDownInterestDescription,
                interestClaimFrom,
                interestClaimUntil,
                interestFromSpecificDate,
                interestFromSpecificDateDescription,
                calculatedInterest,
                specRespondentSolicitor1EmailAddress,
                specRespondent1Represented,
                specRespondent2Represented,
                specResponseTimelineOfEvents,
                specClaimResponseTimelineList,
                specResponseTimelineDocumentFiles,
                specResponselistYourEvidenceList,
                detailsOfWhyDoesYouDisputeTheClaim,
                respondent1SpecDefenceResponseDocument,
                respondent1ClaimResponseTypeForSpec,
                respondent2ClaimResponseTypeForSpec,
                claimant1ClaimResponseTypeForSpec,
                claimant2ClaimResponseTypeForSpec,
                respondent1ClaimResponsePaymentAdmissionForSpec,
                defenceAdmitPartPaymentTimeRouteRequired,
                defenceRouteRequired,
                responseClaimTrack,
                respondToClaim,
                respondToAdmittedClaim,
                respondToAdmittedClaimOwingAmount,
                respondToAdmittedClaimOwingAmountPounds,
                specDefenceFullAdmittedRequired,
                respondent1CourtOrderPayment,
                respondent2CourtOrderPayment,
                respondent1RepaymentPlan,
                respondent2RepaymentPlan,
                respondToClaimAdmitPartLRspec,
                respondToClaimAdmitPartUnemployedLRspec,
                responseClaimAdmitPartEmployer,
                responseClaimAdmitPartEmployerRespondent2,
                responseToClaimAdmitPartWhyNotPayLRspec,
                responseClaimMediationSpecRequired,
                applicant1ClaimMediationSpecRequired,
                defenceAdmitPartEmploymentTypeRequired,
                responseClaimExpertSpecRequired,
                applicant1ClaimExpertSpecRequired,
                responseClaimWitnesses,
                applicant1ClaimWitnesses,
                smallClaimHearingInterpreterRequired,
                smallClaimHearingInterpreterDescription,
                respondToClaimAdmitPartEmploymentTypeLRspec,
                specDefenceAdmittedRequired,
                additionalInformationForJudge,
                applicantAdditionalInformationForJudge,
                respondToClaimExperts,
                caseNote,
                caseNotes,
                withdrawClaim,
                discontinueClaim,
                businessProcess,
                respondent1DQ,
                respondent2DQ,
                applicant1DQ,
                applicant2DQ,
                genericLitigationFriend,
                respondent1LitigationFriend,
                respondent2LitigationFriend,
                applicant1LitigationFriendRequired,
                applicant1LitigationFriend,
                applicant2LitigationFriendRequired,
                applicant2LitigationFriend,
                defendantSolicitorNotifyClaimOptions,
                defendantSolicitorNotifyClaimDetailsOptions,
                selectLitigationFriend,
                litigantFriendSelection,
                claimProceedsInCaseman,
                applicantSolicitor1PbaAccountsIsEmpty,
                multiPartyResponseTypeFlags,
                applicantsProceedIntention,
                claimantResponseScenarioFlag,
                claimantResponseDocumentToDefendant2Flag,
                claimant2ResponseFlag,
                atLeastOneClaimResponseTypeForSpecIsFullDefence,
                specFullAdmissionOrPartAdmission,
                sameSolicitorSameResponse,
                specPaidLessAmountOrDisputesOrPartAdmission,
                specFullDefenceOrPartAdmission1V1,
                specFullDefenceOrPartAdmission,
                specDisputesOrPartAdmission,
                submittedDate,
                paymentSuccessfulDate,
                issueDate,
                claimNotificationDeadline,
                claimNotificationDate,
                claimDetailsNotificationDeadline,
                claimDetailsNotificationDate,
                respondent1ResponseDeadline,
                respondent2ResponseDeadline,
                claimDismissedDeadline,
                respondent1TimeExtensionDate,
                respondent2TimeExtensionDate,
                respondent1AcknowledgeNotificationDate,
                respondent2AcknowledgeNotificationDate,
                respondent1ResponseDate,
                respondent2ResponseDate,
                applicant1ResponseDeadline,
                applicant1ResponseDate,
                applicant2ResponseDate,
                takenOfflineDate,
                takenOfflineByStaffDate,
                claimDismissedDate,
                claimAmountBreakupSummaryObject,
                respondent1LitigationFriendDate,
                respondent2LitigationFriendDate,
                respondent1LitigationFriendCreatedDate,
                respondent2LitigationFriendCreatedDate,
                caseBundles,
                specDefendant1Debts,
                specDefendant2Debts,
                specDefendant1SelfEmploymentDetails,
                specDefendant2SelfEmploymentDetails,
                detailsOfDirectionDisposal,
                detailsOfDirectionTrial,
                hearingSupportRequirementsDJ,
                defendantDetailsSpec,
                defendantDetails,
                bothDefendants,
                partialPaymentAmount,
                partialPayment,
                paymentSetDate,
                repaymentSummaryObject,
                paymentConfirmationDecisionSpec,
                repaymentDue,
                repaymentSuggestion,
                currentDatebox,
                repaymentDate,
                defaultJudgmentDocuments,
                hearingSelection,
                paymentTypeSelection,
                repaymentFrequency,
                isRespondent1,
                isRespondent2,
                isApplicant1,
                claimStarted,
                caseDataExtension
            );
        }

        public String toString() {
            return "CaseData.CaseDataBuilder(ccdCaseReference=" + this.ccdCaseReference + ", ccdState=" + this.ccdState + ", generalAppType=" + this.generalAppType + ", generalAppRespondentAgreement=" + this.generalAppRespondentAgreement + ", generalAppPBADetails=" + this.generalAppPBADetails + ", generalAppDetailsOfOrder=" + this.generalAppDetailsOfOrder + ", generalAppReasonsOfOrder=" + this.generalAppReasonsOfOrder + ", generalAppInformOtherParty=" + this.generalAppInformOtherParty + ", generalAppUrgencyRequirement=" + this.generalAppUrgencyRequirement + ", generalAppStatementOfTruth=" + this.generalAppStatementOfTruth + ", generalAppHearingDetails=" + this.generalAppHearingDetails + ", generalAppApplnSolicitor=" + this.generalAppApplnSolicitor + ", generalAppRespondentSolicitors=" + this.generalAppRespondentSolicitors + ", generalAppEvidenceDocument=" + this.generalAppEvidenceDocument + ", generalApplications=" + this.generalApplications + ", generalApplicationsDetails=" + this.generalApplicationsDetails + ", solicitorReferences=" + this.solicitorReferences + ", solicitorReferencesCopy=" + this.solicitorReferencesCopy + ", respondentSolicitor2Reference=" + this.respondentSolicitor2Reference + ", courtLocation=" + this.courtLocation + ", applicant1=" + this.applicant1 + ", applicant2=" + this.applicant2 + ", applicantSolicitor1CheckEmail=" + this.applicantSolicitor1CheckEmail + ", applicantSolicitor1UserDetails=" + this.applicantSolicitor1UserDetails + ", addApplicant2=" + this.addApplicant2 + ", addRespondent2=" + this.addRespondent2 + ", respondent2SameLegalRepresentative=" + this.respondent2SameLegalRepresentative + ", respondent1=" + this.respondent1 + ", respondent1Copy=" + this.respondent1Copy + ", respondent2=" + this.respondent2 + ", respondent2Copy=" + this.respondent2Copy + ", respondent1DetailsForClaimDetailsTab=" + this.respondent1DetailsForClaimDetailsTab + ", respondent2DetailsForClaimDetailsTab=" + this.respondent2DetailsForClaimDetailsTab + ", respondent1Represented=" + this.respondent1Represented + ", respondent2Represented=" + this.respondent2Represented + ", respondent1OrgRegistered=" + this.respondent1OrgRegistered + ", respondent2OrgRegistered=" + this.respondent2OrgRegistered + ", respondentSolicitor1EmailAddress=" + this.respondentSolicitor1EmailAddress + ", respondentSolicitor2EmailAddress=" + this.respondentSolicitor2EmailAddress + ", uploadParticularsOfClaim=" + this.uploadParticularsOfClaim + ", detailsOfClaim=" + this.detailsOfClaim + ", claimValue=" + this.claimValue + ", claimFee=" + this.claimFee + ", paymentReference=" + this.paymentReference + ", applicantSolicitor1PbaAccounts=" + this.applicantSolicitor1PbaAccounts + ", claimType=" + this.claimType + ", superClaimType=" + this.superClaimType + ", claimTypeOther=" + this.claimTypeOther + ", personalInjuryType=" + this.personalInjuryType + ", personalInjuryTypeOther=" + this.personalInjuryTypeOther + ", applicantSolicitor1ClaimStatementOfTruth=" + this.applicantSolicitor1ClaimStatementOfTruth + ", uiStatementOfTruth=" + this.uiStatementOfTruth + ", legacyCaseReference=" + this.legacyCaseReference + ", allocatedTrack=" + this.allocatedTrack + ", paymentDetails=" + this.paymentDetails + ", claimIssuedPaymentDetails=" + this.claimIssuedPaymentDetails + ", applicant1OrganisationPolicy=" + this.applicant1OrganisationPolicy + ", applicant2OrganisationPolicy=" + this.applicant2OrganisationPolicy + ", respondent1OrganisationPolicy=" + this.respondent1OrganisationPolicy + ", respondent2OrganisationPolicy=" + this.respondent2OrganisationPolicy + ", respondentSolicitor1OrganisationDetails=" + this.respondentSolicitor1OrganisationDetails + ", respondentSolicitor2OrganisationDetails=" + this.respondentSolicitor2OrganisationDetails + ", applicantSolicitor1ServiceAddressRequired=" + this.applicantSolicitor1ServiceAddressRequired + ", applicantSolicitor1ServiceAddress=" + this.applicantSolicitor1ServiceAddress + ", respondentSolicitor1ServiceAddressRequired=" + this.respondentSolicitor1ServiceAddressRequired + ", respondentSolicitor1ServiceAddress=" + this.respondentSolicitor1ServiceAddress + ", respondentSolicitor2ServiceAddressRequired=" + this.respondentSolicitor2ServiceAddressRequired + ", respondentSolicitor2ServiceAddress=" + this.respondentSolicitor2ServiceAddress + ", applicant1ServiceStatementOfTruthToRespondentSolicitor1=" + this.applicant1ServiceStatementOfTruthToRespondentSolicitor1 + ", systemGeneratedCaseDocuments=" + this.systemGeneratedCaseDocuments + ", specClaimTemplateDocumentFiles=" + this.specClaimTemplateDocumentFiles + ", specClaimDetailsDocumentFiles=" + this.specClaimDetailsDocumentFiles + ", speclistYourEvidenceList=" + this.speclistYourEvidenceList + ", specApplicantCorrespondenceAddressRequired=" + this.specApplicantCorrespondenceAddressRequired + ", specApplicantCorrespondenceAddressdetails=" + this.specApplicantCorrespondenceAddressdetails + ", specRespondentCorrespondenceAddressRequired=" + this.specRespondentCorrespondenceAddressRequired + ", specRespondentCorrespondenceAddressdetails=" + this.specRespondentCorrespondenceAddressdetails + ", specAoSRespondent2HomeAddressRequired=" + ", respondentSolicitor1AgreedDeadlineExtension=" + this.respondentSolicitor1AgreedDeadlineExtension + ", respondentSolicitor2AgreedDeadlineExtension=" + this.respondentSolicitor2AgreedDeadlineExtension + ", respondent1ClaimResponseIntentionType=" + this.respondent1ClaimResponseIntentionType + ", respondent2ClaimResponseIntentionType=" + this.respondent2ClaimResponseIntentionType + ", respondent1ClaimResponseIntentionTypeApplicant2=" + this.respondent1ClaimResponseIntentionTypeApplicant2 + ", servedDocumentFiles=" + this.servedDocumentFiles + ", respondentResponseIsSame=" + this.respondentResponseIsSame + ", defendantSingleResponseToBothClaimants=" + this.defendantSingleResponseToBothClaimants + ", respondent1ClaimResponseType=" + this.respondent1ClaimResponseType + ", respondent2ClaimResponseType=" + this.respondent2ClaimResponseType + ", respondent1ClaimResponseTypeToApplicant2=" + this.respondent1ClaimResponseTypeToApplicant2 + ", respondent1ClaimResponseDocument=" + this.respondent1ClaimResponseDocument + ", respondent2ClaimResponseDocument=" + this.respondent2ClaimResponseDocument + ", respondentSharedClaimResponseDocument=" + this.respondentSharedClaimResponseDocument + ", respondent1GeneratedResponseDocument=" + this.respondent1GeneratedResponseDocument + ", respondent2GeneratedResponseDocument=" + this.respondent2GeneratedResponseDocument + ", defendantResponseDocuments=" + this.defendantResponseDocuments + ", applicant1ProceedWithClaim=" + this.applicant1ProceedWithClaim + ", applicant1ProceedWithClaimMultiParty2v1=" + this.applicant1ProceedWithClaimMultiParty2v1 + ", applicant2ProceedWithClaimMultiParty2v1=" + this.applicant2ProceedWithClaimMultiParty2v1 + ", applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2=" + this.applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 + ", applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2=" + this.applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 + ", applicant1ProceedWithClaimRespondent2=" + this.applicant1ProceedWithClaimRespondent2 + ", applicant1DefenceResponseDocument=" + this.applicant1DefenceResponseDocument + ", claimantDefenceResDocToDefendant2=" + this.claimantDefenceResDocToDefendant2 + ", claimantResponseDocuments=" + this.claimantResponseDocuments + ", claimAmountBreakup=" + this.claimAmountBreakup + ", timelineOfEvents=" + this.timelineOfEvents + ", totalClaimAmount=" + this.totalClaimAmount + ", totalInterest=" + this.totalInterest + ", claimInterest=" + this.claimInterest + ", interestClaimOptions=" + this.interestClaimOptions + ", sameRateInterestSelection=" + this.sameRateInterestSelection + ", breakDownInterestTotal=" + this.breakDownInterestTotal + ", breakDownInterestDescription=" + this.breakDownInterestDescription + ", interestClaimFrom=" + this.interestClaimFrom + ", interestClaimUntil=" + this.interestClaimUntil + ", interestFromSpecificDate=" + this.interestFromSpecificDate + ", interestFromSpecificDateDescription=" + this.interestFromSpecificDateDescription + ", calculatedInterest=" + this.calculatedInterest + ", specRespondentSolicitor1EmailAddress=" + this.specRespondentSolicitor1EmailAddress + ", specRespondent1Represented=" + this.specRespondent1Represented + ", specRespondent2Represented=" + this.specRespondent2Represented + ", specResponseTimelineOfEvents=" + this.specResponseTimelineOfEvents + ", specClaimResponseTimelineList=" + this.specClaimResponseTimelineList + ", specResponseTimelineDocumentFiles=" + this.specResponseTimelineDocumentFiles + ", specResponselistYourEvidenceList=" + this.specResponselistYourEvidenceList + ", detailsOfWhyDoesYouDisputeTheClaim=" + this.detailsOfWhyDoesYouDisputeTheClaim + ", respondent1SpecDefenceResponseDocument=" + this.respondent1SpecDefenceResponseDocument + ", respondent1ClaimResponseTypeForSpec=" + this.respondent1ClaimResponseTypeForSpec + ", respondent2ClaimResponseTypeForSpec=" + this.respondent2ClaimResponseTypeForSpec + ", claimant1ClaimResponseTypeForSpec=" + this.claimant1ClaimResponseTypeForSpec + ", claimant2ClaimResponseTypeForSpec=" + this.claimant2ClaimResponseTypeForSpec + ", respondent1ClaimResponsePaymentAdmissionForSpec=" + this.respondent1ClaimResponsePaymentAdmissionForSpec + ", defenceAdmitPartPaymentTimeRouteRequired=" + this.defenceAdmitPartPaymentTimeRouteRequired + ", defenceRouteRequired=" + this.defenceRouteRequired + ", responseClaimTrack=" + this.responseClaimTrack + ", respondToClaim=" + this.respondToClaim + ", respondToAdmittedClaim=" + this.respondToAdmittedClaim + ", respondToAdmittedClaimOwingAmount=" + this.respondToAdmittedClaimOwingAmount + ", respondToAdmittedClaimOwingAmountPounds=" + this.respondToAdmittedClaimOwingAmountPounds + ", specDefenceFullAdmittedRequired=" + this.specDefenceFullAdmittedRequired + ", respondent1CourtOrderPayment=" + this.respondent1CourtOrderPayment + ", respondent2CourtOrderPayment=" + this.respondent2CourtOrderPayment + ", respondent1RepaymentPlan=" + this.respondent1RepaymentPlan + ", respondent2RepaymentPlan=" + this.respondent2RepaymentPlan + ", respondToClaimAdmitPartLRspec=" + this.respondToClaimAdmitPartLRspec + ", respondToClaimAdmitPartUnemployedLRspec=" + this.respondToClaimAdmitPartUnemployedLRspec + ", responseClaimAdmitPartEmployer=" + this.responseClaimAdmitPartEmployer + ", responseClaimAdmitPartEmployerRespondent2=" + this.responseClaimAdmitPartEmployerRespondent2 + ", responseToClaimAdmitPartWhyNotPayLRspec=" + this.responseToClaimAdmitPartWhyNotPayLRspec + ", responseClaimMediationSpecRequired=" + this.responseClaimMediationSpecRequired + ", applicant1ClaimMediationSpecRequired=" + this.applicant1ClaimMediationSpecRequired + ", defenceAdmitPartEmploymentTypeRequired=" + this.defenceAdmitPartEmploymentTypeRequired + ", responseClaimExpertSpecRequired=" + this.responseClaimExpertSpecRequired + ", applicant1ClaimExpertSpecRequired=" + this.applicant1ClaimExpertSpecRequired + ", responseClaimWitnesses=" + this.responseClaimWitnesses + ", applicant1ClaimWitnesses=" + this.applicant1ClaimWitnesses + ", smallClaimHearingInterpreterRequired=" + this.smallClaimHearingInterpreterRequired + ", smallClaimHearingInterpreterDescription=" + this.smallClaimHearingInterpreterDescription + ", respondToClaimAdmitPartEmploymentTypeLRspec=" + this.respondToClaimAdmitPartEmploymentTypeLRspec + ", specDefenceAdmittedRequired=" + this.specDefenceAdmittedRequired + ", additionalInformationForJudge=" + this.additionalInformationForJudge + ", applicantAdditionalInformationForJudge=" + this.applicantAdditionalInformationForJudge + ", respondToClaimExperts=" + this.respondToClaimExperts + ", caseNote=" + this.caseNote + ", caseNotes=" + this.caseNotes + ", withdrawClaim=" + this.withdrawClaim + ", discontinueClaim=" + this.discontinueClaim + ", businessProcess=" + this.businessProcess + ", respondent1DQ=" + this.respondent1DQ + ", respondent2DQ=" + this.respondent2DQ + ", applicant1DQ=" + this.applicant1DQ + ", applicant2DQ=" + this.applicant2DQ + ", genericLitigationFriend=" + this.genericLitigationFriend + ", respondent1LitigationFriend=" + this.respondent1LitigationFriend + ", respondent2LitigationFriend=" + this.respondent2LitigationFriend + ", applicant1LitigationFriendRequired=" + this.applicant1LitigationFriendRequired + ", applicant1LitigationFriend=" + this.applicant1LitigationFriend + ", applicant2LitigationFriendRequired=" + this.applicant2LitigationFriendRequired + ", applicant2LitigationFriend=" + this.applicant2LitigationFriend + ", defendantSolicitorNotifyClaimOptions=" + this.defendantSolicitorNotifyClaimOptions + ", defendantSolicitorNotifyClaimDetailsOptions=" + this.defendantSolicitorNotifyClaimDetailsOptions + ", selectLitigationFriend=" + this.selectLitigationFriend + ", litigantFriendSelection=" + this.litigantFriendSelection + ", claimProceedsInCaseman=" + this.claimProceedsInCaseman + ", applicantSolicitor1PbaAccountsIsEmpty=" + this.applicantSolicitor1PbaAccountsIsEmpty + ", multiPartyResponseTypeFlags=" + this.multiPartyResponseTypeFlags + ", applicantsProceedIntention=" + this.applicantsProceedIntention + ", claimantResponseScenarioFlag=" + this.claimantResponseScenarioFlag + ", claimantResponseDocumentToDefendant2Flag=" + this.claimantResponseDocumentToDefendant2Flag + ", claimant2ResponseFlag=" + this.claimant2ResponseFlag + ", atLeastOneClaimResponseTypeForSpecIsFullDefence=" + this.atLeastOneClaimResponseTypeForSpecIsFullDefence + ", specFullAdmissionOrPartAdmission=" + this.specFullAdmissionOrPartAdmission + ", sameSolicitorSameResponse=" + this.sameSolicitorSameResponse + ", specPaidLessAmountOrDisputesOrPartAdmission=" + this.specPaidLessAmountOrDisputesOrPartAdmission + ", specFullDefenceOrPartAdmission1V1=" + this.specFullDefenceOrPartAdmission1V1 + ", specFullDefenceOrPartAdmission=" + this.specFullDefenceOrPartAdmission + ", specDisputesOrPartAdmission=" + this.specDisputesOrPartAdmission + ", submittedDate=" + this.submittedDate + ", paymentSuccessfulDate=" + this.paymentSuccessfulDate + ", issueDate=" + this.issueDate + ", claimNotificationDeadline=" + this.claimNotificationDeadline + ", claimNotificationDate=" + this.claimNotificationDate + ", claimDetailsNotificationDeadline=" + this.claimDetailsNotificationDeadline + ", claimDetailsNotificationDate=" + this.claimDetailsNotificationDate + ", respondent1ResponseDeadline=" + this.respondent1ResponseDeadline + ", respondent2ResponseDeadline=" + this.respondent2ResponseDeadline + ", claimDismissedDeadline=" + this.claimDismissedDeadline + ", respondent1TimeExtensionDate=" + this.respondent1TimeExtensionDate + ", respondent2TimeExtensionDate=" + this.respondent2TimeExtensionDate + ", respondent1AcknowledgeNotificationDate=" + this.respondent1AcknowledgeNotificationDate + ", respondent2AcknowledgeNotificationDate=" + this.respondent2AcknowledgeNotificationDate + ", respondent1ResponseDate=" + this.respondent1ResponseDate + ", respondent2ResponseDate=" + this.respondent2ResponseDate + ", applicant1ResponseDeadline=" + this.applicant1ResponseDeadline + ", applicant1ResponseDate=" + this.applicant1ResponseDate + ", applicant2ResponseDate=" + this.applicant2ResponseDate + ", takenOfflineDate=" + this.takenOfflineDate + ", takenOfflineByStaffDate=" + this.takenOfflineByStaffDate + ", claimDismissedDate=" + this.claimDismissedDate + ", claimAmountBreakupSummaryObject=" + this.claimAmountBreakupSummaryObject + ", respondent1LitigationFriendDate=" + this.respondent1LitigationFriendDate + ", respondent2LitigationFriendDate=" + this.respondent2LitigationFriendDate + ", respondent1LitigationFriendCreatedDate=" + this.respondent1LitigationFriendCreatedDate + ", respondent2LitigationFriendCreatedDate=" + this.respondent2LitigationFriendCreatedDate + ", caseBundles=" + this.caseBundles + ", specDefendant1Debts=" + this.specDefendant1Debts + ", specDefendant2Debts=" + this.specDefendant2Debts + ", specDefendant1SelfEmploymentDetails=" + this.specDefendant1SelfEmploymentDetails + ", specDefendant2SelfEmploymentDetails=" + this.specDefendant2SelfEmploymentDetails + ", detailsOfDirectionDisposal=" + this.detailsOfDirectionDisposal + ", detailsOfDirectionTrial=" + this.detailsOfDirectionTrial + ", hearingSupportRequirementsDJ=" + this.hearingSupportRequirementsDJ + ", defendantDetailsSpec=" + this.defendantDetailsSpec + ", defendantDetails=" + this.defendantDetails + ", bothDefendants=" + this.bothDefendants + ", partialPaymentAmount=" + this.partialPaymentAmount + ", partialPayment=" + this.partialPayment + ", paymentSetDate=" + this.paymentSetDate + ", repaymentSummaryObject=" + this.repaymentSummaryObject + ", paymentConfirmationDecisionSpec=" + this.paymentConfirmationDecisionSpec + ", repaymentDue=" + this.repaymentDue + ", repaymentSuggestion=" + this.repaymentSuggestion + ", currentDatebox=" + this.currentDatebox + ", repaymentDate=" + this.repaymentDate + ", defaultJudgmentDocuments=" + this.defaultJudgmentDocuments + ", hearingSelection=" + this.hearingSelection + ", paymentTypeSelection=" + this.paymentTypeSelection + ", repaymentFrequency=" + this.repaymentFrequency + ", isRespondent1=" + this.isRespondent1 + ", isRespondent2=" + this.isRespondent2 + ", isApplicant1=" + this.isApplicant1 + ", claimStarted=" + this.claimStarted + ", caseDataExtension=" + this.caseDataExtension + ")";
        }
    }
}
