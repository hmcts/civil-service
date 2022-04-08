package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
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
@Builder(toBuilder = true)
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
    private final YesOrNo specAoSApplicantCorrespondenceAddressRequired;
    private final Address specAoSApplicantCorrespondenceAddressdetails;
    private final YesOrNo specAoSRespondentCorrespondenceAddressRequired;
    private final Address specAoSRespondentCorrespondenceAddressdetails;
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
    private final RepaymentPlanLRspec respondent1RepaymentPlan;
    private final RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec;
    private final UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec;
    private final Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer;
    private final String responseToClaimAdmitPartWhyNotPayLRspec;
    // Fields related to ROC-9453 & ROC-9455
    private final YesOrNo responseClaimMediationSpecRequired;
    private final YesOrNo defenceAdmitPartEmploymentTypeRequired;
    private final YesOrNo responseClaimExpertSpecRequired;
    private final String responseClaimWitnesses;
    private final YesOrNo smallClaimHearingInterpreterRequired;
    private final String smallClaimHearingInterpreterDescription;
    private final List<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspec;
    private final YesOrNo specDefenceAdmittedRequired;

    private final String additionalInformationForJudge;
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
    private final Respondent1SelfEmploymentLRspec specDefendant1SelfEmploymentDetails;

    private final YesOrNo isRespondent1;
    private final YesOrNo isRespondent2;
    private final YesOrNo isApplicant1;
}
