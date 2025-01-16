package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondenttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor.RespondentTwoWitnessReferredDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RespondentTwoWitnessReferredDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private RespondentTwoWitnessReferredDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentReferredInStatement();
    }

    @Test
    void shouldNotRenameDocuments() {
        handler.handleDocuments(caseData, "Respondent", new StringBuilder());

        assertEquals("typeOfDocument referred to in the statement of witnessName 10-02-2022.pdf",
                caseData.getDocumentReferredInStatementRes2().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}