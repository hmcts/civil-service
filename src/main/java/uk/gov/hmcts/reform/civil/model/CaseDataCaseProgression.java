package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseNoteType;
import uk.gov.hmcts.reform.civil.enums.CourtStaffNextSteps;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.enums.SettlementReason;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.ConfirmListingTickBox;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadDisclosure;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadExpert;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList;
import uk.gov.hmcts.reform.civil.enums.cosc.CoscApplicationStatus;
import uk.gov.hmcts.reform.civil.enums.cosc.CoscRPAStatus;
import uk.gov.hmcts.reform.civil.enums.finalorders.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersJudgePapers;
import uk.gov.hmcts.reform.civil.enums.finalorders.HearingLengthFinalOrderList;
import uk.gov.hmcts.reform.civil.enums.finalorders.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.enums.sendandreply.SendAndReplyOption;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.ConfirmOrderGivesPermission;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.DiscontinuanceTypeList;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.MarkPaidConsentList;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.caseprogression.HearingOtherComments;
import uk.gov.hmcts.reform.civil.model.caseprogression.RevisedHearingRequirements;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.TaskManagementLocationTab;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.TaskManagementLocationTypes;
import uk.gov.hmcts.reform.civil.model.documents.DocumentAndNote;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithName;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderCostDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderReasons;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderAppeal;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrdersComplexityBand;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRecitalsRecorded;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRepresentation;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderAfterHearingDate;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMade;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetailsOrderWithoutNotice;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaidInFull;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.querymanagement.LatestQuery;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonForReconsideration;
import uk.gov.hmcts.reform.civil.model.sendandreply.Message;
import uk.gov.hmcts.reform.civil.model.sendandreply.MessageReply;
import uk.gov.hmcts.reform.civil.model.sendandreply.SendMessageMetadata;
import uk.gov.hmcts.reform.civil.model.transferonlinecase.TransferCaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Data
public class CaseDataCaseProgression extends CaseDataCaseSdo implements MappableObject {

