package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicanttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor.ApplicantTwoWitnessStatementDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.APPLICANT;

@ExtendWith(MockitoExtension.class)
public class ApplicantTwoWitnessStatementDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantTwoWitnessStatementDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentWitnessStatement();
    }

    @Test
    void shouldNotRenameDocuments() {
        handler.handleDocuments(caseData, APPLICANT, new StringBuilder());

        assertEquals("Witness Statement of witnessName 10-02-2022.pdf",
                caseData.getDocumentWitnessStatementApp2().get(0).getValue().getWitnessOptionDocument().getDocumentFileName());
    }
}