package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicantonesolicitor;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneBundleDocumentHandler;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.ORIGINAL_FILE_NAME;

@ExtendWith(MockitoExtension.class)
class ApplicantOneBundleDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantOneBundleDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpBundleEvidence();
    }

    @Test
    void shouldNotCopyDocumentsToLegalRep2AndPreserveLegalRep1Changes() {
        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore);

        assertEquals(2, caseData.getBundleEvidence().size());
        assertFalse(handler.shouldCopyDocumentsToLegalRep2(), "Expected shouldCopyDocumentsToLegalRep2 to return false");
    }

    @Test
    void shouldRenameDocuments() {
        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, APPLICANT, notificationBuilder);

        String expectedDocumentName = mockDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "-test.pdf";
        assertEquals(expectedDocumentName, caseData.getBundleEvidence().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }

    @Test
    void shouldRenameDocumentsWithoutIssueDate() {
        Document localDocument = new Document();
        localDocument.setDocumentFileName(ORIGINAL_FILE_NAME);

        UploadEvidenceDocumentType uploadEvidenceDocumentType = new UploadEvidenceDocumentType();
        uploadEvidenceDocumentType.setDocumentIssuedDate(null);
        uploadEvidenceDocumentType.setBundleName(DomainConstants.BUNDLE_TEST);
        uploadEvidenceDocumentType.setDocumentUpload(localDocument);

        Element<UploadEvidenceDocumentType> element = new Element<>();
        element.setValue(uploadEvidenceDocumentType);

        caseData = CaseDataBuilder.builder().build();
        java.util.List<Element<UploadEvidenceDocumentType>> documents = new ArrayList<>();
        documents.add(element);
        caseData.setBundleEvidence(documents);

        StringBuilder notificationBuilder = new StringBuilder();

        assertDoesNotThrow(() -> handler.handleDocuments(caseData, APPLICANT, notificationBuilder));
        assertEquals("test.pdf", caseData.getBundleEvidence().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}
