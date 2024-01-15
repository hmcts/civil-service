package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.draft.DraftClaimFormGenerator;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DRAFT_CLAIM_FORM;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateDraftClaimFormCallBackHandler.class,
    JacksonAutoConfiguration.class
})
class GenerateDraftClaimFormCallBackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private DraftClaimFormGenerator draftClaimFormGenerator;
    @MockBean
    private SystemGeneratedDocumentService systemGeneratedDocumentService;
    @Autowired
    private GenerateDraftClaimFormCallBackHandler handler;
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    private static final CaseDocument FORM = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(DRAFT_CLAIM_FORM)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    @Test
    void shouldGenerateDraftClaimForm() {
        //Given
        given(draftClaimFormGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder().build();
        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(draftClaimFormGenerator).generate(caseData, BEARER_TOKEN);
    }

}
