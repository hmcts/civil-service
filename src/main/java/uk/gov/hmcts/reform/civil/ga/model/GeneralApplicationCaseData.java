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
import uk.gov.hmcts.reform.ccd.model.SolicitorDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList;
import uk.gov.hmcts.reform.civil.ga.enums.dq.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.ga.enums.dq.FinalOrderConsideredToggle;
import uk.gov.hmcts.reform.civil.ga.enums.dq.FinalOrderShowToggle;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
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
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationParty;
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
    private final Long ccdCaseReference;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final CaseState ccdState;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final LocalDateTime createdDate;
    private final String detailsOfClaim;
    private final YesOrNo addApplicant2;
    private final GAApplicationType generalAppType;
    private final GARespondentOrderAgreement generalAppRespondentAgreement;
    private final YesOrNo generalAppConsentOrder;
    private final GAPbaDetails generalAppPBADetails;
    private final String generalAppDetailsOfOrder;
    private final List<Element<String>> generalAppDetailsOfOrderColl;
    private final String generalAppReasonsOfOrder;
    private final List<Element<String>> generalAppReasonsOfOrderColl;
    private final String legacyCaseReference;
    private final LocalDateTime notificationDeadline;
    private final LocalDate submittedOn;
    private final LocalDateTime generalAppNotificationDeadlineDate;
    private final GAInformOtherParty generalAppInformOtherParty;
    private final GAUrgencyRequirement generalAppUrgencyRequirement;
    private final GAStatementOfTruth generalAppStatementOfTruth;
    private final GAStatementOfTruth generalAppResponseStatementOfTruth;
    private final GAHearingDetails generalAppHearingDetails;
    private final GASolicitorDetailsGAspec generalAppApplnSolicitor;
    private final List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors;
    private final List<Element<GASolicitorDetailsGAspec>> generalAppApplicantAddlSolicitors;
    private final GAHearingDetails hearingDetailsResp;
    private final GARespondentRepresentative generalAppRespondent1Representative;
    private final String generalAppRespondReason;
    private final String generalAppRespondConsentReason;
    private final List<Element<CaseDocument>> originalDocumentsBulkPrint;
    private final List<Element<Document>> generalAppRespondDocument;
    private final List<Element<Document>> generalAppRespondConsentDocument;
    private final List<Element<Document>> generalAppRespondDebtorDocument;
    @Deprecated
    private final List<Element<CaseDocument>> gaRespondDoc;
    private final List<Element<CaseDocument>> gaAddlDoc;
    private final List<Element<CaseDocument>> gaAddlDocStaff;
    private final List<Element<CaseDocument>> gaAddlDocClaimant;
    private final List<Element<CaseDocument>> gaAddlDocRespondentSol;
    private final List<Element<CaseDocument>> gaAddlDocRespondentSolTwo;
    private final List<Element<CaseDocument>> gaAddlDocBundle;
    private final LocalDateTime caseDocumentUploadDateRes;
    private final LocalDateTime caseDocumentUploadDate;
    private final YesOrNo isDocumentVisible;
    private final YesOrNo isMultiParty;
    private final YesOrNo parentClaimantIsApplicant;
    private final CaseLink caseLink;
    private final IdamUserDetails applicantSolicitor1UserDetails;
    private final IdamUserDetails civilServiceUserRoles;
    private final List<Element<Document>> generalAppEvidenceDocument;
    private final List<Element<Document>> gaEvidenceDocStaff;
    private final List<Element<Document>> gaEvidenceDocClaimant;
    private final List<Element<Document>> gaEvidenceDocRespondentSol;
    private final List<Element<Document>> gaEvidenceDocRespondentSolTwo;
    private final List<Element<GeneralApplication>> generalApplications;
    private final List<Element<GeneralApplicationsDetails>> claimantGaAppDetails;
    private final List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection;
    private final List<Element<GeneralApplicationsDetails>> gaDetailsTranslationCollection;
    private final List<Element<GADetailsRespondentSol>> respondentSolGaAppDetails;
    private final List<Element<GADetailsRespondentSol>> respondentSolTwoGaAppDetails;
    private final GAJudicialDecision judicialDecision;
    private final List<Element<SolicitorDetails>> applicantSolicitors;
    private final List<Element<SolicitorDetails>> defendantSolicitors;
    private final List<Element<GARespondentResponse>> respondentsResponses;
    private final YesOrNo applicationIsCloaked;
    private final YesOrNo applicationIsUncloakedOnce;
    private final GAJudicialMakeAnOrder judicialDecisionMakeOrder;
    private final Document judicialMakeOrderDocPreview;
    private final Document judicialListHearingDocPreview;
    private final Document judicialWrittenRepDocPreview;
    private final Document judicialRequestMoreInfoDocPreview;
    private final Document consentOrderDocPreview;
    private final GAJudicialRequestMoreInfo judicialDecisionRequestMoreInfo;
    private final GAApproveConsentOrder approveConsentOrder;
    private final GAJudicialWrittenRepresentations judicialDecisionMakeAnOrderForWrittenRepresentations;
    private final String judgeRecitalText;
    private final String directionInRelationToHearingText;
    private final GAJudgesHearingListGAspec judicialListForHearing;
    private final String applicantPartyName;
    private final String gaApplicantDisplayName;
    private final String claimant1PartyName;
    private final String claimant2PartyName;
    private final String defendant1PartyName;
    private final String defendant2PartyName;
    private final CaseLocationCivil caseManagementLocation;
    private final YesOrNo isCcmccLocation;
    private final GACaseManagementCategory caseManagementCategory;
    private final String judicialGeneralHearingOrderRecital;
    private final String judicialGOHearingDirections;
    private final String judicialHearingGeneralOrderHearingText;
    private final String judicialGeneralOrderHearingEstimationTimeText;
    private final String judicialHearingGOHearingReqText;
    private final String judicialSequentialDateText;
    private final String judicialApplicanSequentialDateText;
    private final String judicialConcurrentDateText;
    private final List<Element<Document>> generalAppWrittenRepUpload;
    @Deprecated
    private final List<Element<Document>> gaWrittenRepDocList;
    private final List<Element<Document>> generalAppDirOrderUpload;
    private final List<Element<Document>> gaDirectionDocList;
    private final List<Element<Document>> generalAppAddlnInfoUpload;
    @Deprecated
    private final List<Element<Document>> gaAddlnInfoList;
    @Deprecated
    private final List<Element<Document>> gaRespDocument;
    @Deprecated
    private final List<Element<Document>> gaRespDocStaff;
    @Deprecated
    private final List<Element<Document>> gaRespDocClaimant;
    @Deprecated
    private final List<Element<Document>> gaRespDocRespondentSol;
    @Deprecated
    private final List<Element<Document>> gaRespDocRespondentSolTwo;
    private final String gaRespondentDetails;
    private final LocalDate issueDate;
    private final String generalAppSuperClaimType;
    private final GAMakeApplicationAvailableCheck makeAppVisibleToRespondents;
    private final String respondentSolicitor1EmailAddress;
    private final String respondentSolicitor2EmailAddress;
    private final OrganisationPolicy applicant1OrganisationPolicy;
    private final OrganisationPolicy respondent1OrganisationPolicy;
    private final OrganisationPolicy respondent2OrganisationPolicy;
    private final String respondent1OrganisationIDCopy;
    private final String respondent2OrganisationIDCopy;
    private final YesOrNo respondent2SameLegalRepresentative;
    private final GAReferToJudgeGAspec referToJudge;
    private final GAReferToLegalAdvisorGAspec referToLegalAdvisor;
    private final LocalDateTime applicationClosedDate;
    private final LocalDateTime applicationTakenOfflineDate;
    private final String locationName;
    private final GAByCourtsInitiativeGAspec judicialByCourtsInitiativeListForHearing;
    private final GAByCourtsInitiativeGAspec judicialByCourtsInitiativeForWrittenRep;
    private final YesOrNo showRequestInfoPreviewDoc;
    private final String migrationId;
    private final String caseNameHmctsInternal;
    private final FinalOrderSelection finalOrderSelection;
    private final String freeFormRecitalText;
    private final String freeFormOrderedText;
    private final OrderOnCourtsList orderOnCourtsList;
    private final FreeFormOrderValues orderOnCourtInitiative;
    private final FreeFormOrderValues orderWithoutNotice;
    private final Document gaFinalOrderDocPreview;
    private final LocalDateTime mainCaseSubmittedDate;
    @JsonProperty("CaseAccessCategory")
    private final CaseCategory caseAccessCategory;
    private final YesOrNo generalAppVaryJudgementType;
    private final Document generalAppN245FormUpload;
    private final GAHearingDateGAspec generalAppHearingDate;
    //PDF Documents
    @Builder.Default
    private final List<Element<CaseDocument>> generalOrderDocument = new ArrayList<>();
    private final List<Element<CaseDocument>> generalOrderDocStaff;
    private final List<Element<CaseDocument>> generalOrderDocClaimant;
    private final List<Element<CaseDocument>> generalOrderDocRespondentSol;
    private final List<Element<CaseDocument>> generalOrderDocRespondentSolTwo;
    @Builder.Default
    private final List<Element<CaseDocument>> gaDraftDocument = new ArrayList<>();
    private final List<Element<CaseDocument>> gaDraftDocStaff;
    private final List<Element<CaseDocument>> gaDraftDocClaimant;
    private final List<Element<CaseDocument>> gaDraftDocRespondentSol;
    private final List<Element<CaseDocument>> gaDraftDocRespondentSolTwo;
    @Builder.Default
    private final List<Element<CaseDocument>> consentOrderDocument = new ArrayList<>();
    @Builder.Default
    private final List<Element<CaseDocument>> dismissalOrderDocument = new ArrayList<>();
    private final List<Element<CaseDocument>> dismissalOrderDocStaff;
    private final List<Element<CaseDocument>> dismissalOrderDocClaimant;
    private final List<Element<CaseDocument>> dismissalOrderDocRespondentSol;
    private final List<Element<CaseDocument>> dismissalOrderDocRespondentSolTwo;
    @Builder.Default
    private final List<Element<CaseDocument>> directionOrderDocument = new ArrayList<>();
    private final List<Element<CaseDocument>> directionOrderDocStaff;
    private final List<Element<CaseDocument>> directionOrderDocClaimant;
    private final List<Element<CaseDocument>> directionOrderDocRespondentSol;
    private final List<Element<CaseDocument>> directionOrderDocRespondentSolTwo;
    @Builder.Default
    private final List<Element<CaseDocument>> requestForInformationDocument = new ArrayList<>();
    @Builder.Default
    private final List<Element<CaseDocument>> hearingOrderDocument = new ArrayList<>();
    @Builder.Default
    private final List<Element<CaseDocument>> hearingNoticeDocument = new ArrayList<>();
    private final List<Element<CaseDocument>> hearingNoticeDocStaff;
    private final List<Element<CaseDocument>> hearingNoticeDocClaimant;
    private final List<Element<CaseDocument>> hearingNoticeDocRespondentSol;
    private final List<Element<CaseDocument>> hearingNoticeDocRespondentSolTwo;
    @Builder.Default
    private final List<Element<CaseDocument>> writtenRepSequentialDocument = new ArrayList<>();
    @Builder.Default
    private final List<Element<CaseDocument>> writtenRepConcurrentDocument = new ArrayList<>();
    private final BusinessProcess businessProcess;
    private final GAOrderCourtOwnInitiativeGAspec orderCourtOwnInitiativeListForHearing;
    private final GAOrderWithoutNoticeGAspec orderWithoutNoticeListForHearing;
    private final GAOrderCourtOwnInitiativeGAspec orderCourtOwnInitiativeForWrittenRep;
    private final GAOrderWithoutNoticeGAspec orderWithoutNoticeForWrittenRep;
    private final YesOrNo assistedOrderMadeSelection;
    private final AssistedOrderMadeDateHeardDetails assistedOrderMadeDateHeardDetails;
    private final List<FinalOrderShowToggle> assistedOrderJudgeHeardFrom;
    private final AssistedOrderHeardRepresentation assistedOrderRepresentation;
    private final List<FinalOrderConsideredToggle> typeRepresentationJudgePapersList;
    private final List<FinalOrderShowToggle> assistedOrderRecitals;
    private final AssistedOrderRecitalRecord assistedOrderRecitalsRecorded;
    private final AssistedCostTypesList assistedCostTypes;
    private final AssistedOrderCost assistedOrderMakeAnOrderForCosts;
    private final YesOrNo publicFundingCostsProtection;
    private final DetailText costReservedDetails;
    private final BeSpokeCostDetailText assistedOrderCostsBespoke;
    private final String assistedOrderOrderedThatText;
    private final List<FinalOrderShowToggle> assistedOrderFurtherHearingToggle;
    private final AssistedOrderFurtherHearingDetails assistedOrderFurtherHearingDetails;
    private final List<FinalOrderShowToggle> assistedOrderAppealToggle;
    private final AssistedOrderAppealDetails assistedOrderAppealDetails;
    private final OrderMadeOnTypes orderMadeOnOption;
    private final DetailTextWithDate orderMadeOnOwnInitiative;
    private final DetailTextWithDate orderMadeOnWithOutNotice;
    private final YesOrNo assistedOrderGiveReasonsYesNo;
    private final AssistedOrderGiveReasonsDetails assistedOrderGiveReasonsDetails;
    private final GARespondentDebtorOfferGAspec gaRespondentDebtorOffer;
    private final YesOrNo gaRespondentConsent;
    private final String applicationTypes;
    private final String parentCaseReference;
    private final String judgeTitle;
    private final List<Element<UploadDocumentByType>> uploadDocument;
    private final YesOrNo applicant1Represented;
    private final YesOrNo respondent1Represented;
    private final YesOrNo specRespondent1Represented;
    // GA for LIP
    private final YesOrNo isGaApplicantLip;
    private final YesOrNo isGaRespondentOneLip;
    private final YesOrNo isGaRespondentTwoLip;
    private final YesOrNo isApplicantResponded;
    private final YesOrNo isRespondentResponded;
    private final IdamUserDetails claimantUserDetails;
    private final IdamUserDetails defendantUserDetails;
    private final HelpWithFees generalAppHelpWithFees;
    private final HelpWithFees gaAdditionalHelpWithFees;
    private final FeeType hwfFeeType;
    private final HelpWithFeesDetails gaHwfDetails;
    private final HelpWithFeesDetails additionalHwfDetails;
    private final HelpWithFeesMoreInformation helpWithFeesMoreInformationGa;
    private final HelpWithFeesMoreInformation helpWithFeesMoreInformationAdditional;
    private final YesOrNo generalAppAskForCosts;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal applicationFeeAmountInPence;
    private final CertOfSC certOfSC;
    //WA claim track description
    private final String gaWaTrackLabel;
    private final String emailPartyReference;
    @Builder.Default
    private final List<Value<Document>> caseDocuments = new ArrayList<>();
    private final String caseDocument1Name;
    @Builder.Default
    private final List<IdValue<Bundle>> caseBundles = new ArrayList<>();
    @Builder.Default
    private final List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>();
    private final PreTranslationGaDocumentType preTranslationGaDocumentType;
    @Builder.Default
    private final List<Element<CaseDocument>> preTranslationGaDocsApplicant = new ArrayList<>();
    @Builder.Default
    private final List<Element<CaseDocument>> preTranslationGaDocsRespondent = new ArrayList<>();
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
