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

    private  String notificationText;
    private  List<EvidenceUploadDisclosure> disclosureSelectionEvidence;
    private  List<EvidenceUploadDisclosure> disclosureSelectionEvidenceRes;
    private  List<EvidenceUploadWitness> witnessSelectionEvidence;
    private  List<EvidenceUploadWitness> witnessSelectionEvidenceSmallClaim;
    private  List<EvidenceUploadWitness> witnessSelectionEvidenceRes;
    private  List<EvidenceUploadWitness> witnessSelectionEvidenceSmallClaimRes;
    private  List<EvidenceUploadExpert> expertSelectionEvidenceRes;
    private  List<EvidenceUploadExpert> expertSelectionEvidence;
    private  List<EvidenceUploadExpert> expertSelectionEvidenceSmallClaim;
    private  List<EvidenceUploadExpert> expertSelectionEvidenceSmallClaimRes;
    private  List<EvidenceUploadTrial> trialSelectionEvidence;
    private  List<EvidenceUploadTrial> trialSelectionEvidenceSmallClaim;
    private  List<EvidenceUploadTrial> trialSelectionEvidenceRes;
    private  List<EvidenceUploadTrial> trialSelectionEvidenceSmallClaimRes;
    //applicant
    private  List<Element<UploadEvidenceDocumentType>> documentDisclosureList;
    private  List<Element<UploadEvidenceDocumentType>> documentForDisclosure;
    private  List<Element<UploadEvidenceWitness>> documentWitnessStatement;
    private  List<Element<UploadEvidenceWitness>> documentWitnessSummary;
    private  List<Element<UploadEvidenceWitness>> documentHearsayNotice;
    private  List<Element<UploadEvidenceDocumentType>> documentReferredInStatement;
    private  List<Element<UploadEvidenceExpert>> documentExpertReport;
    private  List<Element<UploadEvidenceExpert>> documentJointStatement;
    private  List<Element<UploadEvidenceExpert>> documentQuestions;
    private  List<Element<UploadEvidenceExpert>> documentAnswers;
    private  List<Element<UploadEvidenceDocumentType>> documentCaseSummary;
    private  List<Element<UploadEvidenceDocumentType>> documentSkeletonArgument;
    private  List<Element<UploadEvidenceDocumentType>> documentAuthorities;
    private  List<Element<UploadEvidenceDocumentType>> documentCosts;
    private  List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial;
    //applicant2
    private  List<Element<UploadEvidenceDocumentType>> documentDisclosureListApp2;
    private  List<Element<UploadEvidenceDocumentType>> documentForDisclosureApp2;
    private  List<Element<UploadEvidenceWitness>> documentWitnessStatementApp2;
    private  List<Element<UploadEvidenceWitness>> documentWitnessSummaryApp2;
    private  List<Element<UploadEvidenceWitness>> documentHearsayNoticeApp2;
    private  List<Element<UploadEvidenceDocumentType>> documentReferredInStatementApp2;
    private  List<Element<UploadEvidenceExpert>> documentExpertReportApp2;
    private  List<Element<UploadEvidenceExpert>> documentJointStatementApp2;
    private  List<Element<UploadEvidenceExpert>> documentQuestionsApp2;
    private  List<Element<UploadEvidenceExpert>> documentAnswersApp2;
    private  List<Element<UploadEvidenceDocumentType>> documentCaseSummaryApp2;
    private  List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentApp2;
    private  List<Element<UploadEvidenceDocumentType>> documentAuthoritiesApp2;
    private  List<Element<UploadEvidenceDocumentType>> documentCostsApp2;
    private  List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialApp2;
    private  LocalDateTime caseDocumentUploadDate;
    //respondent
    private  List<Element<UploadEvidenceDocumentType>> documentDisclosureListRes;
    private  List<Element<UploadEvidenceDocumentType>> documentForDisclosureRes;
    private  List<Element<UploadEvidenceWitness>> documentWitnessStatementRes;
    private  List<Element<UploadEvidenceWitness>> documentWitnessSummaryRes;
    private  List<Element<UploadEvidenceWitness>> documentHearsayNoticeRes;
    private  List<Element<UploadEvidenceDocumentType>> documentReferredInStatementRes;
    private  List<Element<UploadEvidenceExpert>> documentExpertReportRes;
    private  List<Element<UploadEvidenceExpert>> documentJointStatementRes;
    private  List<Element<UploadEvidenceExpert>> documentQuestionsRes;
    private  List<Element<UploadEvidenceExpert>> documentAnswersRes;
    private  List<Element<UploadEvidenceDocumentType>> documentCaseSummaryRes;
    private  List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentRes;
    private  List<Element<UploadEvidenceDocumentType>> documentAuthoritiesRes;
    private  List<Element<UploadEvidenceDocumentType>> documentCostsRes;
    private  List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialRes;
    //these fields are shown if the solicitor is for respondent 2 and respondents have different solicitors
    private  List<Element<UploadEvidenceDocumentType>> documentDisclosureListRes2;
    private  List<Element<UploadEvidenceDocumentType>> documentForDisclosureRes2;
    private  List<Element<UploadEvidenceWitness>> documentWitnessStatementRes2;
    private  List<Element<UploadEvidenceWitness>> documentWitnessSummaryRes2;
    private  List<Element<UploadEvidenceWitness>> documentHearsayNoticeRes2;
    private  List<Element<UploadEvidenceDocumentType>> documentReferredInStatementRes2;
    private  List<Element<UploadEvidenceExpert>> documentExpertReportRes2;
    private  List<Element<UploadEvidenceExpert>> documentJointStatementRes2;
    private  List<Element<UploadEvidenceExpert>> documentQuestionsRes2;
    private  List<Element<UploadEvidenceExpert>> documentAnswersRes2;
    private  List<Element<UploadEvidenceDocumentType>> documentCaseSummaryRes2;
    private  List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentRes2;
    private  List<Element<UploadEvidenceDocumentType>> documentAuthoritiesRes2;
    private  List<Element<UploadEvidenceDocumentType>> documentCostsRes2;
    private  List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialRes2;
    private  LocalDateTime caseDocumentUploadDateRes;
    private  HearingNotes hearingNotes;
    private  List<Element<UploadEvidenceDocumentType>> applicantDocsUploadedAfterBundle;
    private  List<Element<UploadEvidenceDocumentType>> respondentDocsUploadedAfterBundle;
    private  List<Element<UploadEvidenceDocumentType>> bundleEvidence;

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

    private  FinalOrderSelection finalOrderSelection;
    private  String freeFormRecordedTextArea;
    private  String freeFormOrderedTextArea;
    private  FreeFormOrderValues orderOnCourtInitiative;
    private  FreeFormOrderValues orderWithoutNotice;
    private  OrderOnCourtsList orderOnCourtsList;
    private  String freeFormHearingNotes;
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
    private  List<Element<CaseDocument>> trialReadyDocuments = new ArrayList<>();

    // // MINTI case prog
    private DynamicList requestHearingNoticeDynamic;
    private  List<ConfirmListingTickBox> confirmListingTickBox;
    private TaskManagementLocationTypes taskManagementLocations;
    private TaskManagementLocationTab taskManagementLocationsTab;
    private TaskManagementLocationTab caseManagementLocationTab;
    private DynamicList hearingListedDynamicList;

    //case progression
    private  List<Element<DocumentWithName>> documentAndName;
    private  List<Element<DocumentWithName>> documentAndNameToAdd;
    private  List<Element<DocumentAndNote>> documentAndNote;
    private  List<Element<DocumentAndNote>> documentAndNoteToAdd;
    private  CaseNoteType caseNoteType;
    private  String caseNoteTA;
    private  List<Element<CaseNote>> caseNotesTA;
    private  LocalDateTime noteAdditionDateTime;
    private  String caseTypeFlag;
    private  String witnessStatementFlag;
    private  String witnessSummaryFlag;
    private  String witnessReferredStatementFlag;
    private  String expertReportFlag;
    private  String expertJointFlag;
    private  String trialAuthorityFlag;
    private  String trialCostsFlag;
    private  String trialDocumentaryFlag;

    private  YesOrNo urgentFlag;
    private  String caseProgAllocatedTrack;
    private  DynamicList evidenceUploadOptions;

    private  List<Element<RegistrationInformation>> registrationTypeRespondentOne;
    private  List<Element<RegistrationInformation>> registrationTypeRespondentTwo;

    private  String respondent1DocumentURL;
    private  String respondent2DocumentURL;
    private  String respondent2DocumentGeneration;
    private  String hearingHelpFeesReferenceNumber;

    private  String hearingLocationCourtName;
    // bulk claims
    private  String bulkCustomerId;
    private  String sdtRequestIdFromSdt;
    private  List<Element<String>> sdtRequestId;

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

    private  TransferCaseDetails transferCaseDetails;

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
    private  MarkPaidConsentList markPaidConsent;
    private YesOrNo claimantsConsentToDiscontinuance;
    private CaseDocument applicant1NoticeOfDiscontinueCWViewDoc;
    private CaseDocument respondent1NoticeOfDiscontinueCWViewDoc;
    private CaseDocument respondent2NoticeOfDiscontinueCWViewDoc;
    private CaseDocument applicant1NoticeOfDiscontinueAllPartyViewDoc;
    private CaseDocument respondent1NoticeOfDiscontinueAllPartyViewDoc;
    private CaseDocument respondent2NoticeOfDiscontinueAllPartyViewDoc;
    private CaseDocument respondent1NoticeOfDiscontinueAllPartyTranslatedDoc;

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
    private String waTaskToCompleteId;

    //QueryManagement
    private  CaseQueriesCollection qmApplicantSolicitorQueries;
    private  CaseQueriesCollection qmRespondentSolicitor1Queries;
    private  CaseQueriesCollection qmRespondentSolicitor2Queries;
    private  CaseQueriesCollection queries;
    private  CaseMessage caseMessage;
    private  LatestQuery qmLatestQuery;

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