    private final String notificationText;
    private final List<EvidenceUploadDisclosure> disclosureSelectionEvidence;
    private final List<EvidenceUploadDisclosure> disclosureSelectionEvidenceRes;
    private final List<EvidenceUploadWitness> witnessSelectionEvidence;
    private final List<EvidenceUploadWitness> witnessSelectionEvidenceSmallClaim;
    private final List<EvidenceUploadWitness> witnessSelectionEvidenceRes;
    private final List<EvidenceUploadWitness> witnessSelectionEvidenceSmallClaimRes;
    private final List<EvidenceUploadExpert> expertSelectionEvidenceRes;
    private final List<EvidenceUploadExpert> expertSelectionEvidence;
    private final List<EvidenceUploadExpert> expertSelectionEvidenceSmallClaim;
    private final List<EvidenceUploadExpert> expertSelectionEvidenceSmallClaimRes;
    private final List<EvidenceUploadTrial> trialSelectionEvidence;
    private final List<EvidenceUploadTrial> trialSelectionEvidenceSmallClaim;
    private final List<EvidenceUploadTrial> trialSelectionEvidenceRes;
    private final List<EvidenceUploadTrial> trialSelectionEvidenceSmallClaimRes;
    //applicant
    private final List<Element<UploadEvidenceDocumentType>> documentDisclosureList;
    private final List<Element<UploadEvidenceDocumentType>> documentForDisclosure;
    private final List<Element<UploadEvidenceWitness>> documentWitnessStatement;
    private final List<Element<UploadEvidenceWitness>> documentWitnessSummary;
    private final List<Element<UploadEvidenceWitness>> documentHearsayNotice;
    private final List<Element<UploadEvidenceDocumentType>> documentReferredInStatement;
    private final List<Element<UploadEvidenceExpert>> documentExpertReport;
    private final List<Element<UploadEvidenceExpert>> documentJointStatement;
    private final List<Element<UploadEvidenceExpert>> documentQuestions;
    private final List<Element<UploadEvidenceExpert>> documentAnswers;
    private final List<Element<UploadEvidenceDocumentType>> documentCaseSummary;
    private final List<Element<UploadEvidenceDocumentType>> documentSkeletonArgument;
    private final List<Element<UploadEvidenceDocumentType>> documentAuthorities;
    private final List<Element<UploadEvidenceDocumentType>> documentCosts;
    private final List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial;
    //applicant2
    private final List<Element<UploadEvidenceDocumentType>> documentDisclosureListApp2;
    private final List<Element<UploadEvidenceDocumentType>> documentForDisclosureApp2;
    private final List<Element<UploadEvidenceWitness>> documentWitnessStatementApp2;
    private final List<Element<UploadEvidenceWitness>> documentWitnessSummaryApp2;
    private final List<Element<UploadEvidenceWitness>> documentHearsayNoticeApp2;
    private final List<Element<UploadEvidenceDocumentType>> documentReferredInStatementApp2;
    private final List<Element<UploadEvidenceExpert>> documentExpertReportApp2;
    private final List<Element<UploadEvidenceExpert>> documentJointStatementApp2;
    private final List<Element<UploadEvidenceExpert>> documentQuestionsApp2;
    private final List<Element<UploadEvidenceExpert>> documentAnswersApp2;
    private final List<Element<UploadEvidenceDocumentType>> documentCaseSummaryApp2;
    private final List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentApp2;
    private final List<Element<UploadEvidenceDocumentType>> documentAuthoritiesApp2;
    private final List<Element<UploadEvidenceDocumentType>> documentCostsApp2;
    private final List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialApp2;
    private final LocalDateTime caseDocumentUploadDate;
    //respondent
    private final List<Element<UploadEvidenceDocumentType>> documentDisclosureListRes;
    private final List<Element<UploadEvidenceDocumentType>> documentForDisclosureRes;
    private final List<Element<UploadEvidenceWitness>> documentWitnessStatementRes;
    private final List<Element<UploadEvidenceWitness>> documentWitnessSummaryRes;
    private final List<Element<UploadEvidenceWitness>> documentHearsayNoticeRes;
    private final List<Element<UploadEvidenceDocumentType>> documentReferredInStatementRes;
    private final List<Element<UploadEvidenceExpert>> documentExpertReportRes;
    private final List<Element<UploadEvidenceExpert>> documentJointStatementRes;
    private final List<Element<UploadEvidenceExpert>> documentQuestionsRes;
    private final List<Element<UploadEvidenceExpert>> documentAnswersRes;
    private final List<Element<UploadEvidenceDocumentType>> documentCaseSummaryRes;
    private final List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentRes;
    private final List<Element<UploadEvidenceDocumentType>> documentAuthoritiesRes;
    private final List<Element<UploadEvidenceDocumentType>> documentCostsRes;
    private final List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialRes;
    //these fields are shown if the solicitor is for respondent 2 and respondents have different solicitors
    private final List<Element<UploadEvidenceDocumentType>> documentDisclosureListRes2;
    private final List<Element<UploadEvidenceDocumentType>> documentForDisclosureRes2;
    private final List<Element<UploadEvidenceWitness>> documentWitnessStatementRes2;
    private final List<Element<UploadEvidenceWitness>> documentWitnessSummaryRes2;
    private final List<Element<UploadEvidenceWitness>> documentHearsayNoticeRes2;
    private final List<Element<UploadEvidenceDocumentType>> documentReferredInStatementRes2;
    private final List<Element<UploadEvidenceExpert>> documentExpertReportRes2;
    private final List<Element<UploadEvidenceExpert>> documentJointStatementRes2;
    private final List<Element<UploadEvidenceExpert>> documentQuestionsRes2;
    private final List<Element<UploadEvidenceExpert>> documentAnswersRes2;
    private final List<Element<UploadEvidenceDocumentType>> documentCaseSummaryRes2;
    private final List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentRes2;
    private final List<Element<UploadEvidenceDocumentType>> documentAuthoritiesRes2;
    private final List<Element<UploadEvidenceDocumentType>> documentCostsRes2;
    private final List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialRes2;
    private final LocalDateTime caseDocumentUploadDateRes;
    private final HearingNotes hearingNotes;
    private final List<Element<UploadEvidenceDocumentType>> applicantDocsUploadedAfterBundle;
    private final List<Element<UploadEvidenceDocumentType>> respondentDocsUploadedAfterBundle;
    private final List<Element<UploadEvidenceDocumentType>> bundleEvidence;

