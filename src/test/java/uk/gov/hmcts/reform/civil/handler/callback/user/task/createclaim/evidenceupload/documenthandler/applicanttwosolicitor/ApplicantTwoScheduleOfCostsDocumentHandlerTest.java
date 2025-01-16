package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicanttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor.ApplicantTwoScheduleOfCostsDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ApplicantTwoScheduleOfCostsDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantTwoScheduleOfCostsDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentCosts();
    }

    @Test
    void shouldNotRenameDocuments() {
        handler.handleDocuments(caseData, "Applicant", new StringBuilder());

        assertEquals("OriginalName.pdf", caseData.getDocumentCostsApp2().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}