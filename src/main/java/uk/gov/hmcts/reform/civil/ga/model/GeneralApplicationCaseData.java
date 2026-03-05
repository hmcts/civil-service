package uk.gov.hmcts.reform.civil.ga.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList;
import uk.gov.hmcts.reform.civil.ga.enums.dq.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.ga.enums.dq.FinalOrderConsideredToggle;
import uk.gov.hmcts.reform.civil.ga.enums.dq.FinalOrderShowToggle;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GaFinalOrderSelection;
import uk.gov.hmcts.reform.civil.ga.enums.dq.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.ga.enums.welshenhancements.PreTranslationGaDocumentType;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAHearingNoticeApplication;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAHearingNoticeDetail;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudgesHearingListGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAMakeApplicationAvailableCheck;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAOrderCourtOwnInitiativeGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAOrderWithoutNoticeGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAReferToJudgeGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAReferToLegalAdvisorGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentDebtorOfferGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.UploadDocumentByType;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderAppealDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderCost;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderFurtherHearingDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderGiveReasonsDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderHeardRepresentation;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderMadeDateHeardDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderRecitalRecord;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.BeSpokeCostDetailText;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.DetailText;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.DetailTextWithDate;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.model.BaseCaseData;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Value;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.FINISHED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
public class GeneralApplicationCaseData extends BaseCaseData implements MappableObject {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long ccdCaseReference;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private CaseState ccdState;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime createdDate;
    private String detailsOfClaim;
    private YesOrNo addApplicant2;
    private GAApplicationType generalAppType;
    private GARespondentOrderAgreement generalAppRespondentAgreement;
    private YesOrNo generalAppConsentOrder;
    private GeneralApplicationPbaDetails generalAppPBADetails;
    private String generalAppDetailsOfOrder;
    private List<Element<String>> generalAppDetailsOfOrderColl;
    private String generalAppReasonsOfOrder;
    private List<Element<String>> generalAppReasonsOfOrderColl;
    private String legacyCaseReference;
    private LocalDateTime notificationDeadline;
    private LocalDate submittedOn;
    private LocalDateTime generalAppNotificationDeadlineDate;
    private GAInformOtherParty generalAppInformOtherParty;
    private GAUrgencyRequirement generalAppUrgencyRequirement;
    private GAStatementOfTruth generalAppStatementOfTruth;
    private GAStatementOfTruth generalAppResponseStatementOfTruth;
    private GAHearingDetails generalAppHearingDetails;
    private GASolicitorDetailsGAspec generalAppApplnSolicitor;
    private List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors;
    private List<Element<GASolicitorDetailsGAspec>> generalAppApplicantAddlSolicitors;
    private GAHearingDetails hearingDetailsResp;
    private GARespondentRepresentative generalAppRespondent1Representative;
    private String generalAppRespondReason;
    private String generalAppRespondConsentReason;
    private List<Element<CaseDocument>> originalDocumentsBulkPrint;
    private List<Element<Document>> generalAppRespondDocument;
    private List<Element<Document>> generalAppRespondConsentDocument;
    private List<Element<Document>> generalAppRespondDebtorDocument;
    @Deprecated
    private List<Element<CaseDocument>> gaRespondDoc;
    private List<Element<CaseDocument>> gaAddlDoc;
    private List<Element<CaseDocument>> gaAddlDocStaff;
    private List<Element<CaseDocument>> gaAddlDocClaimant;
    private List<Element<CaseDocument>> gaAddlDocRespondentSol;
    private List<Element<CaseDocument>> gaAddlDocRespondentSolTwo;
    private List<Element<CaseDocument>> gaAddlDocBundle;
    private LocalDateTime caseDocumentUploadDateRes;
    private LocalDateTime caseDocumentUploadDate;
    private YesOrNo isDocumentVisible;
    private YesOrNo isMultiParty;
    private YesOrNo parentClaimantIsApplicant;
    private CaseLink caseLink;
    private IdamUserDetails applicantSolicitor1UserDetails;
    private IdamUserDetails civilServiceUserRoles;
    private List<Element<Document>> generalAppEvidenceDocument;
    private List<Element<Document>> gaEvidenceDocStaff;
    private List<Element<Document>> gaEvidenceDocClaimant;
    private List<Element<Document>> gaEvidenceDocRespondentSol;
    private List<Element<Document>> gaEvidenceDocRespondentSolTwo;
    private List<Element<GeneralApplication>> generalApplications;
    private List<Element<GeneralApplicationsDetails>> claimantGaAppDetails;
    private List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection;
    private List<Element<GeneralApplicationsDetails>> gaDetailsTranslationCollection;
    private List<Element<GADetailsRespondentSol>> respondentSolGaAppDetails;
    private List<Element<GADetailsRespondentSol>> respondentSolTwoGaAppDetails;
    private GAJudicialDecision judicialDecision;
    private List<Element<SolicitorDetails>> applicantSolicitors;
    private List<Element<SolicitorDetails>> defendantSolicitors;
    private List<Element<GARespondentResponse>> respondentsResponses;
    private YesOrNo applicationIsCloaked;
    private YesOrNo applicationIsUncloakedOnce;
    private GAJudicialMakeAnOrder judicialDecisionMakeOrder;
    private Document judicialMakeOrderDocPreview;
    private Document judicialListHearingDocPreview;
    private Document judicialWrittenRepDocPreview;
    private Document judicialRequestMoreInfoDocPreview;
    private Document consentOrderDocPreview;
    private GAJudicialRequestMoreInfo judicialDecisionRequestMoreInfo;
    private GAApproveConsentOrder approveConsentOrder;
    private GAJudicialWrittenRepresentations judicialDecisionMakeAnOrderForWrittenRepresentations;
    private String judgeRecitalText;
    private String directionInRelationToHearingText;
    private GAJudgesHearingListGAspec judicialListForHearing;
    private String applicantPartyName;
    private String gaApplicantDisplayName;
    private String claimant1PartyName;
    private String claimant2PartyName;
    private String defendant1PartyName;
    private String defendant2PartyName;
    private CaseLocationCivil caseManagementLocation;
    private YesOrNo isCcmccLocation;
    private GACaseManagementCategory caseManagementCategory;
    private String judicialGeneralHearingOrderRecital;
    private String judicialGOHearingDirections;
    private String judicialHearingGeneralOrderHearingText;
    private String judicialGeneralOrderHearingEstimationTimeText;
    private String judicialHearingGOHearingReqText;
    private String judicialSequentialDateText;
    private String judicialApplicanSequentialDateText;
    private String judicialConcurrentDateText;
    private List<Element<Document>> generalAppWrittenRepUpload;
    @Deprecated
    private List<Element<Document>> gaWrittenRepDocList;
    private List<Element<Document>> generalAppDirOrderUpload;
    private List<Element<Document>> gaDirectionDocList;
    private List<Element<Document>> generalAppAddlnInfoUpload;
    @Deprecated
    private List<Element<Document>> gaAddlnInfoList;
    @Deprecated
    private List<Element<Document>> gaRespDocument;
    @Deprecated
    private List<Element<Document>> gaRespDocStaff;
    @Deprecated
    private List<Element<Document>> gaRespDocClaimant;
    @Deprecated
    private List<Element<Document>> gaRespDocRespondentSol;
    @Deprecated
    private List<Element<Document>> gaRespDocRespondentSolTwo;
    private String gaRespondentDetails;
    private LocalDate issueDate;
    private String generalAppSuperClaimType;
    private GAMakeApplicationAvailableCheck makeAppVisibleToRespondents;
    private String respondentSolicitor1EmailAddress;
    private String respondentSolicitor2EmailAddress;
    private OrganisationPolicy applicant1OrganisationPolicy;
    private OrganisationPolicy respondent1OrganisationPolicy;
    private OrganisationPolicy respondent2OrganisationPolicy;
    private String respondent1OrganisationIDCopy;
    private String respondent2OrganisationIDCopy;
    private YesOrNo respondent2SameLegalRepresentative;
    private GAReferToJudgeGAspec referToJudge;
    private GAReferToLegalAdvisorGAspec referToLegalAdvisor;
    private LocalDateTime applicationClosedDate;
    private LocalDateTime applicationTakenOfflineDate;
    private String locationName;
    private GAByCourtsInitiativeGAspec judicialByCourtsInitiativeListForHearing;
    private GAByCourtsInitiativeGAspec judicialByCourtsInitiativeForWrittenRep;
    private YesOrNo showRequestInfoPreviewDoc;
    private String migrationId;
    private String caseNameHmctsInternal;
    private GaFinalOrderSelection finalOrderSelection;
    private String freeFormRecitalText;
    private String freeFormOrderedText;
    private OrderOnCourtsList orderOnCourtsList;
    private FreeFormOrderValues orderOnCourtInitiative;
    private FreeFormOrderValues orderWithoutNotice;
    private Document gaFinalOrderDocPreview;
    private LocalDateTime mainCaseSubmittedDate;
    @JsonProperty("CaseAccessCategory")
    private CaseCategory caseAccessCategory;
    private YesOrNo generalAppVaryJudgementType;
    private Document generalAppN245FormUpload;
    private GAHearingDateGAspec generalAppHearingDate;
    //PDF Documents
    private List<Element<CaseDocument>> generalOrderDocument = new ArrayList<>();
    private List<Element<CaseDocument>> generalOrderDocStaff;
    private List<Element<CaseDocument>> generalOrderDocClaimant;
    private List<Element<CaseDocument>> generalOrderDocRespondentSol;
    private List<Element<CaseDocument>> generalOrderDocRespondentSolTwo;
    private List<Element<CaseDocument>> gaDraftDocument = new ArrayList<>();
    private List<Element<CaseDocument>> gaDraftDocStaff;
    private List<Element<CaseDocument>> gaDraftDocClaimant;
    private List<Element<CaseDocument>> gaDraftDocRespondentSol;
    private List<Element<CaseDocument>> gaDraftDocRespondentSolTwo;
    private List<Element<CaseDocument>> consentOrderDocument = new ArrayList<>();
    private List<Element<CaseDocument>> dismissalOrderDocument = new ArrayList<>();
    private List<Element<CaseDocument>> dismissalOrderDocStaff;
    private List<Element<CaseDocument>> dismissalOrderDocClaimant;
    private List<Element<CaseDocument>> dismissalOrderDocRespondentSol;
    private List<Element<CaseDocument>> dismissalOrderDocRespondentSolTwo;
    private List<Element<CaseDocument>> directionOrderDocument = new ArrayList<>();
    private List<Element<CaseDocument>> directionOrderDocStaff;
    private List<Element<CaseDocument>> directionOrderDocClaimant;
    private List<Element<CaseDocument>> directionOrderDocRespondentSol;
    private List<Element<CaseDocument>> directionOrderDocRespondentSolTwo;
    private List<Element<CaseDocument>> requestForInformationDocument = new ArrayList<>();
    private List<Element<CaseDocument>> hearingOrderDocument = new ArrayList<>();
    private List<Element<CaseDocument>> hearingNoticeDocument = new ArrayList<>();
    private List<Element<CaseDocument>> hearingNoticeDocStaff;
    private List<Element<CaseDocument>> hearingNoticeDocClaimant;
    private List<Element<CaseDocument>> hearingNoticeDocRespondentSol;
    private List<Element<CaseDocument>> hearingNoticeDocRespondentSolTwo;
    private List<Element<CaseDocument>> writtenRepSequentialDocument = new ArrayList<>();
    private List<Element<CaseDocument>> writtenRepConcurrentDocument = new ArrayList<>();
    private BusinessProcess businessProcess;
    private GAOrderCourtOwnInitiativeGAspec orderCourtOwnInitiativeListForHearing;
    private GAOrderWithoutNoticeGAspec orderWithoutNoticeListForHearing;
    private GAOrderCourtOwnInitiativeGAspec orderCourtOwnInitiativeForWrittenRep;
    private GAOrderWithoutNoticeGAspec orderWithoutNoticeForWrittenRep;
    private YesOrNo assistedOrderMadeSelection;
    private AssistedOrderMadeDateHeardDetails assistedOrderMadeDateHeardDetails;
    private List<FinalOrderShowToggle> assistedOrderJudgeHeardFrom;
    private AssistedOrderHeardRepresentation assistedOrderRepresentation;
    private List<FinalOrderConsideredToggle> typeRepresentationJudgePapersList;
    private List<FinalOrderShowToggle> assistedOrderRecitals;
    private AssistedOrderRecitalRecord assistedOrderRecitalsRecorded;
    private AssistedCostTypesList assistedCostTypes;
    private AssistedOrderCost assistedOrderMakeAnOrderForCosts;
    private YesOrNo publicFundingCostsProtection;
    private DetailText costReservedDetails;
    private BeSpokeCostDetailText assistedOrderCostsBespoke;
    private String assistedOrderOrderedThatText;
    private List<FinalOrderShowToggle> assistedOrderFurtherHearingToggle;
    private AssistedOrderFurtherHearingDetails assistedOrderFurtherHearingDetails;
    private List<FinalOrderShowToggle> assistedOrderAppealToggle;
    private AssistedOrderAppealDetails assistedOrderAppealDetails;
    private OrderMadeOnTypes orderMadeOnOption;
    private DetailTextWithDate orderMadeOnOwnInitiative;
    private DetailTextWithDate orderMadeOnWithOutNotice;
    private YesOrNo assistedOrderGiveReasonsYesNo;
    private AssistedOrderGiveReasonsDetails assistedOrderGiveReasonsDetails;
    private GARespondentDebtorOfferGAspec gaRespondentDebtorOffer;
    private YesOrNo gaRespondentConsent;
    private String applicationTypes;
    private String parentCaseReference;
    private String judgeTitle;
    private List<Element<UploadDocumentByType>> uploadDocument;
    private YesOrNo applicant1Represented;
    private YesOrNo respondent1Represented;
    private YesOrNo specRespondent1Represented;
    // GA for LIP
    private YesOrNo isGaApplicantLip;
    private YesOrNo isGaRespondentOneLip;
    private YesOrNo isGaRespondentTwoLip;
    private YesOrNo isApplicantResponded;
    private YesOrNo isRespondentResponded;
    private IdamUserDetails claimantUserDetails;
    private IdamUserDetails defendantUserDetails;
    private HelpWithFees generalAppHelpWithFees;
    private HelpWithFees gaAdditionalHelpWithFees;
    private FeeType hwfFeeType;
    private HelpWithFeesDetails gaHwfDetails;
    private HelpWithFeesDetails additionalHwfDetails;
    private HelpWithFeesMoreInformation helpWithFeesMoreInformationGa;
    private HelpWithFeesMoreInformation helpWithFeesMoreInformationAdditional;
    private YesOrNo generalAppAskForCosts;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal applicationFeeAmountInPence;
    private CertOfSC certOfSC;
    //WA claim track description
    private String gaWaTrackLabel;
    private String emailPartyReference;
    private List<Value<Document>> caseDocuments = new ArrayList<>();
    private String caseDocument1Name;
    private List<IdValue<Bundle>> caseBundles = new ArrayList<>();
    private List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>();
    private PreTranslationGaDocumentType preTranslationGaDocumentType;
    private List<Element<CaseDocument>> preTranslationGaDocsApplicant = new ArrayList<>();
    private List<Element<CaseDocument>> preTranslationGaDocsRespondent = new ArrayList<>();
    private GeneralApplicationParty applicant1;
    private GeneralApplicationParty respondent1;
    private List<Element<TranslatedDocument>> translatedDocuments;
    private List<Element<TranslatedDocument>> translatedDocumentsBulkPrint;
    private GeneralAppParentCaseLink generalAppParentCaseLink;
    private GAHearingNoticeApplication gaHearingNoticeApplication;
    private GAHearingNoticeDetail gaHearingNoticeDetail;
    private String gaHearingNoticeInformation;
    @JsonUnwrapped
    private FeePaymentOutcomeDetails feePaymentOutcomeDetails;
    private String generalAppAddlnInfoText;
    private String generalAppWrittenRepText;
    private YesOrNo respondentResponseDeadlineChecked;
    //Case name for manage case
    private String caseNameGaInternal;
    private String claimantBilingualLanguagePreference;
    private RespondentLiPResponse respondent1LiPResponse;
    private YesOrNo bilingualHint;
    private YesOrNo applicantBilingualLanguagePreference;
    private YesOrNo respondentBilingualLanguagePreference;
    private LocalDateTime generalAppSubmittedDateGAspec;

