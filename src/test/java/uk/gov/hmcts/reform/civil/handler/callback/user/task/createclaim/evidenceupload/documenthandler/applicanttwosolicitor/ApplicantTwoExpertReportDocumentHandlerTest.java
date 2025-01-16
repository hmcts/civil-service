package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicanttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor.ApplicantTwoExpertReportDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ApplicantTwoExpertReportDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantTwoExpertReportDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentExpertReport();
    }

    @Test
    void shouldRenameDocuments() {
        handler.handleDocuments(caseData, "Applicant", new StringBuilder());

        assertEquals("Experts report test expertise 10-02-2022.pdf", caseData.getDocumentExpertReportApp2().get(0).getValue().getExpertDocument().getDocumentFileName());
    }
}