    /* Final Orders */
    private YesOrNo finalOrderMadeSelection;
    private OrderMade finalOrderDateHeardComplex;
    private List<FinalOrdersJudgePapers> finalOrderJudgePapers;
    private List<FinalOrderToggle> finalOrderJudgeHeardFrom;
    private FinalOrderRepresentation finalOrderRepresentation;
    private List<FinalOrderToggle> finalOrderRecitals;
    private FinalOrderRecitalsRecorded finalOrderRecitalsRecorded;
    private String finalOrderOrderedThatText;
    private AssistedCostTypesList assistedOrderCostList;
    private AssistedOrderCostDetails assistedOrderCostsReserved;
    private AssistedOrderCostDetails assistedOrderCostsBespoke;
    private AssistedOrderCostDetails assistedOrderMakeAnOrderForCosts;
    private YesOrNo publicFundingCostsProtection;
    private List<FinalOrderToggle> finalOrderFurtherHearingToggle;
    private FinalOrderFurtherHearing finalOrderFurtherHearingComplex;
    private HearingLengthFinalOrderList lengthList;
    private List<FinalOrderToggle> finalOrderAppealToggle;
    private FinalOrderAppeal finalOrderAppealComplex;
    private OrderMadeOnTypes orderMadeOnDetailsList;
    private OrderMadeOnDetails orderMadeOnDetailsOrderCourt;
    private OrderMadeOnDetailsOrderWithoutNotice orderMadeOnDetailsOrderWithoutNotice;
    private YesOrNo finalOrderGiveReasonsYesNo;
    private AssistedOrderReasons finalOrderGiveReasonsComplex;
    private YesOrNo finalOrderAllocateToTrack;
    private YesOrNo allowOrderTrackAllocation;
    private AllocatedTrack finalOrderTrackAllocation;
    private FinalOrdersComplexityBand finalOrderIntermediateTrackComplexityBand;
    private DynamicList finalOrderDownloadTemplateOptions;
    private CaseDocument finalOrderDownloadTemplateDocument;
    private Document uploadOrderDocumentFromTemplate;
    private String finalOrderTrackToggle;
    private OrderAfterHearingDate orderAfterHearingDate;
    private YesOrNo showOrderAfterHearingDatePage;

    // judge final freeform orders
    private final FinalOrderSelection finalOrderSelection;
    private final String freeFormRecordedTextArea;
    private final String freeFormOrderedTextArea;
    private final FreeFormOrderValues orderOnCourtInitiative;
    private final FreeFormOrderValues orderWithoutNotice;
    private final OrderOnCourtsList orderOnCourtsList;
    private final String freeFormHearingNotes;
    private CaseDocument finalOrderDocument;
    @Builder.Default
    private final List<Element<CaseDocument>> finalOrderDocumentCollection = new ArrayList<>();

    // Court officer order
    private FinalOrderFurtherHearing courtOfficerFurtherHearingComplex;
    private String courtOfficerOrdered;
    private YesOrNo courtOfficerGiveReasonsYesNo;
    private CaseDocument previewCourtOfficerOrder;

    //Hearing Scheduled
    private DynamicList hearingLocation;
    private LocalDate dateOfApplication;
    private LocalDate hearingDate;
    private LocalDate hearingDueDate;
    private String hearingTimeHourMinute;
    private String hearingReferenceNumber;
    private ListingOrRelisting listingOrRelisting;
    private HearingNoticeList hearingNoticeList;
    private Fee hearingFee;
    private HearingChannel channel;
    private HearingDuration hearingDuration;
    private String hearingDurationMinti;
    private String information;
    private String hearingNoticeListOther;
    private LocalDateTime caseDismissedHearingFeeDueDate;
    private String hearingDurationInMinutesAHN;
    private LocalDateTime claimantTrialReadyDocumentCreated;
    private LocalDateTime defendantTrialReadyDocumentCreated;

    //Trial Readiness
    private YesOrNo trialReadyNotified;
    private YesOrNo trialReadyChecked;
    private YesOrNo trialReadyApplicant;
    private YesOrNo trialReadyRespondent1;
    private YesOrNo trialReadyRespondent2;
    private RevisedHearingRequirements applicantRevisedHearingRequirements;
    private RevisedHearingRequirements respondent1RevisedHearingRequirements;
    private RevisedHearingRequirements respondent2RevisedHearingRequirements;
    private HearingOtherComments applicantHearingOtherComments;
    private HearingOtherComments respondent1HearingOtherComments;
    private HearingOtherComments respondent2HearingOtherComments;
    @Builder.Default
    private final List<Element<CaseDocument>> trialReadyDocuments = new ArrayList<>();

