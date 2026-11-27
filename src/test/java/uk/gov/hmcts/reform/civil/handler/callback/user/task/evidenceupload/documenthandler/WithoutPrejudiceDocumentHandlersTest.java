package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentWithDescriptionTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneWithoutPrejudiceDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor.ApplicantTwoWithoutPrejudiceDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor.RespondentOneWithoutPrejudiceDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor.RespondentTwoWithoutPrejudiceDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadDocumentWithDescriptionRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithDescription;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.WITHOUT_PREJUDICE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

class WithoutPrejudiceDocumentHandlersTest {

    private DocumentWithDescriptionTypeBuilder typeBuilder;
    private UploadDocumentWithDescriptionRetriever retriever;

    @BeforeEach
    void setUp() {
        typeBuilder = new DocumentWithDescriptionTypeBuilder();
        retriever = new UploadDocumentWithDescriptionRetriever();
    }

    @Test
    void applicantOneHandleDocumentsSetsCategoryOnClaimantList() {
        var handler = new ApplicantOneWithoutPrejudiceDocumentHandler(typeBuilder, retriever);
        Document document = new Document().setDocumentFileName("wp.pdf");
        CaseData caseData = CaseData.builder()
            .documentPart36Rejection(List.of(element(new DocumentWithDescription(
                document, "desc", LocalDateTime.now(), "user"))))
            .build();

        handler.handleDocuments(caseData, "Applicant", new StringBuilder());

        assertThat(document.getCategoryID()).isEqualTo(WITHOUT_PREJUDICE.getCategoryId());
    }

    @Test
    void applicantTwoHandleDocumentsSetsCategoryOnClaimantTwoList() {
        var handler = new ApplicantTwoWithoutPrejudiceDocumentHandler(retriever);
        Document document = new Document().setDocumentFileName("wp.pdf");
        CaseData caseData = CaseData.builder()
            .documentPart36RejectionApp2(List.of(element(new DocumentWithDescription(
                document, "desc", LocalDateTime.now(), "user"))))
            .build();

        handler.handleDocuments(caseData, "Applicant", new StringBuilder());

        assertThat(document.getCategoryID()).isEqualTo(WITHOUT_PREJUDICE.getCategoryId());
    }

    @Test
    void respondentOneHandleDocumentsSetsCategoryOnRespondentList() {
        var handler = new RespondentOneWithoutPrejudiceDocumentHandler(typeBuilder, retriever);
        Document document = new Document().setDocumentFileName("wp.pdf");
        CaseData caseData = CaseData.builder()
            .documentPart36RejectionRes(List.of(element(new DocumentWithDescription(
                document, "desc", LocalDateTime.now(), "user"))))
            .build();

        handler.handleDocuments(caseData, "Respondent", new StringBuilder());

        assertThat(document.getCategoryID()).isEqualTo(WITHOUT_PREJUDICE.getCategoryId());
    }

    @Test
    void respondentTwoHandleDocumentsSetsCategoryOnRespondentTwoList() {
        var handler = new RespondentTwoWithoutPrejudiceDocumentHandler(retriever);
        Document document = new Document().setDocumentFileName("wp.pdf");
        CaseData caseData = CaseData.builder()
            .documentPart36RejectionRes2(List.of(element(new DocumentWithDescription(
                document, "desc", LocalDateTime.now(), "user"))))
            .build();

        handler.handleDocuments(caseData, "Respondent", new StringBuilder());

        assertThat(document.getCategoryID()).isEqualTo(WITHOUT_PREJUDICE.getCategoryId());
    }

    @Test
    void withoutPrejudiceHandlersShouldNotPopulatePostBundleUploadList() {
        List<DocumentHandler<?>> handlers = List.of(
            new ApplicantOneWithoutPrejudiceDocumentHandler(typeBuilder, retriever),
            new ApplicantTwoWithoutPrejudiceDocumentHandler(retriever),
            new RespondentOneWithoutPrejudiceDocumentHandler(typeBuilder, retriever),
            new RespondentTwoWithoutPrejudiceDocumentHandler(retriever)
        );

        CaseData caseData = CaseData.builder().build();

        for (DocumentHandler<?> handler : handlers) {
            assertThat(handler.shouldPopulatePostBundleUploadList()).isFalse();
            handler.addUploadDocList(caseData);
        }
    }
}
