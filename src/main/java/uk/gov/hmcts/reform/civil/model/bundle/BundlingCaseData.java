package uk.gov.hmcts.reform.civil.model.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundlingCaseData {

    @JsonProperty("bundleConfiguration")
    public String bundleConfiguration;
    @JsonProperty("id")
    public long id;
    @JsonProperty("systemGeneratedCaseDocuments")
    private final List<Element<BundlingRequestDocument>> systemGeneratedCaseDocuments;
    @JsonProperty("servedDocumentFiles")
    private final ServedDocument servedDocumentFiles;
    @JsonProperty("documentReferredInStatement")
    private final List<Element<BundlingRequestDocument>> documentReferredInStatement;
    @JsonProperty("defendantResponseDocuments")
    private final List<Element<BundlingRequestDocument>> defendantResponseDocuments;
    @JsonProperty("documentReferredInStatementRes")
    private final List<Element<BundlingRequestDocument>> documentReferredInStatementRes;
    @JsonProperty("documentReferredInStatementRes2")
    private final List<Element<BundlingRequestDocument>> documentReferredInStatementRes2;
    @JsonProperty("documentDisclosureList")
    private final List<Element<BundlingRequestDocument>> documentDisclosureList;
    @JsonProperty("documentForDisclosure")
    private final List<Element<BundlingRequestDocument>> documentForDisclosure;
    @JsonProperty("documentDisclosureListRes")
    private final List<Element<BundlingRequestDocument>> documentDisclosureListRes;
    @JsonProperty("documentForDisclosureRes")
    private final List<Element<BundlingRequestDocument>> documentForDisclosureRes;
    @JsonProperty("documentDisclosureListRes2")
    private final List<Element<BundlingRequestDocument>> documentDisclosureListRes2;
    @JsonProperty("documentForDisclosureRes2")
    private final List<Element<BundlingRequestDocument>> documentForDisclosureRes2;
    @JsonProperty("documentWitnessStatement")
    private final List<Element<BundlingRequestDocument>> documentWitnessStatement;
    @JsonProperty("documentWitnessSummary")
    private final List<Element<BundlingRequestDocument>> documentWitnessSummary;
    @JsonProperty("documentHearsayNotice")
    private final List<Element<BundlingRequestDocument>> documentHearsayNotice;
    @JsonProperty("documentWitnessStatementRes")
    private final List<Element<BundlingRequestDocument>> documentWitnessStatementRes;
    @JsonProperty("documentWitnessSummaryRes")
    private final List<Element<BundlingRequestDocument>> documentWitnessSummaryRes;
    @JsonProperty("documentHearsayNoticeRes")
    private final List<Element<BundlingRequestDocument>> documentHearsayNoticeRes;
    @JsonProperty("documentWitnessStatementRes2")
    private final List<Element<BundlingRequestDocument>> documentWitnessStatementRes2;
    @JsonProperty("documentWitnessSummaryRes2")
    private final List<Element<BundlingRequestDocument>> documentWitnessSummaryRes2;
    @JsonProperty("documentHearsayNoticeRes2")
    private final List<Element<BundlingRequestDocument>> documentHearsayNoticeRes2;
    @JsonProperty("documentExpertReport")
    private final List<Element<BundlingRequestDocument>> documentExpertReport;
    @JsonProperty("documentQuestions")
    private final List<Element<BundlingRequestDocument>> documentQuestions;
    @JsonProperty("documentAnswers")
    private final List<Element<BundlingRequestDocument>> documentAnswers;
    @JsonProperty("documentJointStatement")
    private final List<Element<BundlingRequestDocument>> documentJointStatement;
    @JsonProperty("documentExpertReportRes")
    private final List<Element<BundlingRequestDocument>> documentExpertReportRes;
    @JsonProperty("documentQuestionsRes")
    private final List<Element<BundlingRequestDocument>> documentQuestionsRes;
    @JsonProperty("documentAnswersRes")
    private final List<Element<BundlingRequestDocument>> documentAnswersRes;
    @JsonProperty("documentJointStatementRes")
    private final List<Element<BundlingRequestDocument>> documentJointStatementRes;
    @JsonProperty("documentExpertReportRes2")
    private final List<Element<BundlingRequestDocument>> documentExpertReportRes2;
    @JsonProperty("documentQuestionsRes2")
    private final List<Element<BundlingRequestDocument>> documentQuestionsRes2;
    @JsonProperty("documentAnswersRes2")
    private final List<Element<BundlingRequestDocument>> documentAnswersRes2;
    @JsonProperty("documentJointStatementRes2")
    private final List<Element<BundlingRequestDocument>> documentJointStatementRes2;
    @JsonProperty("documentCaseSummary")
    private final List<Element<BundlingRequestDocument>> documentCaseSummary;
    @JsonProperty("documentCaseSummaryRes")
    private final List<Element<BundlingRequestDocument>> documentCaseSummaryRes;
    @JsonProperty("documentCaseSummaryRes2")
    private final List<Element<BundlingRequestDocument>> documentCaseSummaryRes2;
    @JsonProperty("documentSkeletonArgument")
    private final List<Element<BundlingRequestDocument>> documentSkeletonArgument;
    @JsonProperty("documentSkeletonArgumentRes")
    private final List<Element<BundlingRequestDocument>> documentSkeletonArgumentRes;
    @JsonProperty("documentSkeletonArgumentRes2")
    private final List<Element<BundlingRequestDocument>> documentSkeletonArgumentRes2;
    @JsonProperty("generalOrderDocument")
    private final List<Element<BundlingRequestDocument>> generalOrderDocument;
    @JsonProperty("documentAuthorities")
    private final List<Element<BundlingRequestDocument>> documentAuthorities;
    @JsonProperty("documentAuthoritiesRes")
    private final List<Element<BundlingRequestDocument>> documentAuthoritiesRes;
    @JsonProperty("documentAuthoritiesRes2")
    private final List<Element<BundlingRequestDocument>> documentAuthoritiesRes2;
    @JsonProperty("documentEvidenceForTrial")
    private final List<Element<BundlingRequestDocument>> documentEvidenceForTrial;
    @JsonProperty("documentEvidenceForTrialRes")
    private final List<Element<BundlingRequestDocument>> documentEvidenceForTrialRes;
    @JsonProperty("documentEvidenceForTrialRes2")
    private final List<Element<BundlingRequestDocument>> documentEvidenceForTrialRes2;
    @JsonProperty("courtLocation")
    private final String courtLocation;
    @JsonProperty("applicant1")
    private final Party applicant1;
    @JsonProperty("hasApplicant2")
    private final boolean hasApplicant2;
    @JsonProperty("applicant2")
    private final Party applicant2;
    @JsonProperty("respondant1")
    private final Party respondent1;
    @JsonProperty("hasRespondant2")
    private final boolean hasRespondant2;
    @JsonProperty("respondant2")
    private final Party respondent2;
    @JsonProperty("hearingDate")
    private final String hearingDate;
    @JsonProperty("ccdCaseReference")
    private final Long ccdCaseReference;
}
