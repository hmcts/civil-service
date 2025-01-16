package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondentonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor.RespondentOneWitnessStatementDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.RESPONDENT;

@ExtendWith(MockitoExtension.class)
public class RespondentOneWitnessStatementDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private RespondentOneWitnessStatementDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentWitnessStatement();
    }

    @Test
    void shouldCopyWitnessStatementDocumentsToLegalRep2() {
        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(2, builder.build().getDocumentWitnessStatementRes2().size());
    }

    @Test
    void shouldRenameDocuments() {
        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, RESPONDENT, notificationBuilder);

        assertEquals("Witness Statement of witnessName 10-02-2022.pdf",
                caseData.getDocumentWitnessStatementRes().get(0).getValue().getWitnessOptionDocument().getDocumentFileName());
    }
}