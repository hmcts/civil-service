package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondentonesolicitor;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor.RespondentOneDisclosureDocumentHandler;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.ORIGINAL_FILE_NAME;

@ExtendWith(MockitoExtension.class)
class RespondentOneDisclosureDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private RespondentOneDisclosureDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentForDisclosure();
    }

    @Test
    void shouldCopyDisclosureDocumentsToLegalRep2() {
        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore);

        assertEquals(2, caseData.getDocumentForDisclosureRes2().size());
    }

    @Test
    void shouldRenameDocuments() {
        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, RESPONDENT, notificationBuilder);

        assertEquals("Document for disclosure test 10-02-2022.pdf", caseData.getDocumentForDisclosureRes().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }

    @Test
    void shouldSkipMalformedDocumentAndContinueProcessingBatch() {
        Document validDocument = new Document();
        validDocument.setDocumentFileName(ORIGINAL_FILE_NAME);

        UploadEvidenceDocumentType validUpload = new UploadEvidenceDocumentType();
        validUpload.setDocumentIssuedDate(LocalDate.of(2022, 2, 10));
        validUpload.setTypeOfDocument("typeOfDocument");
        validUpload.setDocumentUpload(validDocument);

        UploadEvidenceDocumentType malformedUpload = new UploadEvidenceDocumentType();
        malformedUpload.setDocumentUpload(null);
        malformedUpload.setTypeOfDocument("typeOfDocument");

        Element<UploadEvidenceDocumentType> validElement = new Element<>();
        validElement.setValue(validUpload);
        Element<UploadEvidenceDocumentType> malformedElement = new Element<>();
        malformedElement.setValue(malformedUpload);

        caseData = CaseDataBuilder.builder().build();
        List<Element<UploadEvidenceDocumentType>> documents = new ArrayList<>();
        documents.add(validElement);
        documents.add(malformedElement);
        caseData.setDocumentForDisclosureRes(documents);

        StringBuilder notificationBuilder = new StringBuilder();

        assertDoesNotThrow(() -> handler.handleDocuments(caseData, RESPONDENT, notificationBuilder));
        assertEquals("Document for disclosure typeOfDocument 10-02-2022.pdf",
            caseData.getDocumentForDisclosureRes().get(0).getValue().getDocumentUpload().getDocumentFileName());
        org.junit.jupiter.api.Assertions.assertNull(caseData.getDocumentForDisclosureRes().get(1).getValue().getDocumentUpload());
    }
}
