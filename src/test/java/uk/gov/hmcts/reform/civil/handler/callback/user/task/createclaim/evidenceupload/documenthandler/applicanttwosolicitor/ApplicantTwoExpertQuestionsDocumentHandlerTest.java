package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicanttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor.ApplicantTwoExpertQuestionsDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ApplicantTwoExpertQuestionsDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantTwoExpertQuestionsDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentQuestions();
    }

    @Test
    void shouldRenameDocuments() {
        handler.handleDocuments(caseData, "Applicant", new StringBuilder());

        assertEquals("test Other Party Document Question.pdf", caseData.getDocumentQuestionsApp2().get(0).getValue().getExpertDocument().getDocumentFileName());
    }
}