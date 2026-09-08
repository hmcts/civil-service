package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondentonesolicitor;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor.RespondentOneWitnessReferredDocumentHandler;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.ORIGINAL_FILE_NAME;

@ExtendWith(MockitoExtension.class)
class RespondentOneWitnessReferredDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private RespondentOneWitnessReferredDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentReferredInStatement();
    }

    @Test
    void shouldCopyWitnessReferredDocumentsToLegalRep2() {
        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore);

        assertEquals(2, caseData.getDocumentReferredInStatementRes2().size());
    }

    @Test
    void shouldRenameDocuments() {
        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, RESPONDENT, notificationBuilder);

        assertEquals("typeOfDocument referred to in the statement of witnessName 10-02-2022.pdf",
                caseData.getDocumentReferredInStatementRes().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }

    @Test
    void shouldRenameDocumentsWithoutIssueDate() {
        Document localDocument = new Document();
        localDocument.setDocumentFileName(ORIGINAL_FILE_NAME);

        UploadEvidenceDocumentType uploadEvidenceDocumentType = new UploadEvidenceDocumentType();
        uploadEvidenceDocumentType.setDocumentIssuedDate(null);
        uploadEvidenceDocumentType.setTypeOfDocument("typeOfDocument");
        uploadEvidenceDocumentType.setWitnessOptionName("witnessName");
        uploadEvidenceDocumentType.setDocumentUpload(localDocument);

        Element<UploadEvidenceDocumentType> element = new Element<>();
        element.setValue(uploadEvidenceDocumentType);

        caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentReferredInStatementRes(List.of(element));

        StringBuilder notificationBuilder = new StringBuilder();

        assertDoesNotThrow(() -> handler.handleDocuments(caseData, RESPONDENT, notificationBuilder));
        assertEquals("typeOfDocument referred to in the statement of witnessName.pdf",
            caseData.getDocumentReferredInStatementRes().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}