    public GeneralApplicationCaseData copy() {
        return new GeneralApplicationCaseData(this);
    }

    public GeneralApplicationCaseData build() {
        return this;
    }

    private GeneralApplicationCaseData(GeneralApplicationCaseData other) {
        this.ccdCaseType(other.getCcdCaseType());
        this.ccdCaseReference = other.ccdCaseReference;
        this.ccdState = other.ccdState;
        this.createdDate = other.createdDate;
        this.detailsOfClaim = other.detailsOfClaim;
        this.addApplicant2 = other.addApplicant2;
        this.generalAppType = other.generalAppType;
        this.generalAppRespondentAgreement = other.generalAppRespondentAgreement;
        this.generalAppConsentOrder = other.generalAppConsentOrder;
        this.generalAppPBADetails = other.generalAppPBADetails;
        this.generalAppDetailsOfOrder = other.generalAppDetailsOfOrder;
        this.generalAppDetailsOfOrderColl = other.generalAppDetailsOfOrderColl;
        this.generalAppReasonsOfOrder = other.generalAppReasonsOfOrder;
        this.generalAppReasonsOfOrderColl = other.generalAppReasonsOfOrderColl;
        this.legacyCaseReference = other.legacyCaseReference;
        this.notificationDeadline = other.notificationDeadline;
        this.submittedOn = other.submittedOn;
        this.generalAppNotificationDeadlineDate = other.generalAppNotificationDeadlineDate;
        this.generalAppInformOtherParty = other.generalAppInformOtherParty;
        this.generalAppUrgencyRequirement = other.generalAppUrgencyRequirement;
        this.generalAppStatementOfTruth = other.generalAppStatementOfTruth;
        this.generalAppResponseStatementOfTruth = other.generalAppResponseStatementOfTruth;
        this.generalAppHearingDetails = other.generalAppHearingDetails;
        this.generalAppApplnSolicitor = other.generalAppApplnSolicitor;
        this.generalAppRespondentSolicitors = other.generalAppRespondentSolicitors;
        this.generalAppApplicantAddlSolicitors = other.generalAppApplicantAddlSolicitors;
        this.hearingDetailsResp = other.hearingDetailsResp;
        this.generalAppRespondent1Representative = other.generalAppRespondent1Representative;
        this.generalAppRespondReason = other.generalAppRespondReason;
        this.generalAppRespondConsentReason = other.generalAppRespondConsentReason;
        this.originalDocumentsBulkPrint = other.originalDocumentsBulkPrint;
        this.generalAppRespondDocument = other.generalAppRespondDocument;
        this.generalAppRespondConsentDocument = other.generalAppRespondConsentDocument;
        this.generalAppRespondDebtorDocument = other.generalAppRespondDebtorDocument;
        this.gaRespondDoc = other.gaRespondDoc;
        this.gaAddlDoc = other.gaAddlDoc;
        this.gaAddlDocStaff = other.gaAddlDocStaff;
        this.gaAddlDocClaimant = other.gaAddlDocClaimant;
        this.gaAddlDocRespondentSol = other.gaAddlDocRespondentSol;
        this.gaAddlDocRespondentSolTwo = other.gaAddlDocRespondentSolTwo;
        this.gaAddlDocBundle = other.gaAddlDocBundle;
        this.caseDocumentUploadDateRes = other.caseDocumentUploadDateRes;
        this.caseDocumentUploadDate = other.caseDocumentUploadDate;
        this.isDocumentVisible = other.isDocumentVisible;
        this.isMultiParty = other.isMultiParty;
        this.parentClaimantIsApplicant = other.parentClaimantIsApplicant;
        this.caseLink = other.caseLink;
        this.applicantSolicitor1UserDetails = other.applicantSolicitor1UserDetails;
        this.civilServiceUserRoles = other.civilServiceUserRoles;
        this.generalAppEvidenceDocument = other.generalAppEvidenceDocument;
        this.gaEvidenceDocStaff = other.gaEvidenceDocStaff;
        this.gaEvidenceDocClaimant = other.gaEvidenceDocClaimant;
        this.gaEvidenceDocRespondentSol = other.gaEvidenceDocRespondentSol;
        this.gaEvidenceDocRespondentSolTwo = other.gaEvidenceDocRespondentSolTwo;
        this.generalApplications = other.generalApplications;
        this.claimantGaAppDetails = other.claimantGaAppDetails;
        this.gaDetailsMasterCollection = other.gaDetailsMasterCollection;
        this.gaDetailsTranslationCollection = other.gaDetailsTranslationCollection;
        this.respondentSolGaAppDetails = other.respondentSolGaAppDetails;
        this.respondentSolTwoGaAppDetails = other.respondentSolTwoGaAppDetails;
        this.judicialDecision = other.judicialDecision;
        this.applicantSolicitors = other.applicantSolicitors;
        this.defendantSolicitors = other.defendantSolicitors;
        this.respondentsResponses = other.respondentsResponses;
        this.applicationIsCloaked = other.applicationIsCloaked;
        this.applicationIsUncloakedOnce = other.applicationIsUncloakedOnce;
        this.judicialDecisionMakeOrder = other.judicialDecisionMakeOrder;
        this.judicialMakeOrderDocPreview = other.judicialMakeOrderDocPreview;
        this.judicialListHearingDocPreview = other.judicialListHearingDocPreview;
        this.judicialWrittenRepDocPreview = other.judicialWrittenRepDocPreview;
        this.judicialRequestMoreInfoDocPreview = other.judicialRequestMoreInfoDocPreview;
        this.consentOrderDocPreview = other.consentOrderDocPreview;
        this.judicialDecisionRequestMoreInfo = other.judicialDecisionRequestMoreInfo;
        this.approveConsentOrder = other.approveConsentOrder;
        this.judicialDecisionMakeAnOrderForWrittenRepresentations = other.judicialDecisionMakeAnOrderForWrittenRepresentations;
        this.judgeRecitalText = other.judgeRecitalText;
        this.directionInRelationToHearingText = other.directionInRelationToHearingText;
        this.judicialListForHearing = other.judicialListForHearing;
        this.applicantPartyName = other.applicantPartyName;
        this.gaApplicantDisplayName = other.gaApplicantDisplayName;
        this.claimant1PartyName = other.claimant1PartyName;
        this.claimant2PartyName = other.claimant2PartyName;
        this.defendant1PartyName = other.defendant1PartyName;
        this.defendant2PartyName = other.defendant2PartyName;
        this.caseManagementLocation = other.caseManagementLocation;
        this.isCcmccLocation = other.isCcmccLocation;
        this.caseManagementCategory = other.caseManagementCategory;
        this.judicialGeneralHearingOrderRecital = other.judicialGeneralHearingOrderRecital;
        this.judicialGOHearingDirections = other.judicialGOHearingDirections;
        this.judicialHearingGeneralOrderHearingText = other.judicialHearingGeneralOrderHearingText;
        this.judicialGeneralOrderHearingEstimationTimeText = other.judicialGeneralOrderHearingEstimationTimeText;
        this.judicialHearingGOHearingReqText = other.judicialHearingGOHearingReqText;
        this.judicialSequentialDateText = other.judicialSequentialDateText;
        this.judicialApplicanSequentialDateText = other.judicialApplicanSequentialDateText;
        this.judicialConcurrentDateText = other.judicialConcurrentDateText;
        this.generalAppWrittenRepUpload = other.generalAppWrittenRepUpload;
        this.gaWrittenRepDocList = other.gaWrittenRepDocList;
        this.generalAppDirOrderUpload = other.generalAppDirOrderUpload;
        this.gaDirectionDocList = other.gaDirectionDocList;
        this.generalAppAddlnInfoUpload = other.generalAppAddlnInfoUpload;
        this.gaAddlnInfoList = other.gaAddlnInfoList;
        this.gaRespDocument = other.gaRespDocument;
        this.gaRespDocStaff = other.gaRespDocStaff;
        this.gaRespDocClaimant = other.gaRespDocClaimant;
        this.gaRespDocRespondentSol = other.gaRespDocRespondentSol;
        this.gaRespDocRespondentSolTwo = other.gaRespDocRespondentSolTwo;
        this.gaRespondentDetails = other.gaRespondentDetails;
        this.issueDate = other.issueDate;
        this.generalAppSuperClaimType = other.generalAppSuperClaimType;
        this.makeAppVisibleToRespondents = other.makeAppVisibleToRespondents;
        this.respondentSolicitor1EmailAddress = other.respondentSolicitor1EmailAddress;
        this.respondentSolicitor2EmailAddress = other.respondentSolicitor2EmailAddress;
        this.applicant1OrganisationPolicy = other.applicant1OrganisationPolicy;
        this.respondent1OrganisationPolicy = other.respondent1OrganisationPolicy;
        this.respondent2OrganisationPolicy = other.respondent2OrganisationPolicy;
        this.respondent1OrganisationIDCopy = other.respondent1OrganisationIDCopy;
        this.respondent2OrganisationIDCopy = other.respondent2OrganisationIDCopy;
        this.respondent2SameLegalRepresentative = other.respondent2SameLegalRepresentative;
        this.referToJudge = other.referToJudge;
        this.referToLegalAdvisor = other.referToLegalAdvisor;
        this.applicationClosedDate = other.applicationClosedDate;
        this.applicationTakenOfflineDate = other.applicationTakenOfflineDate;
        this.locationName = other.locationName;
        this.judicialByCourtsInitiativeListForHearing = other.judicialByCourtsInitiativeListForHearing;
        this.judicialByCourtsInitiativeForWrittenRep = other.judicialByCourtsInitiativeForWrittenRep;
        this.showRequestInfoPreviewDoc = other.showRequestInfoPreviewDoc;
        this.migrationId = other.migrationId;
        this.caseNameHmctsInternal = other.caseNameHmctsInternal;
        this.finalOrderSelection = other.finalOrderSelection;
        this.freeFormRecitalText = other.freeFormRecitalText;
        this.freeFormOrderedText = other.freeFormOrderedText;
        this.orderOnCourtsList = other.orderOnCourtsList;
        this.orderOnCourtInitiative = other.orderOnCourtInitiative;
        this.orderWithoutNotice = other.orderWithoutNotice;
        this.gaFinalOrderDocPreview = other.gaFinalOrderDocPreview;
        this.mainCaseSubmittedDate = other.mainCaseSubmittedDate;
        this.caseAccessCategory = other.caseAccessCategory;
        this.generalAppVaryJudgementType = other.generalAppVaryJudgementType;
        this.generalAppN245FormUpload = other.generalAppN245FormUpload;
        this.generalAppHearingDate = other.generalAppHearingDate;
        this.generalOrderDocument = other.generalOrderDocument;
        this.generalOrderDocStaff = other.generalOrderDocStaff;
        this.generalOrderDocClaimant = other.generalOrderDocClaimant;
        this.generalOrderDocRespondentSol = other.generalOrderDocRespondentSol;
        this.generalOrderDocRespondentSolTwo = other.generalOrderDocRespondentSolTwo;
        this.gaDraftDocument = other.gaDraftDocument;
        this.gaDraftDocStaff = other.gaDraftDocStaff;
        this.gaDraftDocClaimant = other.gaDraftDocClaimant;
        this.gaDraftDocRespondentSol = other.gaDraftDocRespondentSol;
        this.gaDraftDocRespondentSolTwo = other.gaDraftDocRespondentSolTwo;
        this.consentOrderDocument = other.consentOrderDocument;
        this.dismissalOrderDocument = other.dismissalOrderDocument;
        this.dismissalOrderDocStaff = other.dismissalOrderDocStaff;
        this.dismissalOrderDocClaimant = other.dismissalOrderDocClaimant;
        this.dismissalOrderDocRespondentSol = other.dismissalOrderDocRespondentSol;
        this.dismissalOrderDocRespondentSolTwo = other.dismissalOrderDocRespondentSolTwo;
        this.directionOrderDocument = other.directionOrderDocument;
        this.directionOrderDocStaff = other.directionOrderDocStaff;
        this.directionOrderDocClaimant = other.directionOrderDocClaimant;
        this.directionOrderDocRespondentSol = other.directionOrderDocRespondentSol;
        this.directionOrderDocRespondentSolTwo = other.directionOrderDocRespondentSolTwo;
        this.requestForInformationDocument = other.requestForInformationDocument;
        this.hearingOrderDocument = other.hearingOrderDocument;
        this.hearingNoticeDocument = other.hearingNoticeDocument;
        this.hearingNoticeDocStaff = other.hearingNoticeDocStaff;
        this.hearingNoticeDocClaimant = other.hearingNoticeDocClaimant;
        this.hearingNoticeDocRespondentSol = other.hearingNoticeDocRespondentSol;
        this.hearingNoticeDocRespondentSolTwo = other.hearingNoticeDocRespondentSolTwo;
        this.writtenRepSequentialDocument = other.writtenRepSequentialDocument;
        this.writtenRepConcurrentDocument = other.writtenRepConcurrentDocument;
        this.businessProcess = other.businessProcess;
        this.orderCourtOwnInitiativeListForHearing = other.orderCourtOwnInitiativeListForHearing;
        this.orderWithoutNoticeListForHearing = other.orderWithoutNoticeListForHearing;
        this.orderCourtOwnInitiativeForWrittenRep = other.orderCourtOwnInitiativeForWrittenRep;
        this.orderWithoutNoticeForWrittenRep = other.orderWithoutNoticeForWrittenRep;
        this.assistedOrderMadeSelection = other.assistedOrderMadeSelection;
        this.assistedOrderMadeDateHeardDetails = other.assistedOrderMadeDateHeardDetails;
        this.assistedOrderJudgeHeardFrom = other.assistedOrderJudgeHeardFrom;
        this.assistedOrderRepresentation = other.assistedOrderRepresentation;
        this.typeRepresentationJudgePapersList = other.typeRepresentationJudgePapersList;
        this.assistedOrderRecitals = other.assistedOrderRecitals;
        this.assistedOrderRecitalsRecorded = other.assistedOrderRecitalsRecorded;
        this.assistedCostTypes = other.assistedCostTypes;
        this.assistedOrderMakeAnOrderForCosts = other.assistedOrderMakeAnOrderForCosts;
        this.publicFundingCostsProtection = other.publicFundingCostsProtection;
        this.costReservedDetails = other.costReservedDetails;
        this.assistedOrderCostsBespoke = other.assistedOrderCostsBespoke;
        this.assistedOrderOrderedThatText = other.assistedOrderOrderedThatText;
        this.assistedOrderFurtherHearingToggle = other.assistedOrderFurtherHearingToggle;
        this.assistedOrderFurtherHearingDetails = other.assistedOrderFurtherHearingDetails;
        this.assistedOrderAppealToggle = other.assistedOrderAppealToggle;
        this.assistedOrderAppealDetails = other.assistedOrderAppealDetails;
        this.orderMadeOnOption = other.orderMadeOnOption;
        this.orderMadeOnOwnInitiative = other.orderMadeOnOwnInitiative;
        this.orderMadeOnWithOutNotice = other.orderMadeOnWithOutNotice;
        this.assistedOrderGiveReasonsYesNo = other.assistedOrderGiveReasonsYesNo;
        this.assistedOrderGiveReasonsDetails = other.assistedOrderGiveReasonsDetails;
        this.gaRespondentDebtorOffer = other.gaRespondentDebtorOffer;
        this.gaRespondentConsent = other.gaRespondentConsent;
        this.applicationTypes = other.applicationTypes;
        this.parentCaseReference = other.parentCaseReference;
        this.judgeTitle = other.judgeTitle;
        this.uploadDocument = other.uploadDocument;
        this.applicant1Represented = other.applicant1Represented;
        this.respondent1Represented = other.respondent1Represented;
        this.specRespondent1Represented = other.specRespondent1Represented;
        this.isGaApplicantLip = other.isGaApplicantLip;
        this.isGaRespondentOneLip = other.isGaRespondentOneLip;
        this.isGaRespondentTwoLip = other.isGaRespondentTwoLip;
        this.isApplicantResponded = other.isApplicantResponded;
        this.isRespondentResponded = other.isRespondentResponded;
        this.claimantUserDetails = other.claimantUserDetails;
        this.defendantUserDetails = other.defendantUserDetails;
        this.generalAppHelpWithFees = other.generalAppHelpWithFees;
        this.gaAdditionalHelpWithFees = other.gaAdditionalHelpWithFees;
        this.hwfFeeType = other.hwfFeeType;
        this.gaHwfDetails = other.gaHwfDetails;
        this.additionalHwfDetails = other.additionalHwfDetails;
        this.helpWithFeesMoreInformationGa = other.helpWithFeesMoreInformationGa;
        this.helpWithFeesMoreInformationAdditional = other.helpWithFeesMoreInformationAdditional;
        this.generalAppAskForCosts = other.generalAppAskForCosts;
        this.applicationFeeAmountInPence = other.applicationFeeAmountInPence;
        this.certOfSC = other.certOfSC;
        this.gaWaTrackLabel = other.gaWaTrackLabel;
        this.emailPartyReference = other.emailPartyReference;
        this.caseDocuments = other.caseDocuments;
        this.caseDocument1Name = other.caseDocument1Name;
        this.caseBundles = other.caseBundles;
        this.preTranslationGaDocuments = other.preTranslationGaDocuments;
        this.preTranslationGaDocumentType = other.preTranslationGaDocumentType;
        this.preTranslationGaDocsApplicant = other.preTranslationGaDocsApplicant;
        this.preTranslationGaDocsRespondent = other.preTranslationGaDocsRespondent;
        this.applicant1 = other.applicant1;
        this.respondent1 = other.respondent1;
        this.translatedDocuments = other.translatedDocuments;
        this.translatedDocumentsBulkPrint = other.translatedDocumentsBulkPrint;
        this.generalAppParentCaseLink = other.generalAppParentCaseLink;
        this.gaHearingNoticeApplication = other.gaHearingNoticeApplication;
        this.gaHearingNoticeDetail = other.gaHearingNoticeDetail;
        this.gaHearingNoticeInformation = other.gaHearingNoticeInformation;
        this.feePaymentOutcomeDetails = other.feePaymentOutcomeDetails;
        this.generalAppAddlnInfoText = other.generalAppAddlnInfoText;
        this.generalAppWrittenRepText = other.generalAppWrittenRepText;
        this.respondentResponseDeadlineChecked = other.respondentResponseDeadlineChecked;
        this.caseNameGaInternal = other.caseNameGaInternal;
        this.claimantBilingualLanguagePreference = other.claimantBilingualLanguagePreference;
        this.respondent1LiPResponse = other.respondent1LiPResponse;
        this.bilingualHint = other.bilingualHint;
        this.applicantBilingualLanguagePreference = other.applicantBilingualLanguagePreference;
        this.respondentBilingualLanguagePreference = other.respondentBilingualLanguagePreference;
        this.generalAppSubmittedDateGAspec = other.generalAppSubmittedDateGAspec;
    }

