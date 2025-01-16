package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceDocumentRetriever;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceExpertRetriever;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceWitnessRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public abstract class BaseDocumentHandlerTest {

    public static final class DomainConstants {
        public static final String APPLICANT = "Applicant";
        public static final String RESPONDENT = "Respondent";

        public static final String ORIGINAL_FILE_NAME = "OriginalName.pdf";
        public static final LocalDate ISSUE_DATE = LocalDate.of(2022, 2, 10);

        public static final String BUNDLE_TEST = "test";
        public static final String BUNDLE_DISCLOSURE = "DisclosureBundle";
        public static final String BUNDLE_DISCLOSURE_LIST = "DisclosureListBundle";
        public static final String BUNDLE_SCHEDULE_OF_COSTS = "ScheduleOfCostsBundle";
        public static final String BUNDLE_PRE_TRIAL_SUMMARY = "PreTrialSummaryBundle";
        public static final String BUNDLE_TRIAL_CORRESPONDENCE = "TrialCorrespondenceBundle";
        public static final String BUNDLE_TRIAL_SKELETON = "TrialSkeletonBundle";

        public static final String WITNESS_HEARSAY_BUNDLE = "WitnessHearsayBundle";
        public static final String WITNESS_NAME = "witnessName";
        public static final String TYPE_OF_DOCUMENT = "typeOfDocument";
    }

    @Mock
    protected UploadEvidenceDocumentRetriever uploadEvidenceDocumentRetriever;

    @Mock
    protected DocumentTypeBuilder<UploadEvidenceDocumentType> documentTypeBuilder;

    @Mock
    protected UploadEvidenceExpertRetriever uploadEvidenceExpertRetriever;

    @Mock
    protected UploadEvidenceWitnessRetriever uploadEvidenceWitnessRetriever;

    protected Document document;
    protected LocalDateTime mockDateTime;
    protected CaseData caseData;
    protected CaseData caseDataBefore;
    protected CaseData.CaseDataBuilder<?, ?> builder;
    protected UploadEvidenceDocumentType uploadEvidenceDocumentType;
    protected UploadEvidenceExpert uploadEvidenceExpert;
    protected UploadEvidenceWitness uploadEvidenceWitness;

    @BeforeEach
    void setUpMocks() {
        document = createMockDocument();
        mockDateTime = LocalDateTime.of(2022, 2, 10, 10, 0);

        mockRetriever(uploadEvidenceDocumentRetriever, document, mockDateTime);
        mockRetriever(uploadEvidenceExpertRetriever, document, mockDateTime);
        mockRetriever(uploadEvidenceWitnessRetriever, document, mockDateTime);

        builder = CaseData.builder();
    }

    private Document createMockDocument() {
        return Document.builder()
                .documentFileName(DomainConstants.ORIGINAL_FILE_NAME)
                .build();
    }

    private void mockRetriever(
            UploadEvidenceDocumentRetriever retriever,
            Document doc,
            LocalDateTime dateTime
    ) {
        lenient().when(retriever.getDocument(any())).thenReturn(doc);
        lenient().when(retriever.getDocumentDateTime(any())).thenReturn(dateTime);
    }

    private void mockRetriever(
            UploadEvidenceExpertRetriever retriever,
            Document doc,
            LocalDateTime dateTime
    ) {
        lenient().when(retriever.getDocument(any())).thenReturn(doc);
        lenient().when(retriever.getDocumentDateTime(any())).thenReturn(dateTime);
    }

    private void mockRetriever(
            UploadEvidenceWitnessRetriever retriever,
            Document doc,
            LocalDateTime dateTime
    ) {
        lenient().when(retriever.getDocument(any())).thenReturn(doc);
        lenient().when(retriever.getDocumentDateTime(any())).thenReturn(dateTime);
    }

    protected <T> List<Element<T>> toElementArrayList(T value) {
        return new ArrayList<>(
                List.of(Element.<T>builder().value(value).build())
        );
    }

    protected UploadEvidenceDocumentType createDocumentType(String bundleName, String typeOfDocument) {
        UploadEvidenceDocumentType.UploadEvidenceDocumentTypeBuilder builder = UploadEvidenceDocumentType.builder()
                .documentIssuedDate(DomainConstants.ISSUE_DATE)
                .bundleName(bundleName)
                .documentUpload(document);

        if (typeOfDocument != null) {
            builder.typeOfDocument(typeOfDocument);
        }

        return builder.build();
    }

    protected UploadEvidenceExpert createExpert(String name) {
        return UploadEvidenceExpert.builder()
                .expertOptionName(name)
                .expertOptionUploadDate(DomainConstants.ISSUE_DATE)
                .expertDocument(document)
                .build();
    }

    protected UploadEvidenceWitness createWitness(String name) {
        return UploadEvidenceWitness.builder()
                .witnessOptionName(name)
                .witnessOptionUploadDate(DomainConstants.ISSUE_DATE)
                .witnessOptionDocument(document)
                .build();
    }

    protected void setUpDocumentAuthorities() {
        uploadEvidenceDocumentType = createDocumentType(DomainConstants.BUNDLE_TEST, null);
        caseData = CaseData.builder()
                .documentAuthorities(toElementArrayList(uploadEvidenceDocumentType))
                .documentAuthoritiesApp2(toElementArrayList(uploadEvidenceDocumentType))
                .documentAuthoritiesRes(toElementArrayList(uploadEvidenceDocumentType))
                .documentAuthoritiesRes2(toElementArrayList(uploadEvidenceDocumentType))
                .build();

        caseDataBefore = CaseData.builder()
                .documentAuthorities(new ArrayList<>())
                .build();
    }

    protected void setUpBundleEvidence() {
        uploadEvidenceDocumentType = createDocumentType(DomainConstants.BUNDLE_TEST, null);
        caseData = CaseData.builder()
                .bundleEvidence(toElementArrayList(uploadEvidenceDocumentType))
                .build();

        caseDataBefore = CaseData.builder()
                .bundleEvidence(new ArrayList<>())
                .build();
    }

    protected void setUpDocumentForDisclosure() {
        uploadEvidenceDocumentType = createDocumentType(
                DomainConstants.BUNDLE_DISCLOSURE,
                DomainConstants.BUNDLE_TEST
        );
        caseData = CaseData.builder()
                .documentForDisclosure(toElementArrayList(uploadEvidenceDocumentType))
                .documentForDisclosureApp2(toElementArrayList(uploadEvidenceDocumentType))
                .documentForDisclosureRes(toElementArrayList(uploadEvidenceDocumentType))
                .documentForDisclosureRes2(toElementArrayList(uploadEvidenceDocumentType))
                .build();

        caseDataBefore = CaseData.builder()
                .documentForDisclosure(new ArrayList<>())
                .build();
    }

    protected void setUpDocumentForDisclosureList() {
        uploadEvidenceDocumentType = createDocumentType(DomainConstants.BUNDLE_DISCLOSURE_LIST, null);
        caseData = CaseData.builder()
                .documentDisclosureList(toElementArrayList(uploadEvidenceDocumentType))
                .documentDisclosureListApp2(toElementArrayList(uploadEvidenceDocumentType))
                .documentDisclosureListRes(toElementArrayList(uploadEvidenceDocumentType))
                .documentDisclosureListRes2(toElementArrayList(uploadEvidenceDocumentType))
                .build();

        caseDataBefore = CaseData.builder()
                .documentDisclosureList(new ArrayList<>())
                .build();
    }

    protected void setUpDocumentAnswers() {
        uploadEvidenceExpert = createExpert("ExpertOptionNameTest").toBuilder()
                .expertOptionOtherParty("ExpertOptionOtherPartyTest")
                .expertDocumentAnswer("ExpertDocumentAnswerTest")
                .build();

        caseData = CaseData.builder()
                .documentAnswers(toElementArrayList(uploadEvidenceExpert))
                .documentAnswersApp2(toElementArrayList(uploadEvidenceExpert))
                .documentAnswersRes(toElementArrayList(uploadEvidenceExpert))
                .documentAnswersRes2(toElementArrayList(uploadEvidenceExpert))
                .build();

        caseDataBefore = CaseData.builder()
                .documentAnswers(new ArrayList<>())
                .build();
    }

    protected void setUpDocumentJointStatement() {
        uploadEvidenceExpert = createExpert(DomainConstants.BUNDLE_TEST).toBuilder()
                .expertOptionExpertises("expertises")
                .build();

        caseData = CaseData.builder()
                .documentJointStatement(toElementArrayList(uploadEvidenceExpert))
                .documentJointStatementApp2(toElementArrayList(uploadEvidenceExpert))
                .documentJointStatementRes(toElementArrayList(uploadEvidenceExpert))
                .documentJointStatementRes2(toElementArrayList(uploadEvidenceExpert))
                .build();

        caseDataBefore = CaseData.builder()
                .documentJointStatement(new ArrayList<>())
                .build();
    }

    protected void setUpDocumentQuestions() {
        uploadEvidenceExpert = createExpert(DomainConstants.BUNDLE_TEST).toBuilder()
                .expertDocumentQuestion("Document Question")
                .expertOptionOtherParty("Other Party")
                .build();

        caseData = CaseData.builder()
                .documentQuestions(toElementArrayList(uploadEvidenceExpert))
                .documentQuestionsApp2(toElementArrayList(uploadEvidenceExpert))
                .documentQuestionsRes(toElementArrayList(uploadEvidenceExpert))
                .documentQuestionsRes2(toElementArrayList(uploadEvidenceExpert))
                .build();

        caseDataBefore = CaseData.builder()
                .documentQuestions(new ArrayList<>())
                .build();
    }

    protected void setUpDocumentExpertReport() {
        uploadEvidenceExpert = createExpert(DomainConstants.BUNDLE_TEST).toBuilder()
                .expertOptionExpertise("expertise")
                .build();

        caseData = CaseData.builder()
                .documentExpertReport(toElementArrayList(uploadEvidenceExpert))
                .documentExpertReportApp2(toElementArrayList(uploadEvidenceExpert))
                .documentExpertReportRes(toElementArrayList(uploadEvidenceExpert))
                .documentExpertReportRes2(toElementArrayList(uploadEvidenceExpert))
                .build();

        caseDataBefore = CaseData.builder()
                .documentExpertReport(new ArrayList<>())
                .build();
    }

    protected void setUpDocumentCaseSummary() {
        uploadEvidenceDocumentType = createDocumentType(DomainConstants.BUNDLE_PRE_TRIAL_SUMMARY, null);
        caseData = CaseData.builder()
                .documentCaseSummary(toElementArrayList(uploadEvidenceDocumentType))
                .documentCaseSummaryApp2(toElementArrayList(uploadEvidenceDocumentType))
                .documentCaseSummaryRes(toElementArrayList(uploadEvidenceDocumentType))
                .documentCaseSummaryRes2(toElementArrayList(uploadEvidenceDocumentType))
                .build();

        caseDataBefore = CaseData.builder()
                .documentCaseSummary(new ArrayList<>())
                .build();
    }

    protected void setUpDocumentCosts() {
        uploadEvidenceDocumentType = createDocumentType(DomainConstants.BUNDLE_SCHEDULE_OF_COSTS, null);
        caseData = CaseData.builder()
                .documentCosts(toElementArrayList(uploadEvidenceDocumentType))
                .documentCostsApp2(toElementArrayList(uploadEvidenceDocumentType))
                .documentCostsRes(toElementArrayList(uploadEvidenceDocumentType))
                .documentCostsRes2(toElementArrayList(uploadEvidenceDocumentType))
                .build();

        caseDataBefore = CaseData.builder()
                .documentCosts(new ArrayList<>())
                .build();
    }

    protected void setUpDocumentEvidenceForTrial() {
        uploadEvidenceDocumentType = createDocumentType(
                DomainConstants.BUNDLE_TRIAL_CORRESPONDENCE,
                DomainConstants.TYPE_OF_DOCUMENT
        );
        caseData = CaseData.builder()
                .documentEvidenceForTrial(toElementArrayList(uploadEvidenceDocumentType))
                .documentEvidenceForTrialApp2(toElementArrayList(uploadEvidenceDocumentType))
                .documentEvidenceForTrialRes(toElementArrayList(uploadEvidenceDocumentType))
                .documentEvidenceForTrialRes2(toElementArrayList(uploadEvidenceDocumentType))
                .build();

        caseDataBefore = CaseData.builder()
                .documentEvidenceForTrial(new ArrayList<>())
                .build();
    }

    protected void setUpDocumentSkeletonArgument() {
        uploadEvidenceDocumentType = createDocumentType(DomainConstants.BUNDLE_TRIAL_SKELETON, null);
        caseData = CaseData.builder()
                .documentSkeletonArgument(toElementArrayList(uploadEvidenceDocumentType))
                .documentSkeletonArgumentApp2(toElementArrayList(uploadEvidenceDocumentType))
                .documentSkeletonArgumentRes(toElementArrayList(uploadEvidenceDocumentType))
                .documentSkeletonArgumentRes2(toElementArrayList(uploadEvidenceDocumentType))
                .build();

        caseDataBefore = CaseData.builder()
                .documentSkeletonArgument(new ArrayList<>())
                .build();
    }

    protected void setUpDocumentHearsayNotice() {
        uploadEvidenceWitness = createWitness(DomainConstants.WITNESS_HEARSAY_BUNDLE);
        caseData = CaseData.builder()
                .documentHearsayNotice(toElementArrayList(uploadEvidenceWitness))
                .documentHearsayNoticeApp2(toElementArrayList(uploadEvidenceWitness))
                .documentHearsayNoticeRes(toElementArrayList(uploadEvidenceWitness))
                .documentHearsayNoticeRes2(toElementArrayList(uploadEvidenceWitness))
                .build();

        caseDataBefore = CaseData.builder()
                .documentHearsayNotice(new ArrayList<>())
                .build();
    }

    protected void setUpDocumentReferredInStatement() {
        uploadEvidenceDocumentType = UploadEvidenceDocumentType.builder()
                .witnessOptionName(DomainConstants.WITNESS_NAME)
                .typeOfDocument(DomainConstants.TYPE_OF_DOCUMENT)
                .documentIssuedDate(DomainConstants.ISSUE_DATE)
                .documentUpload(document)
                .build();

        caseData = CaseData.builder()
                .documentReferredInStatement(toElementArrayList(uploadEvidenceDocumentType))
                .documentReferredInStatementApp2(toElementArrayList(uploadEvidenceDocumentType))
                .documentReferredInStatementRes(toElementArrayList(uploadEvidenceDocumentType))
                .documentReferredInStatementRes2(toElementArrayList(uploadEvidenceDocumentType))
                .build();

        caseDataBefore = CaseData.builder()
                .documentReferredInStatement(new ArrayList<>())
                .build();
    }

    protected void setUpDocumentWitnessStatement() {
        uploadEvidenceWitness = createWitness(DomainConstants.WITNESS_NAME);
        caseData = CaseData.builder()
                .documentWitnessStatement(toElementArrayList(uploadEvidenceWitness))
                .documentWitnessStatementApp2(toElementArrayList(uploadEvidenceWitness))
                .documentWitnessStatementRes(toElementArrayList(uploadEvidenceWitness))
                .documentWitnessStatementRes2(toElementArrayList(uploadEvidenceWitness))
                .build();

        caseDataBefore = CaseData.builder()
                .documentWitnessStatement(new ArrayList<>())
                .build();
    }

    protected void setUpDocumentWitnessSummary() {
        uploadEvidenceWitness = createWitness(DomainConstants.WITNESS_NAME);
        caseData = CaseData.builder()
                .documentWitnessSummary(toElementArrayList(uploadEvidenceWitness))
                .documentWitnessSummaryApp2(toElementArrayList(uploadEvidenceWitness))
                .documentWitnessSummaryRes(toElementArrayList(uploadEvidenceWitness))
                .documentWitnessSummaryRes2(toElementArrayList(uploadEvidenceWitness))
                .build();

        caseDataBefore = CaseData.builder()
                .documentWitnessSummary(new ArrayList<>())
                .build();
    }
}
