package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondentonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor.RespondentOneWitnessSummaryDocumentHandler;
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
public class RespondentOneWitnessSummaryDocumentHandlerTest {

    @Mock
    private DocumentTypeBuilder<UploadEvidenceWitness> documentTypeBuilder;

    @Mock
    private UploadEvidenceWitnessRetriever uploadDocumentRetriever;

    @InjectMocks
    private RespondentOneWitnessSummaryDocumentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RespondentOneWitnessSummaryDocumentHandler(documentTypeBuilder, uploadDocumentRetriever);
    }

    @Test
    void shouldCopyWitnessSummaryDocumentsToLegalRep2() {
        Document document = Document.builder().documentFileName("OriginalWitnessSummary.pdf").build();
        UploadEvidenceWitness uploadEvidenceWitness = UploadEvidenceWitness.builder()
                .witnessOptionUploadDate(LocalDate.of(2022, 2, 10))
                .witnessOptionDocument(document)
                .build();

        CaseData caseData = CaseData.builder()
                .documentWitnessSummaryRes(List.of(
                        Element.<UploadEvidenceWitness>builder()
                                .value(uploadEvidenceWitness)
                                .build()))
                .build();

        CaseData caseDataBefore = CaseData.builder()
                .documentWitnessSummaryRes(List.of())
                .build();
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(1, builder.build().getDocumentWitnessSummaryRes2().size());
    }

    @Test
    void shouldRenameDocumentsWithDateAndBundleName() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        when(uploadDocumentRetriever.getDocument(any())).thenReturn(document);

        LocalDateTime mockDateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        when(uploadDocumentRetriever.getDocumentDateTime(any())).thenReturn(mockDateTime);

        CaseData caseData = CaseData.builder()
                .documentWitnessSummaryRes(List.of(
                        Element.<UploadEvidenceWitness>builder()
                                .value(UploadEvidenceWitness.builder()
                                        .witnessOptionName("witnessName")
                                        .witnessOptionUploadDate(LocalDate.of(2022, 2, 10))
                                        .witnessOptionDocument(document)
                                        .build())
                                .build()))
                .build();

        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, "Respondent", notificationBuilder);

        assertEquals("Witness Summary of witnessName 10-02-2022.pdf", caseData.getDocumentWitnessSummaryRes().get(0).getValue().getWitnessOptionDocument().getDocumentFileName());
    }
}