package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicantonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneExpertReportDocumentHandler;
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
public class ApplicantOneExpertReportDocumentHandlerTest {

    @Mock
    private DocumentTypeBuilder<UploadEvidenceExpert> documentTypeBuilder;

    @Mock
    private UploadEvidenceExpertRetriever uploadDocumentRetriever;

    @InjectMocks
    private ApplicantOneExpertReportDocumentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApplicantOneExpertReportDocumentHandler(documentTypeBuilder, uploadDocumentRetriever);
    }

    @Test
    void shouldCopyExpertReportDocumentsToLegalRep2() {
        Document document = Document.builder().documentFileName("OriginalExpertReport.pdf").build();
        UploadEvidenceExpert uploadEvidenceExpert = UploadEvidenceExpert.builder()
                .expertOptionUploadDate(LocalDate.of(2022, 2, 10))
                .expertOptionName("ExpertReportBundle")
                .expertDocument(document)
                .build();

        CaseData caseData = CaseData.builder()
                .documentExpertReport(List.of(
                        Element.<UploadEvidenceExpert>builder()
                                .value(uploadEvidenceExpert)
                                .build()))
                .build();

        CaseData caseDataBefore = CaseData.builder()
                .documentExpertReport(List.of())
                .build();
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(1, builder.build().getDocumentExpertReportApp2().size());
    }

    @Test
    void shouldRenameDocumentsWithDateAndBundleName() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        when(uploadDocumentRetriever.getDocument(any())).thenReturn(document);

        UploadEvidenceExpert uploadEvidenceExpert = UploadEvidenceExpert.builder()
                .expertOptionName("test")
                .expertOptionExpertise("expertise")
                .expertOptionUploadDate(LocalDate.of(2022, 2, 10))
                .expertDocument(document)
                .build();

        CaseData caseData = CaseData.builder()
                .documentExpertReport(List.of(
                        Element.<UploadEvidenceExpert>builder()
                                .value(uploadEvidenceExpert)
                                .build()))
                .build();

        LocalDateTime mockDateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        when(uploadDocumentRetriever.getDocumentDateTime(any())).thenReturn(mockDateTime);

        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, "Applicant", notificationBuilder);

        String expectedDocumentName = "Experts report test expertise 10-02-2022.pdf";
        assertEquals(expectedDocumentName, caseData.getDocumentExpertReport().get(0).getValue().getExpertDocument().getDocumentFileName());
    }
}
