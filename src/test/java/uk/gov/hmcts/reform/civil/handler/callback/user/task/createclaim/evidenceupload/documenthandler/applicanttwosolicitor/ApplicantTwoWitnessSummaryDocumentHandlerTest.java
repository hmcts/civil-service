package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicanttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor.ApplicantTwoWitnessSummaryDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.APPLICANT;

@ExtendWith(MockitoExtension.class)
public class ApplicantTwoWitnessSummaryDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantTwoWitnessSummaryDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentWitnessSummary();
    }

    @Test
    void shouldNotRenameDocuments() {
        handler.handleDocuments(caseData, APPLICANT, new StringBuilder());

        assertEquals("Witness Summary of witnessName 10-02-2022.pdf", caseData.getDocumentWitnessSummaryApp2().get(0).getValue().getWitnessOptionDocument().getDocumentFileName());
    }
}