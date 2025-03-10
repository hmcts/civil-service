package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicanttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor.ApplicantTwoTrialSkeletonDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.ORIGINAL_FILE_NAME;

@ExtendWith(MockitoExtension.class)
class ApplicantTwoTrialSkeletonDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantTwoTrialSkeletonDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentSkeletonArgument();
    }

    @Test
    void shouldNotRenameDocuments() {
        handler.handleDocuments(caseData, APPLICANT, new StringBuilder());

        assertEquals(ORIGINAL_FILE_NAME, caseData.getDocumentSkeletonArgumentApp2().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}