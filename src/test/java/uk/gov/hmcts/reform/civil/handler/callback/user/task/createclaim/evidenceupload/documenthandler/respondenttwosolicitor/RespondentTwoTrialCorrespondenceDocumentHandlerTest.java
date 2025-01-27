package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondenttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor.RespondentTwoTrialCorrespondenceDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class RespondentTwoTrialCorrespondenceDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private RespondentTwoTrialCorrespondenceDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentEvidenceForTrial();
    }

    @Test
    void shouldNotRenameDocuments() {
        handler.handleDocuments(caseData, RESPONDENT, new StringBuilder());

        assertEquals("Documentary Evidence typeOfDocument 10-02-2022.pdf",
                caseData.getDocumentEvidenceForTrialRes2().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}