    public GeneralApplicationCaseData ccdCaseType(String ccdCaseType) {
        setCcdCaseType(ccdCaseType);
        return this;
    }

    public GeneralApplicationCaseData ccdCaseReference(Long ccdCaseReference) {
        this.ccdCaseReference = ccdCaseReference;
        return this;
    }

    public GeneralApplicationCaseData ccdState(CaseState ccdState) {
        this.ccdState = ccdState;
        return this;
    }

    public GeneralApplicationCaseData createdDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public GeneralApplicationCaseData detailsOfClaim(String detailsOfClaim) {
        this.detailsOfClaim = detailsOfClaim;
        return this;
    }

    public GeneralApplicationCaseData addApplicant2(YesOrNo addApplicant2) {
        this.addApplicant2 = addApplicant2;
        return this;
    }

    public GeneralApplicationCaseData generalAppType(GAApplicationType generalAppType) {
        this.generalAppType = generalAppType;
        return this;
    }

    public GeneralApplicationCaseData generalAppRespondentAgreement(GARespondentOrderAgreement generalAppRespondentAgreement) {
        this.generalAppRespondentAgreement = generalAppRespondentAgreement;
        return this;
    }

    public GeneralApplicationCaseData generalAppConsentOrder(YesOrNo generalAppConsentOrder) {
        this.generalAppConsentOrder = generalAppConsentOrder;
        return this;
    }

