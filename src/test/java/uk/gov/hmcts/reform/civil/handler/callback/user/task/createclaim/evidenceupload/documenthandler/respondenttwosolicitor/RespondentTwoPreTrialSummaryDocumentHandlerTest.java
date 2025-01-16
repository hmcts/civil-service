package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondenttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor.RespondentTwoPreTrialSummaryDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RespondentTwoPreTrialSummaryDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private RespondentTwoPreTrialSummaryDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentCaseSummary();
    }

    @Test
    void shouldNotRenameDocuments() {
        handler.handleDocuments(caseData, "Respondent", new StringBuilder());

        assertEquals("OriginalName.pdf", caseData.getDocumentCaseSummaryRes2().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}