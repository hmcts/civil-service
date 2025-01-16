package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicanttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor.ApplicantTwoAuthoritiesDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.ORIGINAL_FILE_NAME;

@ExtendWith(MockitoExtension.class)
public class ApplicantTwoAuthoritiesDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantTwoAuthoritiesDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentAuthorities();
    }

    @Test
    void shouldNotRenameDocuments() {
        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, APPLICANT, notificationBuilder);

        assertEquals(ORIGINAL_FILE_NAME, caseData.getDocumentAuthoritiesApp2().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}