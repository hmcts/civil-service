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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

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
    }

    private Document createMockDocument() {

        Document document1 = new Document();
        document1.setDocumentFileName(DomainConstants.ORIGINAL_FILE_NAME);
        return document1;
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
        Element<T> element = new Element<>();
        element.setValue(value);

        List<Element<T>> list = new ArrayList<>(1);
        list.add(element);
        return list;
    }

    protected UploadEvidenceDocumentType createDocumentType(String bundleName, String typeOfDocument) {
        UploadEvidenceDocumentType documentType = new UploadEvidenceDocumentType()
                .setDocumentIssuedDate(DomainConstants.ISSUE_DATE)
                .setBundleName(bundleName)
                .setDocumentUpload(document);

        if (typeOfDocument != null) {
            documentType.setTypeOfDocument(typeOfDocument);
        }

        return documentType;
    }

    protected UploadEvidenceExpert createExpert(String name) {
        UploadEvidenceExpert uploadEvidenceExpert1 = new UploadEvidenceExpert();
        uploadEvidenceExpert1.setExpertOptionName(name);
        uploadEvidenceExpert1.setExpertOptionUploadDate(DomainConstants.ISSUE_DATE);
        uploadEvidenceExpert1.setExpertDocument(document);
        return uploadEvidenceExpert1;
    }

    protected UploadEvidenceWitness createWitness(String name) {
        UploadEvidenceWitness uploadEvidenceWitness1 = new UploadEvidenceWitness();
        uploadEvidenceWitness1.setWitnessOptionName(name);
        uploadEvidenceWitness1.setWitnessOptionUploadDate(DomainConstants.ISSUE_DATE);
        uploadEvidenceWitness1.setWitnessOptionDocument(document);
        return uploadEvidenceWitness1;
    }

    protected void setUpDocumentAuthorities() {
        uploadEvidenceDocumentType = createDocumentType(DomainConstants.BUNDLE_TEST, null);
        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentAuthorities(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentAuthoritiesApp2(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentAuthoritiesRes(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentAuthoritiesRes2(toElementArrayList(uploadEvidenceDocumentType));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentAuthorities(new ArrayList<>());
    }

    protected void setUpBundleEvidence() {
        uploadEvidenceDocumentType = createDocumentType(DomainConstants.BUNDLE_TEST, null);
        caseData = CaseDataBuilder.builder().build();
        caseData.setBundleEvidence(toElementArrayList(uploadEvidenceDocumentType));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setBundleEvidence(new ArrayList<>());
    }

    protected void setUpDocumentForDisclosure() {
        uploadEvidenceDocumentType = createDocumentType(
                DomainConstants.BUNDLE_DISCLOSURE,
                DomainConstants.BUNDLE_TEST
        );
        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentForDisclosure(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentForDisclosureApp2(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentForDisclosureRes(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentForDisclosureRes2(toElementArrayList(uploadEvidenceDocumentType));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentForDisclosure(new ArrayList<>());
    }

    protected void setUpDocumentForDisclosureList() {
        uploadEvidenceDocumentType = createDocumentType(DomainConstants.BUNDLE_DISCLOSURE_LIST, null);
        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentDisclosureList(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentDisclosureListApp2(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentDisclosureListRes(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentDisclosureListRes2(toElementArrayList(uploadEvidenceDocumentType));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentDisclosureList(new ArrayList<>());
    }

    protected void setUpDocumentAnswers() {
        uploadEvidenceExpert = createExpert("ExpertOptionNameTest");
        uploadEvidenceExpert.setExpertOptionOtherParty("ExpertOptionOtherPartyTest");
        uploadEvidenceExpert.setExpertDocumentAnswer("ExpertDocumentAnswerTest");

        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentAnswers(toElementArrayList(uploadEvidenceExpert));
        caseData.setDocumentAnswersApp2(toElementArrayList(uploadEvidenceExpert));
        caseData.setDocumentAnswersRes(toElementArrayList(uploadEvidenceExpert));
        caseData.setDocumentAnswersRes2(toElementArrayList(uploadEvidenceExpert));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentAnswers(new ArrayList<>());
    }

    protected void setUpDocumentJointStatement() {
        uploadEvidenceExpert = createExpert(DomainConstants.BUNDLE_TEST);
        uploadEvidenceExpert.setExpertOptionExpertises("expertises");

        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentJointStatement(toElementArrayList(uploadEvidenceExpert));
        caseData.setDocumentJointStatementApp2(toElementArrayList(uploadEvidenceExpert));
        caseData.setDocumentJointStatementRes(toElementArrayList(uploadEvidenceExpert));
        caseData.setDocumentJointStatementRes2(toElementArrayList(uploadEvidenceExpert));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentJointStatement(new ArrayList<>());
    }

    protected void setUpDocumentQuestions() {
        uploadEvidenceExpert = createExpert(DomainConstants.BUNDLE_TEST);
        uploadEvidenceExpert.setExpertDocumentQuestion("Document Question");
        uploadEvidenceExpert.setExpertOptionOtherParty("Other Party");

        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentQuestions(toElementArrayList(uploadEvidenceExpert));
        caseData.setDocumentQuestionsApp2(toElementArrayList(uploadEvidenceExpert));
        caseData.setDocumentQuestionsRes(toElementArrayList(uploadEvidenceExpert));
        caseData.setDocumentQuestionsRes2(toElementArrayList(uploadEvidenceExpert));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentQuestions(new ArrayList<>());
    }

    protected void setUpDocumentExpertReport() {
        uploadEvidenceExpert = createExpert(DomainConstants.BUNDLE_TEST);
        uploadEvidenceExpert.setExpertOptionExpertise("expertise");

        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentExpertReport(toElementArrayList(uploadEvidenceExpert));
        caseData.setDocumentExpertReportApp2(toElementArrayList(uploadEvidenceExpert));
        caseData.setDocumentExpertReportRes(toElementArrayList(uploadEvidenceExpert));
        caseData.setDocumentExpertReportRes2(toElementArrayList(uploadEvidenceExpert));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentExpertReport(new ArrayList<>());
    }

    protected void setUpDocumentCaseSummary() {
        uploadEvidenceDocumentType = createDocumentType(DomainConstants.BUNDLE_PRE_TRIAL_SUMMARY, null);
        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentCaseSummary(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentCaseSummaryApp2(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentCaseSummaryRes(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentCaseSummaryRes2(toElementArrayList(uploadEvidenceDocumentType));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentCaseSummary(new ArrayList<>());
    }

    protected void setUpDocumentCosts() {
        uploadEvidenceDocumentType = createDocumentType(DomainConstants.BUNDLE_SCHEDULE_OF_COSTS, null);
        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentCosts(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentCostsApp2(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentCostsRes(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentCostsRes2(toElementArrayList(uploadEvidenceDocumentType));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentCosts(new ArrayList<>());
    }

    protected void setUpDocumentEvidenceForTrial() {
        uploadEvidenceDocumentType = createDocumentType(
                DomainConstants.BUNDLE_TRIAL_CORRESPONDENCE,
                DomainConstants.TYPE_OF_DOCUMENT
        );
        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentEvidenceForTrial(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentEvidenceForTrialApp2(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentEvidenceForTrialRes(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentEvidenceForTrialRes2(toElementArrayList(uploadEvidenceDocumentType));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentEvidenceForTrial(new ArrayList<>());
    }

    protected void setUpDocumentSkeletonArgument() {
        uploadEvidenceDocumentType = createDocumentType(DomainConstants.BUNDLE_TRIAL_SKELETON, null);
        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentSkeletonArgument(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentSkeletonArgumentApp2(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentSkeletonArgumentRes(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentSkeletonArgumentRes2(toElementArrayList(uploadEvidenceDocumentType));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentSkeletonArgument(new ArrayList<>());
    }

    protected void setUpDocumentHearsayNotice() {
        uploadEvidenceWitness = createWitness(DomainConstants.WITNESS_HEARSAY_BUNDLE);
        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentHearsayNotice(toElementArrayList(uploadEvidenceWitness));
        caseData.setDocumentHearsayNoticeApp2(toElementArrayList(uploadEvidenceWitness));
        caseData.setDocumentHearsayNoticeRes(toElementArrayList(uploadEvidenceWitness));
        caseData.setDocumentHearsayNoticeRes2(toElementArrayList(uploadEvidenceWitness));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentHearsayNotice(new ArrayList<>());
    }

    protected void setUpDocumentReferredInStatement() {
        uploadEvidenceDocumentType = new UploadEvidenceDocumentType()
                .setWitnessOptionName(DomainConstants.WITNESS_NAME)
                .setTypeOfDocument(DomainConstants.TYPE_OF_DOCUMENT)
                .setDocumentIssuedDate(DomainConstants.ISSUE_DATE)
                .setDocumentUpload(document);

        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentReferredInStatement(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentReferredInStatementApp2(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentReferredInStatementRes(toElementArrayList(uploadEvidenceDocumentType));
        caseData.setDocumentReferredInStatementRes2(toElementArrayList(uploadEvidenceDocumentType));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentReferredInStatement(new ArrayList<>());
    }

    protected void setUpDocumentWitnessStatement() {
        uploadEvidenceWitness = createWitness(DomainConstants.WITNESS_NAME);
        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentWitnessStatement(toElementArrayList(uploadEvidenceWitness));
        caseData.setDocumentWitnessStatementApp2(toElementArrayList(uploadEvidenceWitness));
        caseData.setDocumentWitnessStatementRes(toElementArrayList(uploadEvidenceWitness));
        caseData.setDocumentWitnessStatementRes2(toElementArrayList(uploadEvidenceWitness));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentWitnessStatement(new ArrayList<>());
    }

    protected void setUpDocumentWitnessSummary() {
        uploadEvidenceWitness = createWitness(DomainConstants.WITNESS_NAME);
        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentWitnessSummary(toElementArrayList(uploadEvidenceWitness));
        caseData.setDocumentWitnessSummaryApp2(toElementArrayList(uploadEvidenceWitness));
        caseData.setDocumentWitnessSummaryRes(toElementArrayList(uploadEvidenceWitness));
        caseData.setDocumentWitnessSummaryRes2(toElementArrayList(uploadEvidenceWitness));

        caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setDocumentWitnessSummary(new ArrayList<>());
    }
}
