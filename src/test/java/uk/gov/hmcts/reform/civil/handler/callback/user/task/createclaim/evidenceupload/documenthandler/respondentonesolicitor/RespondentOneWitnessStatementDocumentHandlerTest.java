package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondentonesolicitor;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor.RespondentOneWitnessStatementDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceWitnessRetriever;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.ORIGINAL_FILE_NAME;

@ExtendWith(MockitoExtension.class)
class RespondentOneWitnessStatementDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private RespondentOneWitnessStatementDocumentHandler handler;

    @Mock
    private DocumentTypeBuilder<UploadEvidenceWitness> witnessDocumentTypeBuilder;

    private final class TestableRespondentOneWitnessStatementDocumentHandler
        extends RespondentOneWitnessStatementDocumentHandler {

        private TestableRespondentOneWitnessStatementDocumentHandler(
            DocumentTypeBuilder<UploadEvidenceWitness> witnessDocumentTypeBuilder,
            UploadEvidenceWitnessRetriever witnessDocumentTypeRetriever) {
            super(witnessDocumentTypeBuilder, witnessDocumentTypeRetriever);
        }

        void renameWithoutDate(List<Element<UploadEvidenceWitness>> documentUploads) {
            renameUploadEvidenceWitness(documentUploads, evidenceUploadType.getDocumentTypeDisplayName(), false);
        }
    }

    @BeforeEach
    void setUp() {
        setUpDocumentWitnessStatement();
    }

    @Test
    void shouldCopyWitnessStatementDocumentsToLegalRep2() {
        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore);

        assertEquals(2, caseData.getDocumentWitnessStatementRes2().size());
    }

    @Test
    void shouldRenameDocuments() {
        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, RESPONDENT, notificationBuilder);

        assertEquals("Witness Statement of witnessName 10-02-2022.pdf",
                caseData.getDocumentWitnessStatementRes().get(0).getValue().getWitnessOptionDocument().getDocumentFileName());
    }

    @Test
    void shouldRenameDocumentsWithoutUploadDate() {
        Document localDocument = new Document();
        localDocument.setDocumentFileName(ORIGINAL_FILE_NAME);

        UploadEvidenceWitness uploadEvidenceWitness = new UploadEvidenceWitness();
        uploadEvidenceWitness.setWitnessOptionName(DomainConstants.WITNESS_NAME);
        uploadEvidenceWitness.setWitnessOptionUploadDate(LocalDate.of(2022, 2, 10));
        uploadEvidenceWitness.setWitnessOptionDocument(localDocument);

        Element<UploadEvidenceWitness> element = new Element<>();
        element.setValue(uploadEvidenceWitness);

        new TestableRespondentOneWitnessStatementDocumentHandler(witnessDocumentTypeBuilder, uploadEvidenceWitnessRetriever)
            .renameWithoutDate(List.of(element));

        assertEquals("Witness Statement of witnessName.pdf",
            uploadEvidenceWitness.getWitnessOptionDocument().getDocumentFileName());
    }
}
