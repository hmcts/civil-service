package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderCostDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderReasons;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderAppeal;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRecitalsRecorded;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRepresentation;
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
@AllArgsConstructor
@NoArgsConstructor
public class CaseDataCaseProgression extends CaseDataCaseSdo implements MappableObject {

    private String notificationText;
    private List<EvidenceUploadDisclosure> disclosureSelectionEvidence;
    private List<EvidenceUploadDisclosure> disclosureSelectionEvidenceRes;
    private List<EvidenceUploadWitness> witnessSelectionEvidence;
    private List<EvidenceUploadWitness> witnessSelectionEvidenceSmallClaim;
    private List<EvidenceUploadWitness> witnessSelectionEvidenceRes;
    private List<EvidenceUploadWitness> witnessSelectionEvidenceSmallClaimRes;
    private List<EvidenceUploadExpert> expertSelectionEvidenceRes;
    private List<EvidenceUploadExpert> expertSelectionEvidence;
    private List<EvidenceUploadExpert> expertSelectionEvidenceSmallClaim;
    private List<EvidenceUploadExpert> expertSelectionEvidenceSmallClaimRes;
    private List<EvidenceUploadTrial> trialSelectionEvidence;
    private List<EvidenceUploadTrial> trialSelectionEvidenceSmallClaim;
    private List<EvidenceUploadTrial> trialSelectionEvidenceRes;
    private List<EvidenceUploadTrial> trialSelectionEvidenceSmallClaimRes;
    //applicant
    private List<Element<UploadEvidenceDocumentType>> documentDisclosureList;
    private List<Element<UploadEvidenceDocumentType>> documentForDisclosure;
    private List<Element<UploadEvidenceWitness>> documentWitnessStatement;
    private List<Element<UploadEvidenceWitness>> documentWitnessSummary;
    private List<Element<UploadEvidenceWitness>> documentHearsayNotice;
    private List<Element<UploadEvidenceDocumentType>> documentReferredInStatement;
    private List<Element<UploadEvidenceExpert>> documentExpertReport;
    private List<Element<UploadEvidenceExpert>> documentJointStatement;
    private List<Element<UploadEvidenceExpert>> documentQuestions;
    private List<Element<UploadEvidenceExpert>> documentAnswers;
    private List<Element<UploadEvidenceDocumentType>> documentCaseSummary;
    private List<Element<UploadEvidenceDocumentType>> documentSkeletonArgument;
    private List<Element<UploadEvidenceDocumentType>> documentAuthorities;
    private List<Element<UploadEvidenceDocumentType>> documentCosts;
    private List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial;
    //applicant2
    private List<Element<UploadEvidenceDocumentType>> documentDisclosureListApp2;
    private List<Element<UploadEvidenceDocumentType>> documentForDisclosureApp2;
    private List<Element<UploadEvidenceWitness>> documentWitnessStatementApp2;
    private List<Element<UploadEvidenceWitness>> documentWitnessSummaryApp2;
    private List<Element<UploadEvidenceWitness>> documentHearsayNoticeApp2;
    private List<Element<UploadEvidenceDocumentType>> documentReferredInStatementApp2;
    private List<Element<UploadEvidenceExpert>> documentExpertReportApp2;
    private List<Element<UploadEvidenceExpert>> documentJointStatementApp2;
    private List<Element<UploadEvidenceExpert>> documentQuestionsApp2;
    private List<Element<UploadEvidenceExpert>> documentAnswersApp2;
    private List<Element<UploadEvidenceDocumentType>> documentCaseSummaryApp2;
    private List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentApp2;
    private List<Element<UploadEvidenceDocumentType>> documentAuthoritiesApp2;
    private List<Element<UploadEvidenceDocumentType>> documentCostsApp2;
    private List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialApp2;
    private LocalDateTime caseDocumentUploadDate;
    //respondent
    private List<Element<UploadEvidenceDocumentType>> documentDisclosureListRes;
    private List<Element<UploadEvidenceDocumentType>> documentForDisclosureRes;
    private List<Element<UploadEvidenceWitness>> documentWitnessStatementRes;
    private List<Element<UploadEvidenceWitness>> documentWitnessSummaryRes;
    private List<Element<UploadEvidenceWitness>> documentHearsayNoticeRes;
    private List<Element<UploadEvidenceDocumentType>> documentReferredInStatementRes;
    private List<Element<UploadEvidenceExpert>> documentExpertReportRes;
    private List<Element<UploadEvidenceExpert>> documentJointStatementRes;
    private List<Element<UploadEvidenceExpert>> documentQuestionsRes;
    private List<Element<UploadEvidenceExpert>> documentAnswersRes;
    private List<Element<UploadEvidenceDocumentType>> documentCaseSummaryRes;
    private List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentRes;
    private List<Element<UploadEvidenceDocumentType>> documentAuthoritiesRes;
    private List<Element<UploadEvidenceDocumentType>> documentCostsRes;
    private List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialRes;
    //these fields are shown if the solicitor is for respondent 2 and respondents have different solicitors
    private List<Element<UploadEvidenceDocumentType>> documentDisclosureListRes2;
    private List<Element<UploadEvidenceDocumentType>> documentForDisclosureRes2;
    private List<Element<UploadEvidenceWitness>> documentWitnessStatementRes2;
    private List<Element<UploadEvidenceWitness>> documentWitnessSummaryRes2;
    private List<Element<UploadEvidenceWitness>> documentHearsayNoticeRes2;
    private List<Element<UploadEvidenceDocumentType>> documentReferredInStatementRes2;
    private List<Element<UploadEvidenceExpert>> documentExpertReportRes2;
    private List<Element<UploadEvidenceExpert>> documentJointStatementRes2;
    private List<Element<UploadEvidenceExpert>> documentQuestionsRes2;
    private List<Element<UploadEvidenceExpert>> documentAnswersRes2;
    private List<Element<UploadEvidenceDocumentType>> documentCaseSummaryRes2;
    private List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentRes2;
    private List<Element<UploadEvidenceDocumentType>> documentAuthoritiesRes2;
    private List<Element<UploadEvidenceDocumentType>> documentCostsRes2;
    private List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialRes2;
    private LocalDateTime caseDocumentUploadDateRes;
    private HearingNotes hearingNotes;
    private List<Element<UploadEvidenceDocumentType>> applicantDocsUploadedAfterBundle;
    private List<Element<UploadEvidenceDocumentType>> respondentDocsUploadedAfterBundle;

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

    // judge final freeform orders
    private FinalOrderSelection finalOrderSelection;
    private String freeFormRecordedTextArea;
    private String freeFormOrderedTextArea;
    private FreeFormOrderValues orderOnCourtInitiative;
    private FreeFormOrderValues orderWithoutNotice;
    private OrderOnCourtsList orderOnCourtsList;
    private String freeFormHearingNotes;
    private CaseDocument finalOrderDocument;
    @Builder.Default
    private List<Element<CaseDocument>> finalOrderDocumentCollection = new ArrayList<>();

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
    private String information;
    private String hearingNoticeListOther;
    private LocalDateTime caseDismissedHearingFeeDueDate;

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
    private List<Element<CaseDocument>> trialReadyDocuments = new ArrayList<>();

    @JsonIgnore
    public String getHearingLocationText() {
        return ofNullable(hearingLocation)
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel)
            .orElse(null);
    }
}
