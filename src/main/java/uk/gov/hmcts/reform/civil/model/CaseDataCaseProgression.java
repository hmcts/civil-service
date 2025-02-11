package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.ConfirmListingTickBox;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadDisclosure;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadExpert;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList;
import uk.gov.hmcts.reform.civil.enums.finalorders.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersJudgePapers;
import uk.gov.hmcts.reform.civil.enums.finalorders.HearingLengthFinalOrderList;
import uk.gov.hmcts.reform.civil.enums.finalorders.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.caseprogression.HearingOtherComments;
import uk.gov.hmcts.reform.civil.model.caseprogression.RevisedHearingRequirements;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.TaskManagementLocationTab;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.TaskManagementLocationTypes;
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
    private YesOrNo requestAnotherHearing;
    private final List<ConfirmListingTickBox> confirmListingTickBox;
    private TaskManagementLocationTypes taskManagementLocations;
    private TaskManagementLocationTab taskManagementLocationsTab;
    private TaskManagementLocationTab caseManagementLocationTab;

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
