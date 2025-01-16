package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondenttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor.RespondentTwoPreTrialSummaryDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.ORIGINAL_FILE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.RESPONDENT;

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
        handler.handleDocuments(caseData, RESPONDENT, new StringBuilder());

        assertEquals(ORIGINAL_FILE_NAME, caseData.getDocumentCaseSummaryRes2().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}