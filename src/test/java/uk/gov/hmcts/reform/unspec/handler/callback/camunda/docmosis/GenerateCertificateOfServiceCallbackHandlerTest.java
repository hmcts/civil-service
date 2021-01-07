package uk.gov.hmcts.reform.unspec.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.documents.Document;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.docmosis.cos.CertificateOfServiceGenerator;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.model.documents.DocumentType.CERTIFICATE_OF_SERVICE;
import static uk.gov.hmcts.reform.unspec.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.unspec.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateCertificateOfServiceCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class GenerateCertificateOfServiceCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final CaseDocument DOCUMENT = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(CERTIFICATE_OF_SERVICE)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
            .documentUrl("fake-url")
            .documentFileName("file-name")
            .documentBinaryUrl("binary-url")
            .build())
        .build();

    @MockBean
    private CertificateOfServiceGenerator certificateOfServiceGenerator;

    @Autowired
    private GenerateCertificateOfServiceCallbackHandler handler;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        when(certificateOfServiceGenerator.generate(any(CaseData.class), anyString())).thenReturn(DOCUMENT);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldAddDocumentToSystemGeneratedDocuments_whenNoSystemGeneratedDocuments() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(certificateOfServiceGenerator).generate(caseData, "BEARER_TOKEN");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(DOCUMENT);
        }

        @Test
        void shouldAddDocumentToSystemGeneratedDocuments_whenSystemGeneratedDocuments() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build()))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(certificateOfServiceGenerator).generate(caseData, "BEARER_TOKEN");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments()).hasSize(2);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue()).isEqualTo(DOCUMENT);
        }
    }
}
