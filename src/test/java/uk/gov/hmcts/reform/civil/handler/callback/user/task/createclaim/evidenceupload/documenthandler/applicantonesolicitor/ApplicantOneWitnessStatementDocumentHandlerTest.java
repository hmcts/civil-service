package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicantonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneWitnessStatementDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceWitnessRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicantOneWitnessStatementDocumentHandlerTest {

    @Mock
    private DocumentTypeBuilder<UploadEvidenceWitness> documentTypeBuilder;

    @Mock
    private UploadEvidenceWitnessRetriever uploadDocumentRetriever;

    @InjectMocks
    private ApplicantOneWitnessStatementDocumentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApplicantOneWitnessStatementDocumentHandler(documentTypeBuilder, uploadDocumentRetriever);
    }

    @Test
    void shouldCopyWitnessStatementDocumentsToLegalRep2() {
        Document document = Document.builder().documentFileName("OriginalWitnessStatement.pdf").build();
        UploadEvidenceWitness uploadEvidenceWitness = UploadEvidenceWitness.builder()
                .witnessOptionName("witnessName")
                .witnessOptionUploadDate(LocalDate.of(2022, 2, 10))
                .witnessOptionDocument(document)
                .build();

        CaseData caseData = CaseData.builder()
                .documentWitnessStatement(List.of(
                        Element.<UploadEvidenceWitness>builder()
                                .value(uploadEvidenceWitness)
                                .build()))
                .build();

        CaseData caseDataBefore = CaseData.builder()
                .documentWitnessStatement(List.of())
                .build();
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(1, builder.build().getDocumentWitnessStatementApp2().size());
    }

    @Test
    void shouldRenameDocumentsWithDateAndBundleName() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        when(uploadDocumentRetriever.getDocument(any())).thenReturn(document);

        UploadEvidenceWitness uploadEvidenceWitness = UploadEvidenceWitness.builder()
                .witnessOptionName("witnessName")
                .witnessOptionUploadDate(LocalDate.of(2022, 2, 10))
                .witnessOptionDocument(document)
                .build();

        CaseData caseData = CaseData.builder()
                .documentWitnessStatement(List.of(
                        Element.<UploadEvidenceWitness>builder()
                                .value(uploadEvidenceWitness)
                                .build()))
                .build();

        LocalDateTime mockDateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        when(uploadDocumentRetriever.getDocumentDateTime(any())).thenReturn(mockDateTime);

        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, "Applicant", notificationBuilder);

        String expectedDocumentName = "Witness Statement of witnessName 10-02-2022.pdf";
        assertEquals(expectedDocumentName, caseData.getDocumentWitnessStatement().get(0).getValue().getWitnessOptionDocument().getDocumentFileName());
    }
}
