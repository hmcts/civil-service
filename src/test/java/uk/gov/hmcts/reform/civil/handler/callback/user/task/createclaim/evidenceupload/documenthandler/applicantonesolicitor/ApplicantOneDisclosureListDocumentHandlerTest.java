package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicantonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneDisclosureListDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.ORIGINAL_FILE_NAME;

@ExtendWith(MockitoExtension.class)
public class ApplicantOneDisclosureListDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantOneDisclosureListDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentForDisclosureList();
    }

    @Test
    void shouldCopyDisclosureListDocumentsToLegalRep2() {
        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(2, builder.build().getDocumentDisclosureListApp2().size());
    }

    @Test
    void shouldNotRenameDocuments() {
        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, APPLICANT, notificationBuilder);

        assertEquals(ORIGINAL_FILE_NAME, caseData.getDocumentDisclosureList().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}