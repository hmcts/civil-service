package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondenttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor.RespondentTwoExpertQuestionsDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RespondentTwoExpertQuestionsDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private RespondentTwoExpertQuestionsDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentQuestions();
    }

    @Test
    void shouldRenameDocuments() {
        handler.handleDocuments(caseData, "Respondent", new StringBuilder());

        assertEquals("test Other Party Document Question.pdf", caseData.getDocumentQuestionsRes2().get(0).getValue().getExpertDocument().getDocumentFileName());
    }
}