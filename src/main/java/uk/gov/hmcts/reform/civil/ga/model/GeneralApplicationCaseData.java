package uk.gov.hmcts.reform.civil.ga.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
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
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
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
@SuperBuilder(toBuilder = true)
@Jacksonized
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
    private GAPbaDetails generalAppPBADetails;
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
    @Builder.Default
    private List<Element<CaseDocument>> generalOrderDocument = new ArrayList<>();
    private List<Element<CaseDocument>> generalOrderDocStaff;
    private List<Element<CaseDocument>> generalOrderDocClaimant;
    private List<Element<CaseDocument>> generalOrderDocRespondentSol;
    private List<Element<CaseDocument>> generalOrderDocRespondentSolTwo;
    @Builder.Default
    private List<Element<CaseDocument>> gaDraftDocument = new ArrayList<>();
    private List<Element<CaseDocument>> gaDraftDocStaff;
    private List<Element<CaseDocument>> gaDraftDocClaimant;
    private List<Element<CaseDocument>> gaDraftDocRespondentSol;
    private List<Element<CaseDocument>> gaDraftDocRespondentSolTwo;
    @Builder.Default
    private List<Element<CaseDocument>> consentOrderDocument = new ArrayList<>();
    @Builder.Default
    private List<Element<CaseDocument>> dismissalOrderDocument = new ArrayList<>();
    private List<Element<CaseDocument>> dismissalOrderDocStaff;
    private List<Element<CaseDocument>> dismissalOrderDocClaimant;
    private List<Element<CaseDocument>> dismissalOrderDocRespondentSol;
    private List<Element<CaseDocument>> dismissalOrderDocRespondentSolTwo;
    @Builder.Default
    private List<Element<CaseDocument>> directionOrderDocument = new ArrayList<>();
    private List<Element<CaseDocument>> directionOrderDocStaff;
    private List<Element<CaseDocument>> directionOrderDocClaimant;
    private List<Element<CaseDocument>> directionOrderDocRespondentSol;
    private List<Element<CaseDocument>> directionOrderDocRespondentSolTwo;
    @Builder.Default
    private List<Element<CaseDocument>> requestForInformationDocument = new ArrayList<>();
    @Builder.Default
    private List<Element<CaseDocument>> hearingOrderDocument = new ArrayList<>();
    @Builder.Default
    private List<Element<CaseDocument>> hearingNoticeDocument = new ArrayList<>();
    private List<Element<CaseDocument>> hearingNoticeDocStaff;
    private List<Element<CaseDocument>> hearingNoticeDocClaimant;
    private List<Element<CaseDocument>> hearingNoticeDocRespondentSol;
    private List<Element<CaseDocument>> hearingNoticeDocRespondentSolTwo;
    @Builder.Default
    private List<Element<CaseDocument>> writtenRepSequentialDocument = new ArrayList<>();
    @Builder.Default
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
    @Builder.Default
    private List<Value<Document>> caseDocuments = new ArrayList<>();
    private String caseDocument1Name;
    @Builder.Default
    private List<IdValue<Bundle>> caseBundles = new ArrayList<>();
    @Builder.Default
    private List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>();
    private PreTranslationGaDocumentType preTranslationGaDocumentType;
    @Builder.Default
    private List<Element<CaseDocument>> preTranslationGaDocsApplicant = new ArrayList<>();
    @Builder.Default
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
