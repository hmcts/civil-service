package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondentonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor.RespondentOneTrialCorrespondenceDocumentHandler;
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
public class RespondentOneTrialCorrespondenceDocumentHandlerTest {

    @Mock
    private DocumentTypeBuilder<UploadEvidenceDocumentType> documentTypeBuilder;

    @Mock
    private UploadEvidenceDocumentRetriever uploadDocumentRetriever;

    @InjectMocks
    private RespondentOneTrialCorrespondenceDocumentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RespondentOneTrialCorrespondenceDocumentHandler(documentTypeBuilder, uploadDocumentRetriever);
    }

    @Test
    void shouldCopyTrialCorrespondenceDocumentsToLegalRep2() {
        Document document = Document.builder().documentFileName("OriginalTrialCorrespondence.pdf").build();
        UploadEvidenceDocumentType uploadEvidenceDocumentType = UploadEvidenceDocumentType.builder()
                .documentIssuedDate(LocalDate.of(2022, 2, 10))
                .documentUpload(document)
                .build();

        CaseData caseData = CaseData.builder()
                .documentEvidenceForTrialRes(List.of(
                        Element.<UploadEvidenceDocumentType>builder()
                                .value(uploadEvidenceDocumentType)
                                .build()))
                .build();

        CaseData caseDataBefore = CaseData.builder()
                .documentEvidenceForTrialRes(List.of())
                .build();
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(1, builder.build().getDocumentEvidenceForTrialRes2().size());
    }

    @Test
    void shouldRenameDocumentsWithDateAndBundleName() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        when(uploadDocumentRetriever.getDocument(any())).thenReturn(document);

        LocalDateTime mockDateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        when(uploadDocumentRetriever.getDocumentDateTime(any())).thenReturn(mockDateTime);

        CaseData caseData = CaseData.builder()
                .documentEvidenceForTrialRes(List.of(
                        Element.<UploadEvidenceDocumentType>builder()
                                .value(UploadEvidenceDocumentType.builder()
                                        .documentIssuedDate(LocalDate.of(2022, 2, 10))
                                        .documentUpload(document)
                                        .typeOfDocument("typeOfDocument")
                                        .build())
                                .build()))
                .build();

        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, "Respondent", notificationBuilder);

        assertEquals("Documentary Evidence typeOfDocument 10-02-2022.pdf", caseData.getDocumentEvidenceForTrialRes().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}