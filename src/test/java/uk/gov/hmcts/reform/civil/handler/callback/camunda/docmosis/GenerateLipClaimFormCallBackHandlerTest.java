package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.draft.ClaimFormGenerator;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DRAFT_CLAIM_FORM;

@ExtendWith(MockitoExtension.class)
class GenerateLipClaimFormCallBackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ClaimFormGenerator claimFormGenerator;
    @Mock
    private SystemGeneratedDocumentService systemGeneratedDocumentService;
    @Mock
    private ObjectMapper mapper;
    @InjectMocks
    private GenerateLipClaimFormCallBackHandler handler;

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
        //  given(claimFormGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder().build();
        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        //  verify(claimFormGenerator).generate(caseData, BEARER_TOKEN);
    }

    private CaseDocument generateForm(DocumentType documentType) {
      return CaseDocument.builder()
            .createdBy("John")
            .documentName("document name")
            .documentSize(0L)
            .documentType(documentType)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();
    }

}