    public GeneralApplicationCaseData generalAppPBADetails(GeneralApplicationPbaDetails generalAppPBADetails) {
        this.generalAppPBADetails = generalAppPBADetails;
        return this;
    }

    public GeneralApplicationCaseData generalAppDetailsOfOrder(String generalAppDetailsOfOrder) {
        this.generalAppDetailsOfOrder = generalAppDetailsOfOrder;
        return this;
    }

    public GeneralApplicationCaseData generalAppDetailsOfOrderColl(List<Element<String>> generalAppDetailsOfOrderColl) {
        this.generalAppDetailsOfOrderColl = generalAppDetailsOfOrderColl;
        return this;
    }

    public GeneralApplicationCaseData generalAppReasonsOfOrder(String generalAppReasonsOfOrder) {
        this.generalAppReasonsOfOrder = generalAppReasonsOfOrder;
        return this;
    }

    public GeneralApplicationCaseData generalAppReasonsOfOrderColl(List<Element<String>> generalAppReasonsOfOrderColl) {
        this.generalAppReasonsOfOrderColl = generalAppReasonsOfOrderColl;
        return this;
    }

    public GeneralApplicationCaseData legacyCaseReference(String legacyCaseReference) {
        this.legacyCaseReference = legacyCaseReference;
        return this;
    }

    public GeneralApplicationCaseData notificationDeadline(LocalDateTime notificationDeadline) {
        this.notificationDeadline = notificationDeadline;
        return this;
    }

    public GeneralApplicationCaseData submittedOn(LocalDate submittedOn) {
        this.submittedOn = submittedOn;
        return this;
    }

    public GeneralApplicationCaseData generalAppNotificationDeadlineDate(LocalDateTime generalAppNotificationDeadlineDate) {
        this.generalAppNotificationDeadlineDate = generalAppNotificationDeadlineDate;
        return this;
    }

    public GeneralApplicationCaseData generalAppInformOtherParty(GAInformOtherParty generalAppInformOtherParty) {
        this.generalAppInformOtherParty = generalAppInformOtherParty;
        return this;
    }

    public GeneralApplicationCaseData generalAppUrgencyRequirement(GAUrgencyRequirement generalAppUrgencyRequirement) {
        this.generalAppUrgencyRequirement = generalAppUrgencyRequirement;
        return this;
    }

    public GeneralApplicationCaseData generalAppStatementOfTruth(GAStatementOfTruth generalAppStatementOfTruth) {
        this.generalAppStatementOfTruth = generalAppStatementOfTruth;
        return this;
    }

    public GeneralApplicationCaseData generalAppResponseStatementOfTruth(GAStatementOfTruth generalAppResponseStatementOfTruth) {
        this.generalAppResponseStatementOfTruth = generalAppResponseStatementOfTruth;
        return this;
    }

    public GeneralApplicationCaseData generalAppHearingDetails(GAHearingDetails generalAppHearingDetails) {
        this.generalAppHearingDetails = generalAppHearingDetails;
        return this;
    }

    public GeneralApplicationCaseData generalAppApplnSolicitor(GASolicitorDetailsGAspec generalAppApplnSolicitor) {
        this.generalAppApplnSolicitor = generalAppApplnSolicitor;
        return this;
    }

    public GeneralApplicationCaseData generalAppRespondentSolicitors(List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors) {
        this.generalAppRespondentSolicitors = generalAppRespondentSolicitors;
        return this;
    }

    public GeneralApplicationCaseData generalAppApplicantAddlSolicitors(List<Element<GASolicitorDetailsGAspec>> generalAppApplicantAddlSolicitors) {
        this.generalAppApplicantAddlSolicitors = generalAppApplicantAddlSolicitors;
        return this;
    }

    public GeneralApplicationCaseData hearingDetailsResp(GAHearingDetails hearingDetailsResp) {
        this.hearingDetailsResp = hearingDetailsResp;
        return this;
    }

    public GeneralApplicationCaseData generalAppRespondent1Representative(GARespondentRepresentative generalAppRespondent1Representative) {
        this.generalAppRespondent1Representative = generalAppRespondent1Representative;
        return this;
    }

    public GeneralApplicationCaseData generalAppRespondReason(String generalAppRespondReason) {
        this.generalAppRespondReason = generalAppRespondReason;
        return this;
    }

    public GeneralApplicationCaseData generalAppRespondConsentReason(String generalAppRespondConsentReason) {
        this.generalAppRespondConsentReason = generalAppRespondConsentReason;
        return this;
    }

    public GeneralApplicationCaseData originalDocumentsBulkPrint(List<Element<CaseDocument>> originalDocumentsBulkPrint) {
        this.originalDocumentsBulkPrint = originalDocumentsBulkPrint;
        return this;
    }

    public GeneralApplicationCaseData generalAppRespondDocument(List<Element<Document>> generalAppRespondDocument) {
        this.generalAppRespondDocument = generalAppRespondDocument;
        return this;
    }

    public GeneralApplicationCaseData generalAppRespondConsentDocument(List<Element<Document>> generalAppRespondConsentDocument) {
        this.generalAppRespondConsentDocument = generalAppRespondConsentDocument;
        return this;
    }

    public GeneralApplicationCaseData generalAppRespondDebtorDocument(List<Element<Document>> generalAppRespondDebtorDocument) {
        this.generalAppRespondDebtorDocument = generalAppRespondDebtorDocument;
        return this;
    }

    public GeneralApplicationCaseData gaRespondDoc(List<Element<CaseDocument>> gaRespondDoc) {
        this.gaRespondDoc = gaRespondDoc;
        return this;
    }

    public GeneralApplicationCaseData gaAddlDoc(List<Element<CaseDocument>> gaAddlDoc) {
        this.gaAddlDoc = gaAddlDoc;
        return this;
    }

    public GeneralApplicationCaseData gaAddlDocStaff(List<Element<CaseDocument>> gaAddlDocStaff) {
        this.gaAddlDocStaff = gaAddlDocStaff;
        return this;
    }

    public GeneralApplicationCaseData gaAddlDocClaimant(List<Element<CaseDocument>> gaAddlDocClaimant) {
        this.gaAddlDocClaimant = gaAddlDocClaimant;
        return this;
    }

    public GeneralApplicationCaseData gaAddlDocRespondentSol(List<Element<CaseDocument>> gaAddlDocRespondentSol) {
        this.gaAddlDocRespondentSol = gaAddlDocRespondentSol;
        return this;
    }

    public GeneralApplicationCaseData gaAddlDocRespondentSolTwo(List<Element<CaseDocument>> gaAddlDocRespondentSolTwo) {
        this.gaAddlDocRespondentSolTwo = gaAddlDocRespondentSolTwo;
        return this;
    }

    public GeneralApplicationCaseData gaAddlDocBundle(List<Element<CaseDocument>> gaAddlDocBundle) {
        this.gaAddlDocBundle = gaAddlDocBundle;
        return this;
    }

    public GeneralApplicationCaseData caseDocumentUploadDateRes(LocalDateTime caseDocumentUploadDateRes) {
        this.caseDocumentUploadDateRes = caseDocumentUploadDateRes;
        return this;
    }

    public GeneralApplicationCaseData caseDocumentUploadDate(LocalDateTime caseDocumentUploadDate) {
        this.caseDocumentUploadDate = caseDocumentUploadDate;
        return this;
    }

    public GeneralApplicationCaseData isDocumentVisible(YesOrNo isDocumentVisible) {
        this.isDocumentVisible = isDocumentVisible;
        return this;
    }

    public GeneralApplicationCaseData isMultiParty(YesOrNo isMultiParty) {
        this.isMultiParty = isMultiParty;
        return this;
    }

    public GeneralApplicationCaseData parentClaimantIsApplicant(YesOrNo parentClaimantIsApplicant) {
        this.parentClaimantIsApplicant = parentClaimantIsApplicant;
        return this;
    }

    public GeneralApplicationCaseData caseLink(CaseLink caseLink) {
        this.caseLink = caseLink;
        return this;
    }

    public GeneralApplicationCaseData applicantSolicitor1UserDetails(IdamUserDetails applicantSolicitor1UserDetails) {
        this.applicantSolicitor1UserDetails = applicantSolicitor1UserDetails;
        return this;
    }

    public GeneralApplicationCaseData civilServiceUserRoles(IdamUserDetails civilServiceUserRoles) {
        this.civilServiceUserRoles = civilServiceUserRoles;
        return this;
    }

    public GeneralApplicationCaseData generalAppEvidenceDocument(List<Element<Document>> generalAppEvidenceDocument) {
        this.generalAppEvidenceDocument = generalAppEvidenceDocument;
        return this;
    }

    public GeneralApplicationCaseData gaEvidenceDocStaff(List<Element<Document>> gaEvidenceDocStaff) {
        this.gaEvidenceDocStaff = gaEvidenceDocStaff;
        return this;
    }

    public GeneralApplicationCaseData gaEvidenceDocClaimant(List<Element<Document>> gaEvidenceDocClaimant) {
        this.gaEvidenceDocClaimant = gaEvidenceDocClaimant;
        return this;
    }

    public GeneralApplicationCaseData gaEvidenceDocRespondentSol(List<Element<Document>> gaEvidenceDocRespondentSol) {
        this.gaEvidenceDocRespondentSol = gaEvidenceDocRespondentSol;
        return this;
    }

    public GeneralApplicationCaseData gaEvidenceDocRespondentSolTwo(List<Element<Document>> gaEvidenceDocRespondentSolTwo) {
        this.gaEvidenceDocRespondentSolTwo = gaEvidenceDocRespondentSolTwo;
        return this;
    }

    public GeneralApplicationCaseData generalApplications(List<Element<GeneralApplication>> generalApplications) {
        this.generalApplications = generalApplications;
        return this;
    }

    public GeneralApplicationCaseData claimantGaAppDetails(List<Element<GeneralApplicationsDetails>> claimantGaAppDetails) {
        this.claimantGaAppDetails = claimantGaAppDetails;
        return this;
    }

