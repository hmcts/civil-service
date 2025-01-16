package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondenttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor.RespondentTwoExpertJointStatmementDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.RESPONDENT;

@ExtendWith(MockitoExtension.class)
public class RespondentTwoExpertJointStatmementDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private RespondentTwoExpertJointStatmementDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentJointStatement();
    }

    @Test
    void shouldRenameDocuments() {
        handler.handleDocuments(caseData, RESPONDENT, new StringBuilder());

        assertEquals("Joint report test expertises 10-02-2022.pdf", caseData.getDocumentJointStatementRes2().get(0).getValue().getExpertDocument().getDocumentFileName());
    }
}