    // // MINTI case prog
    private DynamicList requestHearingNoticeDynamic;
    private final List<ConfirmListingTickBox> confirmListingTickBox;
    private TaskManagementLocationTypes taskManagementLocations;
    private TaskManagementLocationTab taskManagementLocationsTab;
    private TaskManagementLocationTab caseManagementLocationTab;
    private DynamicList hearingListedDynamicList;

    //case progression
    private final List<Element<DocumentWithName>> documentAndName;
    private final List<Element<DocumentWithName>> documentAndNameToAdd;
    private final List<Element<DocumentAndNote>> documentAndNote;
    private final List<Element<DocumentAndNote>> documentAndNoteToAdd;
    private final CaseNoteType caseNoteType;
    private final String caseNoteTA;
    private final List<Element<CaseNote>> caseNotesTA;
    private final LocalDateTime noteAdditionDateTime;
    private final String caseTypeFlag;
    private final String witnessStatementFlag;
    private final String witnessSummaryFlag;
    private final String witnessReferredStatementFlag;
    private final String expertReportFlag;
    private final String expertJointFlag;
    private final String trialAuthorityFlag;
    private final String trialCostsFlag;
    private final String trialDocumentaryFlag;

    private final YesOrNo urgentFlag;
    private final String caseProgAllocatedTrack;
    private final DynamicList evidenceUploadOptions;

    private final List<Element<RegistrationInformation>> registrationTypeRespondentOne;
    private final List<Element<RegistrationInformation>> registrationTypeRespondentTwo;

    private final String respondent1DocumentURL;
    private final String respondent2DocumentURL;
    private final String respondent2DocumentGeneration;
    private final String hearingHelpFeesReferenceNumber;

    private final String hearingLocationCourtName;
    // bulk claims
    private final String bulkCustomerId;
    private final String sdtRequestIdFromSdt;
    private final List<Element<String>> sdtRequestId;

    //Judgments Online
    private JudgmentRecordedReason joJudgmentRecordReason;
    private LocalDate joOrderMadeDate;
    private LocalDate joIssuedDate;
    private String joAmountOrdered;
    private String joAmountCostOrdered;
    private YesOrNo joIsRegisteredWithRTL;
    private JudgmentPaymentPlan joPaymentPlan;
    private JudgmentInstalmentDetails joInstalmentDetails;
    private YesOrNo joIsLiveJudgmentExists;
    private JudgmentPaidInFull joJudgmentPaidInFull;
    private JudgmentSetAsideReason joSetAsideReason;
    private String joSetAsideJudgmentErrorText;
    private JudgmentSetAsideOrderType joSetAsideOrderType;
    private LocalDate joSetAsideOrderDate;
    private LocalDate joSetAsideApplicationDate;
    private LocalDate joSetAsideDefenceReceivedDate;
    private YesOrNo joShowRegisteredWithRTLOption;
    private JudgmentDetails activeJudgment;
    private List<Element<JudgmentDetails>> historicJudgment;
    private LocalDateTime joSetAsideCreatedDate;

    private String joDefendantName1;
    private String joDefendantName2;
    private PaymentPlanSelection joPaymentPlanSelected;
    private String joRepaymentAmount;
    private LocalDate joRepaymentStartDate;
    private PaymentFrequency joRepaymentFrequency;
    private LocalDate joIssueDate;
    private JudgmentState joState;
    private LocalDate joFullyPaymentMadeDate;
    private LocalDateTime joMarkedPaidInFullIssueDate;
    private LocalDateTime joDefendantMarkedPaidInFullIssueDate;
    private LocalDateTime joJudgementByAdmissionIssueDate;
    private CoscRPAStatus joCoscRpaStatus;
    private String joOrderedAmount;
    private String joCosts;
    private String joTotalAmount;
    private YesOrNo joIsDisplayInJudgmentTab;
    private String joRepaymentSummaryObject;
    private YesOrNo respondForImmediateOption;
    private LocalDateTime joDJCreatedDate;

    private final TransferCaseDetails transferCaseDetails;