    public GeneralApplicationCaseData gaDetailsMasterCollection(List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection) {
        this.gaDetailsMasterCollection = gaDetailsMasterCollection;
        return this;
    }

    public GeneralApplicationCaseData gaDetailsTranslationCollection(List<Element<GeneralApplicationsDetails>> gaDetailsTranslationCollection) {
        this.gaDetailsTranslationCollection = gaDetailsTranslationCollection;
        return this;
    }

    public GeneralApplicationCaseData respondentSolGaAppDetails(List<Element<GADetailsRespondentSol>> respondentSolGaAppDetails) {
        this.respondentSolGaAppDetails = respondentSolGaAppDetails;
        return this;
    }

    public GeneralApplicationCaseData respondentSolTwoGaAppDetails(List<Element<GADetailsRespondentSol>> respondentSolTwoGaAppDetails) {
        this.respondentSolTwoGaAppDetails = respondentSolTwoGaAppDetails;
        return this;
    }

    public GeneralApplicationCaseData judicialDecision(GAJudicialDecision judicialDecision) {
        this.judicialDecision = judicialDecision;
        return this;
    }

    public GeneralApplicationCaseData applicantSolicitors(List<Element<SolicitorDetails>> applicantSolicitors) {
        this.applicantSolicitors = applicantSolicitors;
        return this;
    }

    public GeneralApplicationCaseData defendantSolicitors(List<Element<SolicitorDetails>> defendantSolicitors) {
        this.defendantSolicitors = defendantSolicitors;
        return this;
    }

    public GeneralApplicationCaseData respondentsResponses(List<Element<GARespondentResponse>> respondentsResponses) {
        this.respondentsResponses = respondentsResponses;
        return this;
    }

    public GeneralApplicationCaseData applicationIsCloaked(YesOrNo applicationIsCloaked) {
        this.applicationIsCloaked = applicationIsCloaked;
        return this;
    }

    public GeneralApplicationCaseData applicationIsUncloakedOnce(YesOrNo applicationIsUncloakedOnce) {
        this.applicationIsUncloakedOnce = applicationIsUncloakedOnce;
        return this;
    }

    public GeneralApplicationCaseData judicialDecisionMakeOrder(GAJudicialMakeAnOrder judicialDecisionMakeOrder) {
        this.judicialDecisionMakeOrder = judicialDecisionMakeOrder;
        return this;
    }

    public GeneralApplicationCaseData judicialMakeOrderDocPreview(Document judicialMakeOrderDocPreview) {
        this.judicialMakeOrderDocPreview = judicialMakeOrderDocPreview;
        return this;
    }

    public GeneralApplicationCaseData judicialListHearingDocPreview(Document judicialListHearingDocPreview) {
        this.judicialListHearingDocPreview = judicialListHearingDocPreview;
        return this;
    }

    public GeneralApplicationCaseData judicialWrittenRepDocPreview(Document judicialWrittenRepDocPreview) {
        this.judicialWrittenRepDocPreview = judicialWrittenRepDocPreview;
        return this;
    }

    public GeneralApplicationCaseData judicialRequestMoreInfoDocPreview(Document judicialRequestMoreInfoDocPreview) {
        this.judicialRequestMoreInfoDocPreview = judicialRequestMoreInfoDocPreview;
        return this;
    }

    public GeneralApplicationCaseData consentOrderDocPreview(Document consentOrderDocPreview) {
        this.consentOrderDocPreview = consentOrderDocPreview;
        return this;
    }

