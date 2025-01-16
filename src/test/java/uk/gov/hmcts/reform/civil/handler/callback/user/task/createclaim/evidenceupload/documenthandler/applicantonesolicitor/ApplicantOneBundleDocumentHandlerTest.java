package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicantonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneBundleDocumentHandler;

import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.APPLICANT;

@ExtendWith(MockitoExtension.class)
public class ApplicantOneBundleDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantOneBundleDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpBundleEvidence();
    }

    @Test
    void shouldNotCopyDocumentsToLegalRep2AndPreserveLegalRep1Changes() {
        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(2, builder.build().getBundleEvidence().size());
        assertFalse(handler.shouldCopyDocumentsToLegalRep2(), "Expected shouldCopyDocumentsToLegalRep2 to return false");
    }

    @Test
    void shouldRenameDocuments() {
        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, APPLICANT, notificationBuilder);

        String expectedDocumentName = mockDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "-test.pdf";
        assertEquals(expectedDocumentName, caseData.getBundleEvidence().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}