    //SDO-R2
    private YesOrNo isFlightDelayClaim;
    private FlightDelayDetails flightDelayDetails;
    private ReasonForReconsideration reasonForReconsiderationApplicant;
    private ReasonForReconsideration reasonForReconsiderationRespondent1;
    private ReasonForReconsideration reasonForReconsiderationRespondent2;
    private String casePartyRequestForReconsideration;
    private DecisionOnRequestReconsiderationOptions decisionOnRequestReconsiderationOptions;
    private UpholdingPreviousOrderReason upholdingPreviousOrderReason;
    private String dashboardNotificationTypeOrder;
    private CaseDocument decisionOnReconsiderationDocument;
    private CaseDocument requestForReconsiderationDocument;
    private CaseDocument requestForReconsiderationDocumentRes;
    private LocalDateTime requestForReconsiderationDeadline;
    private YesOrNo requestForReconsiderationDeadlineChecked;

    //Settle And Discontinue
    private YesOrNo markPaidForAllClaimants;
    private DynamicList claimantWhoIsSettling;
    private DynamicList claimantWhoIsDiscontinuing;
    private DynamicList discontinuingAgainstOneDefendant;
    private String selectedClaimantForDiscontinuance;
    private SettleDiscontinueYesOrNoList courtPermissionNeeded;
    private SettleDiscontinueYesOrNoList isPermissionGranted;
    private PermissionGranted permissionGrantedComplex;
    private String permissionGrantedJudgeCopy;
    private LocalDate permissionGrantedDateCopy;
    private DiscontinuanceTypeList typeOfDiscontinuance;
    private String partDiscontinuanceDetails;
    private ConfirmOrderGivesPermission confirmOrderGivesPermission;
    private SettleDiscontinueYesOrNoList isDiscontinuingAgainstBothDefendants;
    private SettlementReason settleReason;
    private final MarkPaidConsentList markPaidConsent;
    private YesOrNo claimantsConsentToDiscontinuance;
    private CaseDocument applicant1NoticeOfDiscontinueCWViewDoc;
    private CaseDocument respondent1NoticeOfDiscontinueCWViewDoc;
    private CaseDocument respondent2NoticeOfDiscontinueCWViewDoc;
    private CaseDocument applicant1NoticeOfDiscontinueAllPartyViewDoc;
    private CaseDocument respondent1NoticeOfDiscontinueAllPartyViewDoc;
    private CaseDocument respondent2NoticeOfDiscontinueAllPartyViewDoc;

    @JsonUnwrapped
    private FeePaymentOutcomeDetails feePaymentOutcomeDetails;
    private LocalDate coscSchedulerDeadline;
    private CoscApplicationStatus coSCApplicationStatus;

    //Caseworker events
    private YesOrNo obligationDatePresent;
    private CourtStaffNextSteps courtStaffNextSteps;
    private List<Element<ObligationData>> obligationData;
    private List<Element<StoredObligationData>> storedObligationData;
    private YesOrNo isFinalOrder;
    private SendAndReplyOption sendAndReplyOption;
    private SendMessageMetadata sendMessageMetadata;
    private String sendMessageContent;
    private MessageReply messageReplyMetadata;
    private String messageHistory;
    private DynamicList messagesToReplyTo;
    private List<Element<Message>> messages;
    private ObligationWAFlag obligationWAFlag;
    private Message lastMessage;
    private String lastMessageAllocatedTrack;
    private String lastMessageJudgeLabel;

    //QueryManagement
    private final CaseQueriesCollection qmApplicantSolicitorQueries;
    private final CaseQueriesCollection qmRespondentSolicitor1Queries;
    private final CaseQueriesCollection qmRespondentSolicitor2Queries;
    private final CaseQueriesCollection qmApplicantCitizenQueries;
    private final CaseQueriesCollection qmRespondentCitizenQueries;
    private final CaseMessage caseMessage;
    private final LatestQuery qmLatestQuery;

    /**
     * Claimant has requested a reconsideration of the SDO.
     */
    private YesOrNo orderRequestedForReviewClaimant;
    /**
     * Defendant has requested a reconsideration of the SDO.
     */
    private YesOrNo orderRequestedForReviewDefendant;

    @JsonIgnore
    public String getHearingLocationText() {
        return ofNullable(hearingLocation)
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel)
            .orElse(null);
    }
}