    public GeneralApplicationCaseData judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo judicialDecisionRequestMoreInfo) {
        this.judicialDecisionRequestMoreInfo = judicialDecisionRequestMoreInfo;
        return this;
    }

    public GeneralApplicationCaseData approveConsentOrder(GAApproveConsentOrder approveConsentOrder) {
        this.approveConsentOrder = approveConsentOrder;
        return this;
    }

    public GeneralApplicationCaseData judicialDecisionMakeAnOrderForWrittenRepresentations(GAJudicialWrittenRepresentations judicialDecisionMakeAnOrderForWrittenRepresentations) {
        this.judicialDecisionMakeAnOrderForWrittenRepresentations = judicialDecisionMakeAnOrderForWrittenRepresentations;
        return this;
    }

    public GeneralApplicationCaseData judgeRecitalText(String judgeRecitalText) {
        this.judgeRecitalText = judgeRecitalText;
        return this;
    }

    public GeneralApplicationCaseData directionInRelationToHearingText(String directionInRelationToHearingText) {
        this.directionInRelationToHearingText = directionInRelationToHearingText;
        return this;
    }

    public GeneralApplicationCaseData judicialListForHearing(GAJudgesHearingListGAspec judicialListForHearing) {
        this.judicialListForHearing = judicialListForHearing;
        return this;
    }

    public GeneralApplicationCaseData applicantPartyName(String applicantPartyName) {
        this.applicantPartyName = applicantPartyName;
        return this;
    }

    public GeneralApplicationCaseData gaApplicantDisplayName(String gaApplicantDisplayName) {
        this.gaApplicantDisplayName = gaApplicantDisplayName;
        return this;
    }

    public GeneralApplicationCaseData claimant1PartyName(String claimant1PartyName) {
        this.claimant1PartyName = claimant1PartyName;
        return this;
    }

    public GeneralApplicationCaseData claimant2PartyName(String claimant2PartyName) {
        this.claimant2PartyName = claimant2PartyName;
        return this;
    }

    public GeneralApplicationCaseData defendant1PartyName(String defendant1PartyName) {
        this.defendant1PartyName = defendant1PartyName;
        return this;
    }

    public GeneralApplicationCaseData defendant2PartyName(String defendant2PartyName) {
        this.defendant2PartyName = defendant2PartyName;
        return this;
    }

    public GeneralApplicationCaseData caseManagementLocation(CaseLocationCivil caseManagementLocation) {
        this.caseManagementLocation = caseManagementLocation;
        return this;
    }

    public GeneralApplicationCaseData isCcmccLocation(YesOrNo isCcmccLocation) {
        this.isCcmccLocation = isCcmccLocation;
        return this;
    }

    public GeneralApplicationCaseData caseManagementCategory(GACaseManagementCategory caseManagementCategory) {
        this.caseManagementCategory = caseManagementCategory;
        return this;
    }

    public GeneralApplicationCaseData judicialGeneralHearingOrderRecital(String judicialGeneralHearingOrderRecital) {
        this.judicialGeneralHearingOrderRecital = judicialGeneralHearingOrderRecital;
        return this;
    }

    public GeneralApplicationCaseData judicialGOHearingDirections(String judicialGOHearingDirections) {
        this.judicialGOHearingDirections = judicialGOHearingDirections;
        return this;
    }

    public GeneralApplicationCaseData judicialHearingGeneralOrderHearingText(String judicialHearingGeneralOrderHearingText) {
        this.judicialHearingGeneralOrderHearingText = judicialHearingGeneralOrderHearingText;
        return this;
    }

    public GeneralApplicationCaseData judicialGeneralOrderHearingEstimationTimeText(String judicialGeneralOrderHearingEstimationTimeText) {
        this.judicialGeneralOrderHearingEstimationTimeText = judicialGeneralOrderHearingEstimationTimeText;
        return this;
    }

    public GeneralApplicationCaseData judicialHearingGOHearingReqText(String judicialHearingGOHearingReqText) {
        this.judicialHearingGOHearingReqText = judicialHearingGOHearingReqText;
        return this;
    }

    public GeneralApplicationCaseData judicialSequentialDateText(String judicialSequentialDateText) {
        this.judicialSequentialDateText = judicialSequentialDateText;
        return this;
    }

    public GeneralApplicationCaseData judicialApplicanSequentialDateText(String judicialApplicanSequentialDateText) {
        this.judicialApplicanSequentialDateText = judicialApplicanSequentialDateText;
        return this;
    }

    public GeneralApplicationCaseData judicialConcurrentDateText(String judicialConcurrentDateText) {
        this.judicialConcurrentDateText = judicialConcurrentDateText;
        return this;
    }

    public GeneralApplicationCaseData generalAppWrittenRepUpload(List<Element<Document>> generalAppWrittenRepUpload) {
        this.generalAppWrittenRepUpload = generalAppWrittenRepUpload;
        return this;
    }

    public GeneralApplicationCaseData gaWrittenRepDocList(List<Element<Document>> gaWrittenRepDocList) {
        this.gaWrittenRepDocList = gaWrittenRepDocList;
        return this;
    }

    public GeneralApplicationCaseData generalAppDirOrderUpload(List<Element<Document>> generalAppDirOrderUpload) {
        this.generalAppDirOrderUpload = generalAppDirOrderUpload;
        return this;
    }

    public GeneralApplicationCaseData gaDirectionDocList(List<Element<Document>> gaDirectionDocList) {
        this.gaDirectionDocList = gaDirectionDocList;
        return this;
    }

    public GeneralApplicationCaseData generalAppAddlnInfoUpload(List<Element<Document>> generalAppAddlnInfoUpload) {
        this.generalAppAddlnInfoUpload = generalAppAddlnInfoUpload;
        return this;
    }

    public GeneralApplicationCaseData gaAddlnInfoList(List<Element<Document>> gaAddlnInfoList) {
        this.gaAddlnInfoList = gaAddlnInfoList;
        return this;
    }

    public GeneralApplicationCaseData gaRespDocument(List<Element<Document>> gaRespDocument) {
        this.gaRespDocument = gaRespDocument;
        return this;
    }

    public GeneralApplicationCaseData gaRespDocStaff(List<Element<Document>> gaRespDocStaff) {
        this.gaRespDocStaff = gaRespDocStaff;
        return this;
    }

    public GeneralApplicationCaseData gaRespDocClaimant(List<Element<Document>> gaRespDocClaimant) {
        this.gaRespDocClaimant = gaRespDocClaimant;
        return this;
    }

    public GeneralApplicationCaseData gaRespDocRespondentSol(List<Element<Document>> gaRespDocRespondentSol) {
        this.gaRespDocRespondentSol = gaRespDocRespondentSol;
        return this;
    }

    public GeneralApplicationCaseData gaRespDocRespondentSolTwo(List<Element<Document>> gaRespDocRespondentSolTwo) {
        this.gaRespDocRespondentSolTwo = gaRespDocRespondentSolTwo;
        return this;
    }

    public GeneralApplicationCaseData gaRespondentDetails(String gaRespondentDetails) {
        this.gaRespondentDetails = gaRespondentDetails;
        return this;
    }

    public GeneralApplicationCaseData issueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    public GeneralApplicationCaseData generalAppSuperClaimType(String generalAppSuperClaimType) {
        this.generalAppSuperClaimType = generalAppSuperClaimType;
        return this;
    }

    public GeneralApplicationCaseData makeAppVisibleToRespondents(GAMakeApplicationAvailableCheck makeAppVisibleToRespondents) {
        this.makeAppVisibleToRespondents = makeAppVisibleToRespondents;
        return this;
    }

    public GeneralApplicationCaseData respondentSolicitor1EmailAddress(String respondentSolicitor1EmailAddress) {
        this.respondentSolicitor1EmailAddress = respondentSolicitor1EmailAddress;
        return this;
    }

    public GeneralApplicationCaseData respondentSolicitor2EmailAddress(String respondentSolicitor2EmailAddress) {
        this.respondentSolicitor2EmailAddress = respondentSolicitor2EmailAddress;
        return this;
    }

    public GeneralApplicationCaseData applicant1OrganisationPolicy(OrganisationPolicy applicant1OrganisationPolicy) {
        this.applicant1OrganisationPolicy = applicant1OrganisationPolicy;
        return this;
    }

    public GeneralApplicationCaseData respondent1OrganisationPolicy(OrganisationPolicy respondent1OrganisationPolicy) {
        this.respondent1OrganisationPolicy = respondent1OrganisationPolicy;
        return this;
    }

    public GeneralApplicationCaseData respondent2OrganisationPolicy(OrganisationPolicy respondent2OrganisationPolicy) {
        this.respondent2OrganisationPolicy = respondent2OrganisationPolicy;
        return this;
    }

    public GeneralApplicationCaseData respondent1OrganisationIDCopy(String respondent1OrganisationIDCopy) {
        this.respondent1OrganisationIDCopy = respondent1OrganisationIDCopy;
        return this;
    }

    public GeneralApplicationCaseData respondent2OrganisationIDCopy(String respondent2OrganisationIDCopy) {
        this.respondent2OrganisationIDCopy = respondent2OrganisationIDCopy;
        return this;
    }

    public GeneralApplicationCaseData respondent2SameLegalRepresentative(YesOrNo respondent2SameLegalRepresentative) {
        this.respondent2SameLegalRepresentative = respondent2SameLegalRepresentative;
        return this;
    }

    public GeneralApplicationCaseData referToJudge(GAReferToJudgeGAspec referToJudge) {
        this.referToJudge = referToJudge;
        return this;
    }

    public GeneralApplicationCaseData referToLegalAdvisor(GAReferToLegalAdvisorGAspec referToLegalAdvisor) {
        this.referToLegalAdvisor = referToLegalAdvisor;
        return this;
    }

    public GeneralApplicationCaseData applicationClosedDate(LocalDateTime applicationClosedDate) {
        this.applicationClosedDate = applicationClosedDate;
        return this;
    }

    public GeneralApplicationCaseData applicationTakenOfflineDate(LocalDateTime applicationTakenOfflineDate) {
        this.applicationTakenOfflineDate = applicationTakenOfflineDate;
        return this;
    }

    public GeneralApplicationCaseData locationName(String locationName) {
        this.locationName = locationName;
        return this;
    }

    public GeneralApplicationCaseData judicialByCourtsInitiativeListForHearing(GAByCourtsInitiativeGAspec judicialByCourtsInitiativeListForHearing) {
        this.judicialByCourtsInitiativeListForHearing = judicialByCourtsInitiativeListForHearing;
        return this;
    }

    public GeneralApplicationCaseData judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec judicialByCourtsInitiativeForWrittenRep) {
        this.judicialByCourtsInitiativeForWrittenRep = judicialByCourtsInitiativeForWrittenRep;
        return this;
    }

    public GeneralApplicationCaseData showRequestInfoPreviewDoc(YesOrNo showRequestInfoPreviewDoc) {
        this.showRequestInfoPreviewDoc = showRequestInfoPreviewDoc;
        return this;
    }

    public GeneralApplicationCaseData migrationId(String migrationId) {
        this.migrationId = migrationId;
        return this;
    }

    public GeneralApplicationCaseData caseNameHmctsInternal(String caseNameHmctsInternal) {
        this.caseNameHmctsInternal = caseNameHmctsInternal;
        return this;
    }

    public GeneralApplicationCaseData finalOrderSelection(GaFinalOrderSelection finalOrderSelection) {
        this.finalOrderSelection = finalOrderSelection;
        return this;
    }

    public GeneralApplicationCaseData freeFormRecitalText(String freeFormRecitalText) {
        this.freeFormRecitalText = freeFormRecitalText;
        return this;
    }

    public GeneralApplicationCaseData freeFormOrderedText(String freeFormOrderedText) {
        this.freeFormOrderedText = freeFormOrderedText;
        return this;
    }

    public GeneralApplicationCaseData orderOnCourtsList(OrderOnCourtsList orderOnCourtsList) {
        this.orderOnCourtsList = orderOnCourtsList;
        return this;
    }

    public GeneralApplicationCaseData orderOnCourtInitiative(FreeFormOrderValues orderOnCourtInitiative) {
        this.orderOnCourtInitiative = orderOnCourtInitiative;
        return this;
    }

    public GeneralApplicationCaseData orderWithoutNotice(FreeFormOrderValues orderWithoutNotice) {
        this.orderWithoutNotice = orderWithoutNotice;
        return this;
    }

    public GeneralApplicationCaseData gaFinalOrderDocPreview(Document gaFinalOrderDocPreview) {
        this.gaFinalOrderDocPreview = gaFinalOrderDocPreview;
        return this;
    }

    public GeneralApplicationCaseData mainCaseSubmittedDate(LocalDateTime mainCaseSubmittedDate) {
        this.mainCaseSubmittedDate = mainCaseSubmittedDate;
        return this;
    }

    public GeneralApplicationCaseData caseAccessCategory(CaseCategory caseAccessCategory) {
        this.caseAccessCategory = caseAccessCategory;
        return this;
    }

    public GeneralApplicationCaseData generalAppVaryJudgementType(YesOrNo generalAppVaryJudgementType) {
        this.generalAppVaryJudgementType = generalAppVaryJudgementType;
        return this;
    }

    public GeneralApplicationCaseData generalAppN245FormUpload(Document generalAppN245FormUpload) {
        this.generalAppN245FormUpload = generalAppN245FormUpload;
        return this;
    }

    public GeneralApplicationCaseData generalAppHearingDate(GAHearingDateGAspec generalAppHearingDate) {
        this.generalAppHearingDate = generalAppHearingDate;
        return this;
    }

    public GeneralApplicationCaseData generalOrderDocument(List<Element<CaseDocument>> generalOrderDocument) {
        this.generalOrderDocument = generalOrderDocument;
        return this;
    }

    public GeneralApplicationCaseData generalOrderDocStaff(List<Element<CaseDocument>> generalOrderDocStaff) {
        this.generalOrderDocStaff = generalOrderDocStaff;
        return this;
    }

    public GeneralApplicationCaseData generalOrderDocClaimant(List<Element<CaseDocument>> generalOrderDocClaimant) {
        this.generalOrderDocClaimant = generalOrderDocClaimant;
        return this;
    }

    public GeneralApplicationCaseData generalOrderDocRespondentSol(List<Element<CaseDocument>> generalOrderDocRespondentSol) {
        this.generalOrderDocRespondentSol = generalOrderDocRespondentSol;
        return this;
    }

    public GeneralApplicationCaseData generalOrderDocRespondentSolTwo(List<Element<CaseDocument>> generalOrderDocRespondentSolTwo) {
        this.generalOrderDocRespondentSolTwo = generalOrderDocRespondentSolTwo;
        return this;
    }

    public GeneralApplicationCaseData gaDraftDocument(List<Element<CaseDocument>> gaDraftDocument) {
        this.gaDraftDocument = gaDraftDocument;
        return this;
    }

    public GeneralApplicationCaseData gaDraftDocStaff(List<Element<CaseDocument>> gaDraftDocStaff) {
        this.gaDraftDocStaff = gaDraftDocStaff;
        return this;
    }

    public GeneralApplicationCaseData gaDraftDocClaimant(List<Element<CaseDocument>> gaDraftDocClaimant) {
        this.gaDraftDocClaimant = gaDraftDocClaimant;
        return this;
    }

    public GeneralApplicationCaseData gaDraftDocRespondentSol(List<Element<CaseDocument>> gaDraftDocRespondentSol) {
        this.gaDraftDocRespondentSol = gaDraftDocRespondentSol;
        return this;
    }

    public GeneralApplicationCaseData gaDraftDocRespondentSolTwo(List<Element<CaseDocument>> gaDraftDocRespondentSolTwo) {
        this.gaDraftDocRespondentSolTwo = gaDraftDocRespondentSolTwo;
        return this;
    }

    public GeneralApplicationCaseData consentOrderDocument(List<Element<CaseDocument>> consentOrderDocument) {
        this.consentOrderDocument = consentOrderDocument;
        return this;
    }

    public GeneralApplicationCaseData dismissalOrderDocument(List<Element<CaseDocument>> dismissalOrderDocument) {
        this.dismissalOrderDocument = dismissalOrderDocument;
        return this;
    }

    public GeneralApplicationCaseData dismissalOrderDocStaff(List<Element<CaseDocument>> dismissalOrderDocStaff) {
        this.dismissalOrderDocStaff = dismissalOrderDocStaff;
        return this;
    }

    public GeneralApplicationCaseData dismissalOrderDocClaimant(List<Element<CaseDocument>> dismissalOrderDocClaimant) {
        this.dismissalOrderDocClaimant = dismissalOrderDocClaimant;
        return this;
    }

    public GeneralApplicationCaseData dismissalOrderDocRespondentSol(List<Element<CaseDocument>> dismissalOrderDocRespondentSol) {
        this.dismissalOrderDocRespondentSol = dismissalOrderDocRespondentSol;
        return this;
    }

    public GeneralApplicationCaseData dismissalOrderDocRespondentSolTwo(List<Element<CaseDocument>> dismissalOrderDocRespondentSolTwo) {
        this.dismissalOrderDocRespondentSolTwo = dismissalOrderDocRespondentSolTwo;
        return this;
    }

    public GeneralApplicationCaseData directionOrderDocument(List<Element<CaseDocument>> directionOrderDocument) {
        this.directionOrderDocument = directionOrderDocument;
        return this;
    }

    public GeneralApplicationCaseData directionOrderDocStaff(List<Element<CaseDocument>> directionOrderDocStaff) {
        this.directionOrderDocStaff = directionOrderDocStaff;
        return this;
    }

    public GeneralApplicationCaseData directionOrderDocClaimant(List<Element<CaseDocument>> directionOrderDocClaimant) {
        this.directionOrderDocClaimant = directionOrderDocClaimant;
        return this;
    }

    public GeneralApplicationCaseData directionOrderDocRespondentSol(List<Element<CaseDocument>> directionOrderDocRespondentSol) {
        this.directionOrderDocRespondentSol = directionOrderDocRespondentSol;
        return this;
    }

    public GeneralApplicationCaseData directionOrderDocRespondentSolTwo(List<Element<CaseDocument>> directionOrderDocRespondentSolTwo) {
        this.directionOrderDocRespondentSolTwo = directionOrderDocRespondentSolTwo;
        return this;
    }

    public GeneralApplicationCaseData requestForInformationDocument(List<Element<CaseDocument>> requestForInformationDocument) {
        this.requestForInformationDocument = requestForInformationDocument;
        return this;
    }

    public GeneralApplicationCaseData hearingOrderDocument(List<Element<CaseDocument>> hearingOrderDocument) {
        this.hearingOrderDocument = hearingOrderDocument;
        return this;
    }

    public GeneralApplicationCaseData hearingNoticeDocument(List<Element<CaseDocument>> hearingNoticeDocument) {
        this.hearingNoticeDocument = hearingNoticeDocument;
        return this;
    }

    public GeneralApplicationCaseData hearingNoticeDocStaff(List<Element<CaseDocument>> hearingNoticeDocStaff) {
        this.hearingNoticeDocStaff = hearingNoticeDocStaff;
        return this;
    }

    public GeneralApplicationCaseData hearingNoticeDocClaimant(List<Element<CaseDocument>> hearingNoticeDocClaimant) {
        this.hearingNoticeDocClaimant = hearingNoticeDocClaimant;
        return this;
    }

    public GeneralApplicationCaseData hearingNoticeDocRespondentSol(List<Element<CaseDocument>> hearingNoticeDocRespondentSol) {
        this.hearingNoticeDocRespondentSol = hearingNoticeDocRespondentSol;
        return this;
    }

    public GeneralApplicationCaseData hearingNoticeDocRespondentSolTwo(List<Element<CaseDocument>> hearingNoticeDocRespondentSolTwo) {
        this.hearingNoticeDocRespondentSolTwo = hearingNoticeDocRespondentSolTwo;
        return this;
    }

    public GeneralApplicationCaseData writtenRepSequentialDocument(List<Element<CaseDocument>> writtenRepSequentialDocument) {
        this.writtenRepSequentialDocument = writtenRepSequentialDocument;
        return this;
    }

    public GeneralApplicationCaseData writtenRepConcurrentDocument(List<Element<CaseDocument>> writtenRepConcurrentDocument) {
        this.writtenRepConcurrentDocument = writtenRepConcurrentDocument;
        return this;
    }

    public GeneralApplicationCaseData businessProcess(BusinessProcess businessProcess) {
        this.businessProcess = businessProcess;
        return this;
    }

    public GeneralApplicationCaseData orderCourtOwnInitiativeListForHearing(GAOrderCourtOwnInitiativeGAspec orderCourtOwnInitiativeListForHearing) {
        this.orderCourtOwnInitiativeListForHearing = orderCourtOwnInitiativeListForHearing;
        return this;
    }

    public GeneralApplicationCaseData orderWithoutNoticeListForHearing(GAOrderWithoutNoticeGAspec orderWithoutNoticeListForHearing) {
        this.orderWithoutNoticeListForHearing = orderWithoutNoticeListForHearing;
        return this;
    }

    public GeneralApplicationCaseData orderCourtOwnInitiativeForWrittenRep(GAOrderCourtOwnInitiativeGAspec orderCourtOwnInitiativeForWrittenRep) {
        this.orderCourtOwnInitiativeForWrittenRep = orderCourtOwnInitiativeForWrittenRep;
        return this;
    }

    public GeneralApplicationCaseData orderWithoutNoticeForWrittenRep(GAOrderWithoutNoticeGAspec orderWithoutNoticeForWrittenRep) {
        this.orderWithoutNoticeForWrittenRep = orderWithoutNoticeForWrittenRep;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderMadeSelection(YesOrNo assistedOrderMadeSelection) {
        this.assistedOrderMadeSelection = assistedOrderMadeSelection;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails assistedOrderMadeDateHeardDetails) {
        this.assistedOrderMadeDateHeardDetails = assistedOrderMadeDateHeardDetails;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderJudgeHeardFrom(List<FinalOrderShowToggle> assistedOrderJudgeHeardFrom) {
        this.assistedOrderJudgeHeardFrom = assistedOrderJudgeHeardFrom;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderRepresentation(AssistedOrderHeardRepresentation assistedOrderRepresentation) {
        this.assistedOrderRepresentation = assistedOrderRepresentation;
        return this;
    }

    public GeneralApplicationCaseData typeRepresentationJudgePapersList(List<FinalOrderConsideredToggle> typeRepresentationJudgePapersList) {
        this.typeRepresentationJudgePapersList = typeRepresentationJudgePapersList;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderRecitals(List<FinalOrderShowToggle> assistedOrderRecitals) {
        this.assistedOrderRecitals = assistedOrderRecitals;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderRecitalsRecorded(AssistedOrderRecitalRecord assistedOrderRecitalsRecorded) {
        this.assistedOrderRecitalsRecorded = assistedOrderRecitalsRecorded;
        return this;
    }

    public GeneralApplicationCaseData assistedCostTypes(AssistedCostTypesList assistedCostTypes) {
        this.assistedCostTypes = assistedCostTypes;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderMakeAnOrderForCosts(AssistedOrderCost assistedOrderMakeAnOrderForCosts) {
        this.assistedOrderMakeAnOrderForCosts = assistedOrderMakeAnOrderForCosts;
        return this;
    }

    public GeneralApplicationCaseData publicFundingCostsProtection(YesOrNo publicFundingCostsProtection) {
        this.publicFundingCostsProtection = publicFundingCostsProtection;
        return this;
    }

    public GeneralApplicationCaseData costReservedDetails(DetailText costReservedDetails) {
        this.costReservedDetails = costReservedDetails;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderCostsBespoke(BeSpokeCostDetailText assistedOrderCostsBespoke) {
        this.assistedOrderCostsBespoke = assistedOrderCostsBespoke;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderOrderedThatText(String assistedOrderOrderedThatText) {
        this.assistedOrderOrderedThatText = assistedOrderOrderedThatText;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderFurtherHearingToggle(List<FinalOrderShowToggle> assistedOrderFurtherHearingToggle) {
        this.assistedOrderFurtherHearingToggle = assistedOrderFurtherHearingToggle;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails assistedOrderFurtherHearingDetails) {
        this.assistedOrderFurtherHearingDetails = assistedOrderFurtherHearingDetails;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderAppealToggle(List<FinalOrderShowToggle> assistedOrderAppealToggle) {
        this.assistedOrderAppealToggle = assistedOrderAppealToggle;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderAppealDetails(AssistedOrderAppealDetails assistedOrderAppealDetails) {
        this.assistedOrderAppealDetails = assistedOrderAppealDetails;
        return this;
    }

    public GeneralApplicationCaseData orderMadeOnOption(OrderMadeOnTypes orderMadeOnOption) {
        this.orderMadeOnOption = orderMadeOnOption;
        return this;
    }

    public GeneralApplicationCaseData orderMadeOnOwnInitiative(DetailTextWithDate orderMadeOnOwnInitiative) {
        this.orderMadeOnOwnInitiative = orderMadeOnOwnInitiative;
        return this;
    }

    public GeneralApplicationCaseData orderMadeOnWithOutNotice(DetailTextWithDate orderMadeOnWithOutNotice) {
        this.orderMadeOnWithOutNotice = orderMadeOnWithOutNotice;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderGiveReasonsYesNo(YesOrNo assistedOrderGiveReasonsYesNo) {
        this.assistedOrderGiveReasonsYesNo = assistedOrderGiveReasonsYesNo;
        return this;
    }

    public GeneralApplicationCaseData assistedOrderGiveReasonsDetails(AssistedOrderGiveReasonsDetails assistedOrderGiveReasonsDetails) {
        this.assistedOrderGiveReasonsDetails = assistedOrderGiveReasonsDetails;
        return this;
    }

    public GeneralApplicationCaseData gaRespondentDebtorOffer(GARespondentDebtorOfferGAspec gaRespondentDebtorOffer) {
        this.gaRespondentDebtorOffer = gaRespondentDebtorOffer;
        return this;
    }

    public GeneralApplicationCaseData gaRespondentConsent(YesOrNo gaRespondentConsent) {
        this.gaRespondentConsent = gaRespondentConsent;
        return this;
    }

    public GeneralApplicationCaseData applicationTypes(String applicationTypes) {
        this.applicationTypes = applicationTypes;
        return this;
    }

    public GeneralApplicationCaseData parentCaseReference(String parentCaseReference) {
        this.parentCaseReference = parentCaseReference;
        return this;
    }

    public GeneralApplicationCaseData judgeTitle(String judgeTitle) {
        this.judgeTitle = judgeTitle;
        return this;
    }

    public GeneralApplicationCaseData uploadDocument(List<Element<UploadDocumentByType>> uploadDocument) {
        this.uploadDocument = uploadDocument;
        return this;
    }

    public GeneralApplicationCaseData applicant1Represented(YesOrNo applicant1Represented) {
        this.applicant1Represented = applicant1Represented;
        return this;
    }

    public GeneralApplicationCaseData respondent1Represented(YesOrNo respondent1Represented) {
        this.respondent1Represented = respondent1Represented;
        return this;
    }

    public GeneralApplicationCaseData specRespondent1Represented(YesOrNo specRespondent1Represented) {
        this.specRespondent1Represented = specRespondent1Represented;
        return this;
    }

    public GeneralApplicationCaseData isGaApplicantLip(YesOrNo isGaApplicantLip) {
        this.isGaApplicantLip = isGaApplicantLip;
        return this;
    }

    public GeneralApplicationCaseData isGaRespondentOneLip(YesOrNo isGaRespondentOneLip) {
        this.isGaRespondentOneLip = isGaRespondentOneLip;
        return this;
    }

    public GeneralApplicationCaseData isGaRespondentTwoLip(YesOrNo isGaRespondentTwoLip) {
        this.isGaRespondentTwoLip = isGaRespondentTwoLip;
        return this;
    }

    public GeneralApplicationCaseData isApplicantResponded(YesOrNo isApplicantResponded) {
        this.isApplicantResponded = isApplicantResponded;
        return this;
    }

    public GeneralApplicationCaseData isRespondentResponded(YesOrNo isRespondentResponded) {
        this.isRespondentResponded = isRespondentResponded;
        return this;
    }

    public GeneralApplicationCaseData claimantUserDetails(IdamUserDetails claimantUserDetails) {
        this.claimantUserDetails = claimantUserDetails;
        return this;
    }

    public GeneralApplicationCaseData defendantUserDetails(IdamUserDetails defendantUserDetails) {
        this.defendantUserDetails = defendantUserDetails;
        return this;
    }

    public GeneralApplicationCaseData generalAppHelpWithFees(HelpWithFees generalAppHelpWithFees) {
        this.generalAppHelpWithFees = generalAppHelpWithFees;
        return this;
    }

    public GeneralApplicationCaseData gaAdditionalHelpWithFees(HelpWithFees gaAdditionalHelpWithFees) {
        this.gaAdditionalHelpWithFees = gaAdditionalHelpWithFees;
        return this;
    }

    public GeneralApplicationCaseData hwfFeeType(FeeType hwfFeeType) {
        this.hwfFeeType = hwfFeeType;
        return this;
    }

    public GeneralApplicationCaseData gaHwfDetails(HelpWithFeesDetails gaHwfDetails) {
        this.gaHwfDetails = gaHwfDetails;
        return this;
    }

    public GeneralApplicationCaseData additionalHwfDetails(HelpWithFeesDetails additionalHwfDetails) {
        this.additionalHwfDetails = additionalHwfDetails;
        return this;
    }

    public GeneralApplicationCaseData helpWithFeesMoreInformationGa(HelpWithFeesMoreInformation helpWithFeesMoreInformationGa) {
        this.helpWithFeesMoreInformationGa = helpWithFeesMoreInformationGa;
        return this;
    }

    public GeneralApplicationCaseData helpWithFeesMoreInformationAdditional(HelpWithFeesMoreInformation helpWithFeesMoreInformationAdditional) {
        this.helpWithFeesMoreInformationAdditional = helpWithFeesMoreInformationAdditional;
        return this;
    }

    public GeneralApplicationCaseData generalAppAskForCosts(YesOrNo generalAppAskForCosts) {
        this.generalAppAskForCosts = generalAppAskForCosts;
        return this;
    }

    public GeneralApplicationCaseData applicationFeeAmountInPence(BigDecimal applicationFeeAmountInPence) {
        this.applicationFeeAmountInPence = applicationFeeAmountInPence;
        return this;
    }

    public GeneralApplicationCaseData certOfSC(CertOfSC certOfSC) {
        this.certOfSC = certOfSC;
        return this;
    }

    public GeneralApplicationCaseData gaWaTrackLabel(String gaWaTrackLabel) {
        this.gaWaTrackLabel = gaWaTrackLabel;
        return this;
    }

    public GeneralApplicationCaseData emailPartyReference(String emailPartyReference) {
        this.emailPartyReference = emailPartyReference;
        return this;
    }

    public GeneralApplicationCaseData caseDocuments(List<Value<Document>> caseDocuments) {
        this.caseDocuments = caseDocuments;
        return this;
    }

    public GeneralApplicationCaseData caseDocument1Name(String caseDocument1Name) {
        this.caseDocument1Name = caseDocument1Name;
        return this;
    }

    public GeneralApplicationCaseData caseBundles(List<IdValue<Bundle>> caseBundles) {
        this.caseBundles = caseBundles;
        return this;
    }

    public GeneralApplicationCaseData preTranslationGaDocuments(List<Element<CaseDocument>> preTranslationGaDocuments) {
        this.preTranslationGaDocuments = preTranslationGaDocuments;
        return this;
    }

    public GeneralApplicationCaseData preTranslationGaDocumentType(PreTranslationGaDocumentType preTranslationGaDocumentType) {
        this.preTranslationGaDocumentType = preTranslationGaDocumentType;
        return this;
    }

    public GeneralApplicationCaseData preTranslationGaDocsApplicant(List<Element<CaseDocument>> preTranslationGaDocsApplicant) {
        this.preTranslationGaDocsApplicant = preTranslationGaDocsApplicant;
        return this;
    }

    public GeneralApplicationCaseData preTranslationGaDocsRespondent(List<Element<CaseDocument>> preTranslationGaDocsRespondent) {
        this.preTranslationGaDocsRespondent = preTranslationGaDocsRespondent;
        return this;
    }

    public GeneralApplicationCaseData applicant1(GeneralApplicationParty applicant1) {
        this.applicant1 = applicant1;
        return this;
    }

    public GeneralApplicationCaseData respondent1(GeneralApplicationParty respondent1) {
        this.respondent1 = respondent1;
        return this;
    }

    public GeneralApplicationCaseData translatedDocuments(List<Element<TranslatedDocument>> translatedDocuments) {
        this.translatedDocuments = translatedDocuments;
        return this;
    }

    public GeneralApplicationCaseData translatedDocumentsBulkPrint(List<Element<TranslatedDocument>> translatedDocumentsBulkPrint) {
        this.translatedDocumentsBulkPrint = translatedDocumentsBulkPrint;
        return this;
    }

    public GeneralApplicationCaseData generalAppParentCaseLink(GeneralAppParentCaseLink generalAppParentCaseLink) {
        this.generalAppParentCaseLink = generalAppParentCaseLink;
        return this;
    }

    public GeneralApplicationCaseData gaHearingNoticeApplication(GAHearingNoticeApplication gaHearingNoticeApplication) {
        this.gaHearingNoticeApplication = gaHearingNoticeApplication;
        return this;
    }

    public GeneralApplicationCaseData gaHearingNoticeDetail(GAHearingNoticeDetail gaHearingNoticeDetail) {
        this.gaHearingNoticeDetail = gaHearingNoticeDetail;
        return this;
    }

    public GeneralApplicationCaseData gaHearingNoticeInformation(String gaHearingNoticeInformation) {
        this.gaHearingNoticeInformation = gaHearingNoticeInformation;
        return this;
    }

    public GeneralApplicationCaseData feePaymentOutcomeDetails(FeePaymentOutcomeDetails feePaymentOutcomeDetails) {
        this.feePaymentOutcomeDetails = feePaymentOutcomeDetails;
        return this;
    }

    public GeneralApplicationCaseData generalAppAddlnInfoText(String generalAppAddlnInfoText) {
        this.generalAppAddlnInfoText = generalAppAddlnInfoText;
        return this;
    }

    public GeneralApplicationCaseData generalAppWrittenRepText(String generalAppWrittenRepText) {
        this.generalAppWrittenRepText = generalAppWrittenRepText;
        return this;
    }

    public GeneralApplicationCaseData respondentResponseDeadlineChecked(YesOrNo respondentResponseDeadlineChecked) {
        this.respondentResponseDeadlineChecked = respondentResponseDeadlineChecked;
        return this;
    }

    public GeneralApplicationCaseData caseNameGaInternal(String caseNameGaInternal) {
        this.caseNameGaInternal = caseNameGaInternal;
        return this;
    }

    public GeneralApplicationCaseData claimantBilingualLanguagePreference(String claimantBilingualLanguagePreference) {
        this.claimantBilingualLanguagePreference = claimantBilingualLanguagePreference;
        return this;
    }

    public GeneralApplicationCaseData respondent1LiPResponse(RespondentLiPResponse respondent1LiPResponse) {
        this.respondent1LiPResponse = respondent1LiPResponse;
        return this;
    }

    public GeneralApplicationCaseData bilingualHint(YesOrNo bilingualHint) {
        this.bilingualHint = bilingualHint;
        return this;
    }

    public GeneralApplicationCaseData applicantBilingualLanguagePreference(YesOrNo applicantBilingualLanguagePreference) {
        this.applicantBilingualLanguagePreference = applicantBilingualLanguagePreference;
        return this;
    }

    public GeneralApplicationCaseData respondentBilingualLanguagePreference(YesOrNo respondentBilingualLanguagePreference) {
        this.respondentBilingualLanguagePreference = respondentBilingualLanguagePreference;
        return this;
    }

    public GeneralApplicationCaseData generalAppSubmittedDateGAspec(LocalDateTime generalAppSubmittedDateGAspec) {
        this.generalAppSubmittedDateGAspec = generalAppSubmittedDateGAspec;
        return this;
    }

    @JsonIgnore
    public boolean isHWFTypeApplication() {
        return getHwfFeeType() == FeeType.APPLICATION;
    }

    @JsonIgnore
    public boolean isHWFTypeAdditional() {
        return getHwfFeeType() == FeeType.ADDITIONAL;
    }

    @JsonIgnore
    public boolean isAdditionalFeeRequested() {
        return getGeneralAppPBADetails() != null && getGeneralAppPBADetails().getAdditionalPaymentServiceRef() != null;
    }

    public boolean hasNoOngoingBusinessProcess() {
        return businessProcess == null
            || businessProcess.getStatus() == null
            || businessProcess.getStatus() == FINISHED;
    }

    @JsonIgnore
    public boolean isUrgent() {
        return Optional.ofNullable(this.getGeneralAppUrgencyRequirement())
            .map(GAUrgencyRequirement::getGeneralAppUrgency)
            .filter(urgency -> urgency == YES)
            .isPresent();
    }

    @JsonIgnore
    public boolean isApplicantBilingual() {
        return Objects.nonNull(applicantBilingualLanguagePreference)
            && applicantBilingualLanguagePreference.equals(YES);
    }

    @JsonIgnore
    public boolean isRespondentBilingual() {
        return Objects.nonNull(respondentBilingualLanguagePreference)
            && respondentBilingualLanguagePreference.equals(YES);
    }

    @JsonIgnore
    public boolean identifyParentClaimantIsApplicant(GeneralApplicationCaseData caseData) {

        return caseData.getParentClaimantIsApplicant() == null
            || YES.equals(caseData.getParentClaimantIsApplicant());

    }

    @JsonIgnore
    public String getPartyName(boolean parentClaimantIsApplicant, FlowFlag userType, GeneralApplicationCaseData civilCaseData) {

        if (userType.equals(POST_JUDGE_ORDER_LIP_APPLICANT)) {
            return parentClaimantIsApplicant
                ? civilCaseData.getApplicant1().getPartyName()
                : civilCaseData.getRespondent1().getPartyName();
        } else {
            return parentClaimantIsApplicant
                ? civilCaseData.getRespondent1().getPartyName()
                : civilCaseData.getApplicant1().getPartyName();
        }
    }

    @JsonIgnore
    public String partyAddressAddressLine1(boolean parentClaimantIsApplicant, FlowFlag userType, GeneralApplicationCaseData civilCaseData) {

        if (userType.equals(POST_JUDGE_ORDER_LIP_APPLICANT)) {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getAddressLine1())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY)
                : ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getAddressLine1())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY);
        } else {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getAddressLine1())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY)
                : ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getAddressLine1())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY);
        }
    }

    @JsonIgnore
    public String partyAddressAddressLine2(boolean parentClaimantIsApplicant, FlowFlag userType, GeneralApplicationCaseData civilCaseData) {
        if (userType.equals(POST_JUDGE_ORDER_LIP_APPLICANT)) {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getAddressLine2())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY)
                : ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getAddressLine2())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY);
        } else {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getAddressLine2())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY)
                : ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getAddressLine2())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY);
        }
    }

    @JsonIgnore
    public String partyAddressAddressLine3(boolean parentClaimantIsApplicant, FlowFlag userType, GeneralApplicationCaseData civilCaseData) {
        if (userType.equals(POST_JUDGE_ORDER_LIP_APPLICANT)) {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getAddressLine3())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY)
                : ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getAddressLine3())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY);
        } else {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getAddressLine3())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY)
                : ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getAddressLine3())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY);
        }
    }

    @JsonIgnore
    public String partyAddressPostCode(boolean parentClaimantIsApplicant, FlowFlag userType, GeneralApplicationCaseData civilCaseData) {
        if (userType.equals(POST_JUDGE_ORDER_LIP_APPLICANT)) {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getPostCode())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY)
                : ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getPostCode())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY);
        } else {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getPostCode())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY)
                : ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getPostCode())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY);
        }
    }

    @JsonIgnore
    public String partyAddressPostTown(boolean parentClaimantIsApplicant, FlowFlag userType, GeneralApplicationCaseData civilCaseData) {
        if (userType.equals(POST_JUDGE_ORDER_LIP_APPLICANT)) {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getPostTown())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY)
                : ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getPostTown())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY);
        } else {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getPostTown())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY)
                : ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getPostTown())
                .orElse(org.apache.commons.lang3.StringUtils.EMPTY);
        }
    }

    @JsonIgnore
    public boolean claimIssueFeePaymentDoneWithHWF(GeneralApplicationCaseData caseData) {
        return Objects.nonNull(caseData.getGeneralAppHelpWithFees())
            && YES.equals(caseData.getGeneralAppHelpWithFees().getHelpWithFee())
            && Objects.nonNull(caseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber());
    }

    @JsonIgnore
    public boolean judgeHasMadeAnOrder() {
        return (Objects.nonNull(this.getJudicialDecision()))
            && (this.getJudicialDecision().getDecision().equals(GAJudgeDecisionOption.MAKE_AN_ORDER)
            || this.getJudicialDecision().getDecision().equals(GAJudgeDecisionOption.FREE_FORM_ORDER)
            || this.getJudicialDecision().getDecision().equals(GAJudgeDecisionOption.LIST_FOR_A_HEARING));
    }

    @JsonIgnore
    public boolean gaApplicationFeeFullRemissionNotGrantedHWF(GeneralApplicationCaseData caseData) {
        return (Objects.nonNull(caseData.getFeePaymentOutcomeDetails())
            && caseData.getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForGa() == NO)
            || (Objects.nonNull(caseData.getGaHwfDetails())
            && caseData.getGaHwfDetails().getHwfCaseEvent() == CaseEvent.NO_REMISSION_HWF_GA);
    }

    @JsonIgnore
    public boolean gaAdditionalFeeFullRemissionNotGrantedHWF(GeneralApplicationCaseData caseData) {
        return (Objects.nonNull(caseData.getFeePaymentOutcomeDetails())
            && caseData.getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForAdditionalFee() == NO)
            || (Objects.nonNull(caseData.getAdditionalHwfDetails())
            && caseData.getAdditionalHwfDetails().getHwfCaseEvent() == CaseEvent.NO_REMISSION_HWF_GA);
    }

    @JsonIgnore
    public boolean isApplicantNotRepresented() {
        return this.applicant1Represented == NO;
    }

    @JsonIgnore
    public boolean isRespondent1NotRepresented() {
        return NO.equals(getRespondent1Represented());
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
    public boolean isApplicationBilingual() {
        return ((this.getIsGaApplicantLip() == YES && this.isApplicantBilingual())
            || (this.getIsGaRespondentOneLip() == YES && this.isRespondentBilingual()));
    }

    @JsonIgnore
    public String getDefendantBilingualLanguagePreference() {
        return Optional.ofNullable(getRespondent1LiPResponse())
            .map(RespondentLiPResponse::getRespondent1ResponseLanguage)
            .orElse(null);
    }
}
