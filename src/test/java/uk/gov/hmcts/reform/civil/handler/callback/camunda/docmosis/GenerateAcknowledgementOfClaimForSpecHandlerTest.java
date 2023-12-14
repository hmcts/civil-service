package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.aos.AcknowledgementOfClaimGeneratorForSpec;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_ACKNOWLEDGEMENT_OF_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.ACKNOWLEDGEMENT_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    GenerateAcknowledgementOfClaimForSpecCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class GenerateAcknowledgementOfClaimForSpecHandlerTest extends BaseCallbackHandlerTest {

    public static final CaseDocument DOCUMENT = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(ACKNOWLEDGEMENT_OF_CLAIM)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();
    @Autowired
    private GenerateAcknowledgementOfClaimForSpecCallbackHandler handler;

    @MockBean
    private AcknowledgementOfClaimGeneratorForSpec acknowledgementOfClaimGenerator;
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();

        // When
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // Then
        assertThat(handler.camundaActivityId(params)).isEqualTo("AcknowledgeClaimGenerateAcknowledgementOfClaimForSpec");
    }

    @Test
    void shouldAddDocumentToSystemGeneratedDocuments_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build()))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        when(acknowledgementOfClaimGenerator.generate(any(CaseData.class), anyString())).thenReturn(DOCUMENT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(acknowledgementOfClaimGenerator).generate(caseData, "BEARER_TOKEN");

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedData.getSystemGeneratedCaseDocuments()).hasSize(2);
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue()).isEqualTo(DOCUMENT);
    }

    @Test
    void testHandledEvents() {
        assertThat(handler.handledEvents()).contains(GENERATE_ACKNOWLEDGEMENT_OF_CLAIM_SPEC);
    }
}
