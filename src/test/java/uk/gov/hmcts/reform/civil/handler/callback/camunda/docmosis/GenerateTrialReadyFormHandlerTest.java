package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.trialready.TrialReadyFormGenerator;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_TRIAL_READY_FORM_APPLICANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_TRIAL_READY_FORM_RESPONDENT1;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.TRIAL_READY_DOCUMENT;

@ExtendWith(MockitoExtension.class)
class GenerateTrialReadyFormHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper mapper;
    @InjectMocks
    private GenerateTrialReadyFormHandler handler;
    @Mock
    private TrialReadyFormGenerator trialReadyFormGenerator;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new GenerateTrialReadyFormHandler(trialReadyFormGenerator, featureToggleService, mapper);
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldGenerateForm_when1v1() {
        // Given
        Document documentLink = new Document();
        documentLink.setDocumentUrl("fake-url");
        documentLink.setDocumentFileName("file-name");
        documentLink.setDocumentBinaryUrl("binary-url");
        CaseDocument document = new CaseDocument();
        document.setCreatedBy("John");
        document.setDocumentName("document name");
        document.setDocumentSize(0L);
        document.setDocumentType(TRIAL_READY_DOCUMENT);
        document.setCreatedDatetime(LocalDateTime.now());
        document.setDocumentLink(documentLink);

        when(trialReadyFormGenerator.generate(any(CaseData.class), anyString(), anyString(), any(CaseRole.class)))
            .thenReturn(document);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_TRIAL_READY_FORM_APPLICANT.name());
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        verify(trialReadyFormGenerator).generate(
            any(CaseData.class), eq("BEARER_TOKEN"), eq("GenerateTrialReadyFormApplicant"),
            eq(CaseRole.CLAIMANT));

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getTrialReadyDocuments()).hasSize(1);

    }

    @Test
    void shouldGenerateForm_when1v1AndRespondent1() {
        // Given
        Document documentLink = new Document();
        documentLink.setDocumentUrl("fake-url");
        documentLink.setDocumentFileName("file-name");
        documentLink.setDocumentBinaryUrl("binary-url");
        CaseDocument document = new CaseDocument();
        document.setCreatedBy("John");
        document.setDocumentName("document name");
        document.setDocumentSize(0L);
        document.setDocumentType(TRIAL_READY_DOCUMENT);
        document.setCreatedDatetime(LocalDateTime.now());
        document.setDocumentLink(documentLink);

        when(trialReadyFormGenerator.generate(any(CaseData.class), anyString(), anyString(), any(CaseRole.class)))
            .thenReturn(document);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_TRIAL_READY_FORM_RESPONDENT1.name());
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        verify(trialReadyFormGenerator).generate(
            any(CaseData.class), eq("BEARER_TOKEN"), eq("GenerateTrialReadyFormRespondent1"),
            eq(CaseRole.DEFENDANT));

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getTrialReadyDocuments()).hasSize(1);

    }
}
