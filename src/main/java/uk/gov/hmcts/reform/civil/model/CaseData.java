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
import uk.gov.hmcts.reform.civil.enums.dq.Language;
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
import uk.gov.hmcts.reform.civil.model.welshenhancements.ChangeLanguagePreference;
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
    private final Long ccdCaseReference;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final CaseState ccdState;
    private final CaseState previousCCDState;
    private final String preStayState;
    private final String manageStayOption;
    private final LocalDate manageStayUpdateRequestDate;
    private final GAApplicationType generalAppType;
    private final GAApplicationTypeLR generalAppTypeLR;
    private final GARespondentOrderAgreement generalAppRespondentAgreement;
    private final GAPbaDetails generalAppPBADetails;
    private final String generalAppDetailsOfOrder;
    private final List<Element<String>> generalAppDetailsOfOrderColl;
    private final String generalAppReasonsOfOrder;
    private final List<Element<String>> generalAppReasonsOfOrderColl;
    private final YesOrNo generalAppAskForCosts;
    private final GAInformOtherParty generalAppInformOtherParty;
    private final GAUrgencyRequirement generalAppUrgencyRequirement;
    private final GAStatementOfTruth generalAppStatementOfTruth;
    private final GAHearingDetails generalAppHearingDetails;
    private final GASolicitorDetailsGAspec generalAppApplnSolicitor;
    private final SRPbaDetails hearingFeePBADetails;
    private final SRPbaDetails claimIssuedPBADetails;
    private final String applicantPartyName;
    private final CertOfSC certOfSC;
    private final String gaWaTrackLabel;

    private final YesOrNo generalAppVaryJudgementType;
    private final YesOrNo generalAppParentClaimantIsApplicant;
    private final YesOrNo parentClaimantIsApplicant;
    private final GAHearingDateGAspec generalAppHearingDate;
    private final Document generalAppN245FormUpload;
    private final YesOrNo gaEaCourtLocation;

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
    private final String serviceRequestReference;
    private final String paymentReference;
    private final DynamicList applicantSolicitor1PbaAccounts;
    private final ClaimTypeUnspec claimTypeUnSpec;
    private final ClaimType claimType;
    private HelpWithFees generalAppHelpWithFees;
    private final HelpWithFeesDetails claimIssuedHwfDetails;
    private final HelpWithFeesDetails hearingHwfDetails;
    private final FeeType hwfFeeType;
    private final SuperClaimType superClaimType;
    private final String claimTypeOther;
    private final PersonalInjuryType personalInjuryType;
    private final String personalInjuryTypeOther;
    private final StatementOfTruth applicantSolicitor1ClaimStatementOfTruth;
    private final StatementOfTruth uiStatementOfTruth;
    private final StatementOfTruth respondent1LiPStatementOfTruth;
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
    private final RespondentSolicitorDetails respondentSolicitorDetails;

    @Builder.Default
    private final List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();

    @Builder.Default
    private final List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
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
    private final LocalDate claimMovedToMediationOn;

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

    @Builder.Default
    private final List<Element<CaseDocument>> duplicateSystemGeneratedCaseDocs = new ArrayList<>();

    @Builder.Default
    @JsonProperty("duplicateClaimantDefResponseDocs")
    private final List<Element<CaseDocument>> duplicateClaimantDefendantResponseDocs = new ArrayList<>();

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
    private final List<TimelineOfEvents> specResponseTimelineOfEvents2;
    private final TimelineUploadTypeSpec specClaimResponseTimelineList;
    private final Document specResponseTimelineDocumentFiles;
    private final List<Evidence> specResponselistYourEvidenceList;
    private final List<Evidence> specResponselistYourEvidenceList2;

    private final String detailsOfWhyDoesYouDisputeTheClaim;
    private final String detailsOfWhyDoesYouDisputeTheClaim2;

    private final ResponseDocument respondent1SpecDefenceResponseDocument;
    private final ResponseDocument respondent2SpecDefenceResponseDocument;

    private final YesOrNo bundleError;
    private final String bundleEvent;

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

    private final MediationContactInformation app1MediationContactInfo;
    private final MediationAvailability app1MediationAvailability;
    private final MediationContactInformation resp1MediationContactInfo;
    private final MediationContactInformation resp2MediationContactInfo;
    private final MediationAvailability resp1MediationAvailability;
    private final MediationAvailability resp2MediationAvailability;

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
    private final LocalDateTime addLegalRepDeadlineRes1;
    private final LocalDateTime addLegalRepDeadlineRes2;
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
    private final OtherDetails otherDetails;
    private final LocalDateTime claimDismissedDate;
    private final String claimAmountBreakupSummaryObject;
    private final LocalDateTime respondent1LitigationFriendDate;
    private final LocalDateTime respondent2LitigationFriendDate;
    private final LocalDateTime respondent1RespondToSettlementAgreementDeadline;
    private final YesOrNo respondent1ResponseDeadlineChecked;
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

    private YesOrNo setRequestDJDamagesFlagForWA;
    private String featureToggleWA;

    private ContactDetailsUpdatedEvent contactDetailsUpdatedEvent;

    /**
     * RTJ = Refer To Judge.
     */
    private final String eventDescriptionRTJ;
    /**
     * RTJ = Refer To Judge.
     */
    private final String additionalInformationRTJ;
    /**
     * Refer To Judge(Defence received in time).
     */
    private List<ConfirmationToggle> confirmReferToJudgeDefenceReceived;

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
    private final List<Element<CaseDocument>> gaAddlDoc;
    private final List<Element<CaseDocument>> gaAddlDocStaff;
    private final List<Element<CaseDocument>> gaAddlDocClaimant;
    private final List<Element<CaseDocument>> gaAddlDocRespondentSol;
    private final List<Element<CaseDocument>> gaAddlDocRespondentSolTwo;
    private final List<Element<CaseDocument>> gaAddlDocBundle;
    private final List<Element<CaseDocument>> gaDraftDocument;
    private final List<Element<CaseDocument>> gaDraftDocStaff;
    private final List<Element<CaseDocument>> gaDraftDocClaimant;
    private final List<Element<CaseDocument>> gaDraftDocRespondentSol;
    private final List<Element<CaseDocument>> gaDraftDocRespondentSolTwo;

    private final List<Element<CaseDocument>> gaRespondDoc;

    @Builder.Default
    private final List<Element<CaseDocument>> hearingDocuments = new ArrayList<>();

    @Builder.Default
    private final List<Element<CaseDocument>> hearingDocumentsWelsh = new ArrayList<>();

    // GA for LIP
    private final YesOrNo isGaApplicantLip;
    private final YesOrNo isGaRespondentOneLip;
    private final YesOrNo isGaRespondentTwoLip;

    private List<DocumentToKeepCollection> documentToKeepCollection;

    private final ChangeLanguagePreference changeLanguagePreference;
    private final PreferredLanguage claimantLanguagePreferenceDisplay;
    private final PreferredLanguage defendantLanguagePreferenceDisplay;

    @Builder.Default
    private final List<Element<CaseDocument>> queryDocuments = new ArrayList<>();

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
    public String setUpJudgementFormattedPermittedDate(LocalDate extendedRespondent1ResponseDate) {
        if (isJudgementDateNotPermitted()) {
            return getFormattedJudgementPermittedDate(extendedRespondent1ResponseDate);
        }
        return null;
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
        return ofNullable(systemGeneratedCaseDocuments)
            .flatMap(docs -> docs.stream()
                .filter(doc -> doc.getValue().getDocumentType().equals(DocumentType.SDO_ORDER))
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
    public boolean isLipClaimantSpecifiedBilingualDocuments() {
        return isApplicant1NotRepresented()
            && getApplicant1DQ() != null
            && getApplicant1DQ().getApplicant1DQLanguage() != null
            && (getApplicant1DQ().getApplicant1DQLanguage().getDocuments() == Language.BOTH || getApplicant1DQ().getApplicant1DQLanguage().getDocuments() == Language.WELSH);
    }

    @JsonIgnore
    public boolean isLipDefendantSpecifiedBilingualDocuments() {
        return isRespondent1NotRepresented()
            && getRespondent1DQ() != null
            && getRespondent1DQ().getRespondent1DQLanguage() != null
            && (getRespondent1DQ().getRespondent1DQLanguage().getDocuments() == Language.BOTH || getRespondent1DQ().getRespondent1DQLanguage().getDocuments() == Language.WELSH);
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
}
