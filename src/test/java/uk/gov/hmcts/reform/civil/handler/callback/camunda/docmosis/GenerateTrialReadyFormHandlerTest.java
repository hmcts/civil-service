package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;

import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.trialready.TrialReadyFormGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_TRIAL_READY_FORM_APPLICANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_TRIAL_READY_FORM_RESPONDENT1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_TRIAL_READY_FORM_RESPONDENT2;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.TRIAL_READY_DOCUMENT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateTrialReadyFormHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
})
public class GenerateTrialReadyFormHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private GenerateTrialReadyFormHandler handler;
    @MockBean
    private TrialReadyFormGenerator trialReadyFormGenerator;

    @Test
    public void shouldGenerateForm_when1v1() {
        // Given
        CaseDocument document = CaseDocument.builder()
            .createdBy("John")
            .documentName("document name")
            .documentSize(0L)
            .documentType(TRIAL_READY_DOCUMENT)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

        when(trialReadyFormGenerator.generate(any(CaseData.class), anyString(), anyString())).thenReturn(document);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_TRIAL_READY_FORM_APPLICANT.name());
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        verify(trialReadyFormGenerator).generate(
            any(CaseData.class), eq("BEARER_TOKEN"), eq("GenerateTrialReadyFormApplicant"));

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getTrialReadyDocuments().size()).isEqualTo(1);

    }

    @Test
    public void shouldGenerateForm_when1v1AndRespondent1() {
        // Given
        CaseDocument document = CaseDocument.builder()
            .createdBy("John")
            .documentName("document name")
            .documentSize(0L)
            .documentType(TRIAL_READY_DOCUMENT)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

        when(trialReadyFormGenerator.generate(any(CaseData.class), anyString(), anyString())).thenReturn(document);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_TRIAL_READY_FORM_RESPONDENT1.name());
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        verify(trialReadyFormGenerator).generate(
            any(CaseData.class), eq("BEARER_TOKEN"), eq("GenerateTrialReadyFormRespondent1"));

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getTrialReadyDocuments().size()).isEqualTo(1);

    }

    @Test
    public void shouldGenerateForm_when1v2AndOneSeveralPartiesGeneratesDocument() {
        // Given
        CaseDocument document = CaseDocument.builder()
            .createdBy("John")
            .documentName("document name")
            .documentSize(0L)
            .documentType(TRIAL_READY_DOCUMENT)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        systemGeneratedCaseDocuments.add(element(document));

        when(trialReadyFormGenerator.generate(any(CaseData.class), anyString(), anyString())).thenReturn(document);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .trialReadyDocuments(systemGeneratedCaseDocuments).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_TRIAL_READY_FORM_RESPONDENT2.name());
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        verify(trialReadyFormGenerator).generate(
            any(CaseData.class), eq("BEARER_TOKEN"), eq("GenerateTrialReadyFormRespondent2"));

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getTrialReadyDocuments().size()).isEqualTo(2);

    }
}
