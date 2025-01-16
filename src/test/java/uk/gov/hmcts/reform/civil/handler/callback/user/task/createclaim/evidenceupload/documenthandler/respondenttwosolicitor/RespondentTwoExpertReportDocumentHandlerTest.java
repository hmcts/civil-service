package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondenttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor.RespondentTwoExpertReportDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RespondentTwoExpertReportDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private RespondentTwoExpertReportDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentExpertReport();
    }

    @Test
    void shouldRenameDocuments() {
        handler.handleDocuments(caseData, "Respondent", new StringBuilder());

        assertEquals("Experts report test expertise 10-02-2022.pdf", caseData.getDocumentExpertReportRes2().get(0).getValue().getExpertDocument().getDocumentFileName());
    }
}