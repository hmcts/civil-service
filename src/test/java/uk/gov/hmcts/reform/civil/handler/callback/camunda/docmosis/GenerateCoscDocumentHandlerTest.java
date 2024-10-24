package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.cosc.CertificateOfDebtGenerator;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_HEARING_FORM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_FORM;

@ExtendWith(MockitoExtension.class)
class GenerateCoscDocumentHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper mapper;
    @InjectMocks
    private GenerateCoscDocumentHandler handler;
    @Mock
    private CertificateOfDebtGenerator certificateOfDebtGenerator;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new GenerateCoscDocumentHandler(certificateOfDebtGenerator, mapper);
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldGenerate_cosc_doc() {
        CaseDocument document = CaseDocument.builder()
            .createdBy("John")
            .documentName("document name")
            .documentSize(0L)
            .documentType(HEARING_FORM)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

        when(certificateOfDebtGenerator.generateDoc(any(CaseData.class), anyString())).thenReturn(document);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudgmentOnlineCaseWithMarkJudgementPaidWithin31DaysForCosc().toBuilder()
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_FORM.name());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(certificateOfDebtGenerator).generateDoc(any(CaseData.class), eq("BEARER_TOKEN"));
        assertThat(response.getData())
            .extracting("coSCApplicationStatus")
            .isEqualTo("PROCESSED");

    }

}
