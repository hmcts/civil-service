package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondentonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor.RespondentOneScheduleOfCostsDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RespondentOneScheduleOfCostsDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private RespondentOneScheduleOfCostsDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentCosts();
    }

    @Test
    void shouldCopyScheduleOfCostsDocumentsToLegalRep2() {
        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(2, builder.build().getDocumentCostsRes2().size());
    }

    @Test
    void shouldNotRenameDocuments() {
        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, "Respondent", notificationBuilder);

        assertEquals("OriginalName.pdf", caseData.getDocumentCostsRes().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}