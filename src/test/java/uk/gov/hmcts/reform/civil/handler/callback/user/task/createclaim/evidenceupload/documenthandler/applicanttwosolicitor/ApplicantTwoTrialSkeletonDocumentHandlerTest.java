package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicanttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor.ApplicantTwoTrialSkeletonDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ApplicantTwoTrialSkeletonDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantTwoTrialSkeletonDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentSkeletonArgument();
    }

    @Test
    void shouldNotRenameDocuments() {
        handler.handleDocuments(caseData, "Applicant", new StringBuilder());

        assertEquals("OriginalName.pdf", caseData.getDocumentSkeletonArgumentApp2().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}