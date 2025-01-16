package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicantonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneAuthoritiesDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.ORIGINAL_FILE_NAME;

public class ApplicantOneAuthoritiesDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantOneAuthoritiesDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentAuthorities();
    }

    @Test
    void shouldCopyLegalRep1ChangesToLegalRep2() {
        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(2, builder.build().getDocumentAuthoritiesApp2().size());
    }

    @Test
    void shouldNotRenameDocuments() {
        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, APPLICANT, notificationBuilder);

        assertEquals(ORIGINAL_FILE_NAME, caseData.getDocumentAuthorities().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}
