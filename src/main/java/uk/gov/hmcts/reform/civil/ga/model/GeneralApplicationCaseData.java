package uk.gov.hmcts.reform.civil.ga.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationTypeLR;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAMakeApplicationAvailableCheck;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentDebtorOfferGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAOrderCourtOwnInitiativeGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAOrderWithoutNoticeGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingNoticeApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingNoticeDetail;
import uk.gov.hmcts.reform.civil.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.model.genapplication.UploadDocumentByType;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.AssistedOrderCost;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.BeSpokeCostDetailText;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;

/**
 * Minimal GA-specific CaseData representation used while the GA code is ported across
 * from civil-general-applications. Fields will be expanded incrementally as more handlers
 * are migrated.
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneralApplicationCaseData implements MappableObject, GaLipData {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    Long ccdCaseReference;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    CaseState ccdState;

    CaseState state;
    BusinessProcess businessProcess;

    GAApplicationType generalAppType;
    GAPbaDetails generalAppPBADetails;
    YesOrNo parentClaimantIsApplicant;
    String parentCaseReference;
    GAInformOtherParty generalAppInformOtherParty;
    GAUrgencyRequirement generalAppUrgencyRequirement;
    YesOrNo generalAppConsentOrder;
    GARespondentOrderAgreement generalAppRespondentAgreement;
    GeneralAppParentCaseLink generalAppParentCaseLink;
    String emailPartyReference;
    GASolicitorDetailsGAspec generalAppApplnSolicitor;
    HelpWithFees generalAppHelpWithFees;
    CertOfSC certOfSC;
    GAMakeApplicationAvailableCheck makeAppVisibleToRespondents;
    GAHearingDetails generalAppHearingDetails;
    GAHearingDateGAspec generalAppHearingDate;
    GAStatementOfTruth generalAppStatementOfTruth;
    GAStatementOfTruth generalAppResponseStatementOfTruth;
    YesOrNo generalAppVaryJudgementType;
    String generalAppWrittenRepText;
    String generalAppAddlnInfoText;
    String generalAppSuperClaimType;
    String applicantPartyName;
    String claimant1PartyName;
    String claimant2PartyName;
    String defendant1PartyName;
    String defendant2PartyName;
    YesOrNo respondent2SameLegalRepresentative;
    LocalDateTime generalAppNotificationDeadlineDate;
    GAJudicialWrittenRepresentations judicialDecisionMakeAnOrderForWrittenRepresentations;
    GAJudicialDecision judicialDecision;
    GAJudicialMakeAnOrder judicialDecisionMakeOrder;
    GAJudicialRequestMoreInfo judicialDecisionRequestMoreInfo;
    GAApproveConsentOrder approveConsentOrder;
    GAByCourtsInitiativeGAspec judicialByCourtsInitiativeForWrittenRep;
    GAOrderCourtOwnInitiativeGAspec orderCourtOwnInitiativeForWrittenRep;
    GAOrderWithoutNoticeGAspec orderWithoutNoticeForWrittenRep;
    GARespondentRepresentative generalAppRespondent1Representative;
    String generalAppRespondReason;
    String generalAppRespondConsentReason;

    YesOrNo applicant1Represented;
    YesOrNo isGaApplicantLip;
    YesOrNo isGaRespondentOneLip;
    YesOrNo isGaRespondentTwoLip;
    YesOrNo isMultiParty;

    YesOrNo applicantBilingualLanguagePreference;
    YesOrNo respondentBilingualLanguagePreference;
    RespondentLiPResponse respondent1LiPResponse;

    YesOrNo applicationIsCloaked;
    YesOrNo applicationIsUncloakedOnce;
    LocalDateTime applicationTakenOfflineDate;

    GAHearingNoticeApplication gaHearingNoticeApplication;
    GAHearingNoticeDetail gaHearingNoticeDetail;
    String gaHearingNoticeInformation;

    FeeType hwfFeeType;
    HelpWithFeesDetails gaHwfDetails;
    HelpWithFeesDetails additionalHwfDetails;
    HelpWithFeesMoreInformation helpWithFeesMoreInformationGa;
    HelpWithFeesMoreInformation helpWithFeesMoreInformationAdditional;
    FeePaymentOutcomeDetails feePaymentOutcomeDetails;

    @Builder.Default
    List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors = List.of();
    @Builder.Default
    List<Element<GASolicitorDetailsGAspec>> generalAppApplicantAddlSolicitors = List.of();
    @Builder.Default
    List<Element<Document>> generalAppEvidenceDocument = List.of();
    @Builder.Default
    List<Element<Document>> generalAppRespondDocument = List.of();
    @Builder.Default
    List<Element<Document>> generalAppRespondConsentDocument = List.of();
    @Builder.Default
    List<Element<Document>> generalAppRespondDebtorDocument = List.of();
    @Builder.Default
    List<Element<Document>> generalAppWrittenRepUpload = List.of();
    @Builder.Default
    List<Document> generalAppDirOrderUpload = List.of();
    @Builder.Default
    List<Element<Document>> generalAppAddlnInfoUpload = List.of();
    @Builder.Default
    List<Element<GARespondentResponse>> respondentsResponses = List.of();

    @Builder.Default
    List<Element<TranslatedDocument>> translatedDocuments = List.of();
    @Builder.Default
    List<Element<TranslatedDocument>> translatedDocumentsBulkPrint = List.of();

    AssistedOrderCost assistedOrderMakeAnOrderForCosts;
    BeSpokeCostDetailText assistedOrderCostsBespoke;
    FinalOrderSelection finalOrderSelection;
    GARespondentDebtorOfferGAspec gaRespondentDebtorOffer;

    GAApplicationTypeLR generalAppTypeLR;
    YesOrNo generalAppAskForCosts;
    String generalAppDetailsOfOrder;
    @Builder.Default
    List<Element<String>> generalAppDetailsOfOrderColl = List.of();
    String generalAppReasonsOfOrder;
    @Builder.Default
    List<Element<String>> generalAppReasonsOfOrderColl = List.of();
    Party applicant1;
    Party applicant2;
    Party respondent1;
    Party respondent2;
    OrganisationPolicy applicant1OrganisationPolicy;
    OrganisationPolicy respondent1OrganisationPolicy;
    OrganisationPolicy respondent2OrganisationPolicy;
    String respondent1OrganisationIDCopy;
    String respondent2OrganisationIDCopy;
    GACaseLocation caseManagementLocation;
    GACaseManagementCategory caseManagementCategory;
    CaseCategory caseAccessCategory;
    String caseNameHmctsInternal;
    String locationName;
    IdamUserDetails civilServiceUserRoles;
    YesOrNo isDocumentVisible;
    Document generalAppN245FormUpload;
    @Builder.Default
    List<Element<GeneralApplication>> generalApplications = List.of();
    @Builder.Default
    List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection = List.of();
    @Builder.Default
    List<Element<GeneralApplicationsDetails>> gaDetailsTranslationCollection = List.of();
    @Builder.Default
    List<Element<GADetailsRespondentSol>> respondentSolGaAppDetails = List.of();
    @Builder.Default
    List<Element<GADetailsRespondentSol>> respondentSolTwoGaAppDetails = List.of();
    @Builder.Default
    List<Element<CaseDocument>> generalOrderDocument = List.of();
    @Builder.Default
    List<Element<CaseDocument>> dismissalOrderDocument = List.of();
    @Builder.Default
    List<Element<CaseDocument>> directionOrderDocument = List.of();
    @Builder.Default
    List<Element<CaseDocument>> hearingNoticeDocument = List.of();
    @Builder.Default
    List<Element<Document>> gaRespDocument = List.of();
    @Builder.Default
    List<Element<CaseDocument>> gaDraftDocument = List.of();
    @Builder.Default
    List<Element<CaseDocument>> gaAddlDoc = List.of();
    @Builder.Default
    List<Element<UploadDocumentByType>> uploadDocument = List.of();
    LocalDateTime submittedDate;

    @Override
    public boolean isApplicantBilingual() {
        return YesOrNo.YES == applicantBilingualLanguagePreference;
    }

    @Override
    public boolean isRespondentBilingual() {
        return YesOrNo.YES == respondentBilingualLanguagePreference;
    }

    @JsonIgnore
    public boolean isUrgent() {
        return generalAppUrgencyRequirement != null
            && YesOrNo.YES == generalAppUrgencyRequirement.getGeneralAppUrgency();
    }

    @JsonIgnore
    public boolean isNonUrgent() {
        return generalAppUrgencyRequirement != null
            && YesOrNo.NO == generalAppUrgencyRequirement.getGeneralAppUrgency();
    }

    @JsonIgnore
    public boolean isWithNotice() {
        return (applicationIsUncloakedOnce != null && applicationIsUncloakedOnce == YesOrNo.YES)
            || (applicationIsCloaked != null && applicationIsCloaked == YesOrNo.NO)
            || (generalAppInformOtherParty != null
            && YesOrNo.YES == generalAppInformOtherParty.getIsWithNotice());
    }

    @JsonIgnore
    public boolean isHWFTypeApplication() {
        return FeeType.APPLICATION == hwfFeeType;
    }

    @JsonIgnore
    public boolean isHWFTypeAdditional() {
        return FeeType.ADDITIONAL == hwfFeeType;
    }
}
