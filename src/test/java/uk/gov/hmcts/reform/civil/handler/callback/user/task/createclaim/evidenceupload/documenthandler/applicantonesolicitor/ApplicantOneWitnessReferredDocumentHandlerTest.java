package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicantonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneWitnessReferredDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.APPLICANT;

@ExtendWith(MockitoExtension.class)
public class ApplicantOneWitnessReferredDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantOneWitnessReferredDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentReferredInStatement();
    }

    @Test
    void shouldCopyWitnessReferredDocumentsToLegalRep2() {
        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(2, builder.build().getDocumentReferredInStatementApp2().size());
    }

    @Test
    void shouldRenameDocuments() {
        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, APPLICANT, notificationBuilder);

        String expectedDocumentName = "typeOfDocument referred to in the statement of witnessName 10-02-2022.pdf";
        assertEquals(expectedDocumentName, caseData.getDocumentReferredInStatement().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}
