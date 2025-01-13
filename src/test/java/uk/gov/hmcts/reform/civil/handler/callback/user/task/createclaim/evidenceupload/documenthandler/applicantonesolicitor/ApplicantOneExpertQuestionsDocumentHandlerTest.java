package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicantonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneExpertQuestionsDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceExpertRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicantOneExpertQuestionsDocumentHandlerTest {

    @Mock
    private DocumentTypeBuilder<UploadEvidenceExpert> documentTypeBuilder;

    @Mock
    private UploadEvidenceExpertRetriever uploadDocumentRetriever;

    @InjectMocks
    private ApplicantOneExpertQuestionsDocumentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApplicantOneExpertQuestionsDocumentHandler(documentTypeBuilder, uploadDocumentRetriever);
    }

    @Test
    void shouldCopyExpertQuestionsDocumentsToLegalRep2() {
        Document document = Document.builder().documentFileName("OriginalExpertQuestions.pdf").build();
        UploadEvidenceExpert uploadEvidenceExpert = UploadEvidenceExpert.builder()
                .expertOptionUploadDate(LocalDate.of(2022, 2, 10))
                .expertOptionName("ExpertQuestionsBundle")
                .expertDocument(document)
                .build();

        CaseData caseData = CaseData.builder()
                .documentQuestions(List.of(
                        Element.<UploadEvidenceExpert>builder()
                                .value(uploadEvidenceExpert)
                                .build()))
                .build();

        CaseData caseDataBefore = CaseData.builder()
                .documentQuestions(List.of())
                .build();
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(1, builder.build().getDocumentQuestionsApp2().size());
    }

    @Test
    void shouldRenameDocumentsWithDateAndBundleName() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        when(uploadDocumentRetriever.getDocument(any())).thenReturn(document);

        UploadEvidenceExpert uploadEvidenceExpert = UploadEvidenceExpert.builder()
                .expertOptionName("test")
                .expertOptionOtherParty("otherParty")
                .expertDocumentQuestion("documentQuestion")
                .expertDocument(document)
                .build();

        CaseData caseData = CaseData.builder()
                .documentQuestions(List.of(
                        Element.<UploadEvidenceExpert>builder()
                                .value(uploadEvidenceExpert)
                                .build()))
                .build();

        LocalDateTime mockDateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        when(uploadDocumentRetriever.getDocumentDateTime(any())).thenReturn(mockDateTime);

        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, "Applicant", notificationBuilder);

        String expectedDocumentName = "test otherParty documentQuestion.pdf";
        assertEquals(expectedDocumentName, caseData.getDocumentQuestions().get(0).getValue().getExpertDocument().getDocumentFileName());
    }
}
