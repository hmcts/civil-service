package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondentonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor.RespondentOneWitnessHearsayDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class RespondentOneWitnessHearsayDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private RespondentOneWitnessHearsayDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentHearsayNotice();
    }

    @Test
    void shouldCopyWitnessHearsayDocumentsToLegalRep2() {
        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(2, builder.build().getDocumentHearsayNoticeRes2().size());
    }

    @Test
    void shouldRenameDocuments() {
        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, RESPONDENT, notificationBuilder);

        assertEquals("Hearsay evidence WitnessHearsayBundle 10-02-2022.pdf",
                caseData.getDocumentHearsayNoticeRes().get(0).getValue().getWitnessOptionDocument().getDocumentFileName());
    }
}