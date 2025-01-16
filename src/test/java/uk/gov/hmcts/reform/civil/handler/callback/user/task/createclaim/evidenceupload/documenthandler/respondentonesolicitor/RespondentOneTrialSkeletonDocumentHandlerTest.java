package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondentonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor.RespondentOneTrialSkeletonDocumentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.ORIGINAL_FILE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.RESPONDENT;

@ExtendWith(MockitoExtension.class)
public class RespondentOneTrialSkeletonDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private RespondentOneTrialSkeletonDocumentHandler handler;

    @BeforeEach
    void setUp() {
        setUpDocumentSkeletonArgument();
    }

    @Test
    void shouldCopyTrialSkeletonDocumentsToLegalRep2() {
        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(2, builder.build().getDocumentSkeletonArgumentRes2().size());
    }

    @Test
    void shouldNotRenameDocuments() {
        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, RESPONDENT, notificationBuilder);

        assertEquals(ORIGINAL_FILE_NAME, caseData.getDocumentSkeletonArgumentRes().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}