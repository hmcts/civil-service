package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneAuthoritiesDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceDocumentRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicantOneAuthoritiesDocumentHandlerTest {

    @Mock
    private DocumentTypeBuilder<UploadEvidenceDocumentType> documentTypeBuilder;

    @Mock
    private UploadEvidenceDocumentRetriever uploadDocumentRetriever;

    @InjectMocks
    private ApplicantOneAuthoritiesDocumentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApplicantOneAuthoritiesDocumentHandler(documentTypeBuilder, uploadDocumentRetriever);
    }

    @Test
    void shouldCopyLegalRep1ChangesToLegalRep2() {
        Document document = Document.builder().build();
        UploadEvidenceDocumentType uploadEvidenceDocumentType = UploadEvidenceDocumentType.builder()
            .documentIssuedDate(LocalDate.of(2022, 2, 10))
            .bundleName("test")
            .documentUpload(document)
            .build();

        CaseData caseData = CaseData.builder()
            .documentAuthorities(List.of(
                Element.<UploadEvidenceDocumentType>builder()
                    .value(uploadEvidenceDocumentType)
                    .build()))
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .documentAuthorities(List.of())
            .build();

        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(1, builder.build().getDocumentAuthoritiesApp2().size());
    }

    @Test
    void shouldNotRenameDocumentsIndirectlyThroughHandleDocuments() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        when(uploadDocumentRetriever.getDocument(any())).thenReturn(document);

        CaseData caseData = CaseData.builder()
            .documentAuthorities(List.of(
                Element.<UploadEvidenceDocumentType>builder()
                    .value(UploadEvidenceDocumentType.builder()
                               .documentIssuedDate(LocalDate.of(2022, 2, 10))
                               .bundleName("test")
                               .documentUpload(document)
                               .build())
                    .build()))
            .build();

        LocalDateTime mockDateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        when(uploadDocumentRetriever.getDocumentDateTime(any())).thenReturn(mockDateTime);

        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, "Applicant", notificationBuilder);

        assertEquals("OriginalName.pdf", caseData.getDocumentAuthorities().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}

