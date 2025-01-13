package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicantonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneTrialCorrespondenceDocumentHandler;
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
public class ApplicantOneTrialCorrespondenceDocumentHandlerTest {

    @Mock
    private DocumentTypeBuilder<UploadEvidenceDocumentType> documentTypeBuilder;

    @Mock
    private UploadEvidenceDocumentRetriever uploadDocumentRetriever;

    @InjectMocks
    private ApplicantOneTrialCorrespondenceDocumentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApplicantOneTrialCorrespondenceDocumentHandler(documentTypeBuilder, uploadDocumentRetriever);
    }

    @Test
    void shouldCopyTrialCorrespondenceDocumentsToLegalRep2() {
        Document document = Document.builder().documentFileName("OriginalTrialCorrespondence.pdf").build();
        UploadEvidenceDocumentType uploadEvidenceDocumentType = UploadEvidenceDocumentType.builder()
                .documentIssuedDate(LocalDate.of(2022, 2, 10))
                .bundleName("TrialCorrespondenceBundle")
                .documentUpload(document)
                .build();

        CaseData caseData = CaseData.builder()
                .documentEvidenceForTrial(List.of(
                        Element.<UploadEvidenceDocumentType>builder()
                                .value(uploadEvidenceDocumentType)
                                .build()))
                .build();

        CaseData caseDataBefore = CaseData.builder()
                .documentEvidenceForTrial(List.of())
                .build();
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(1, builder.build().getDocumentEvidenceForTrialApp2().size());
    }

    @Test
    void shouldRenameDocumentsWithDateAndBundleName() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        when(uploadDocumentRetriever.getDocument(any())).thenReturn(document);

        UploadEvidenceDocumentType uploadEvidenceDocumentType = UploadEvidenceDocumentType.builder()
                .typeOfDocument("typeOfDocument")
                .documentIssuedDate(LocalDate.of(2022, 2, 10))
                .documentUpload(document)
                .build();

        CaseData caseData = CaseData.builder()
                .documentEvidenceForTrial(List.of(
                        Element.<UploadEvidenceDocumentType>builder()
                                .value(uploadEvidenceDocumentType)
                                .build()))
                .build();

        LocalDateTime mockDateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        when(uploadDocumentRetriever.getDocumentDateTime(any())).thenReturn(mockDateTime);

        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, "Applicant", notificationBuilder);

        String expectedDocumentName = "Documentary Evidence typeOfDocument 10-02-2022.pdf";
        assertEquals(expectedDocumentName, caseData.getDocumentEvidenceForTrial().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}
