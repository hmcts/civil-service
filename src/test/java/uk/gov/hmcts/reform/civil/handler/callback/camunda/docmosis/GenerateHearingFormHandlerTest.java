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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.hearing.HearingFormGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_HEARING_FORM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_FORM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class GenerateHearingFormHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper mapper;
    @InjectMocks
    private GenerateHearingFormHandler handler;
    @Mock
    private HearingFormGenerator hearingFormGenerator;
    @Mock
    private FeatureToggleService featureToggleService;
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new GenerateHearingFormHandler(hearingFormGenerator, mapper, featureToggleService);
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldGenerateForm_when1v1() {
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

        List<CaseDocument> documents = new ArrayList<>();
        documents.add(document);
        when(hearingFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(documents);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(YES)
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_FORM.name());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(hearingFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments()).hasSize(1);
    }

    @Test
    void shouldGenerateForm_when1v1ButHideIt_IfClaimantIsWelsh() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
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

        List<CaseDocument> documents = new ArrayList<>();
        documents.add(document);
        when(hearingFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(documents);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .information("test")
            .claimantBilingualLanguagePreference("WELSH")
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_FORM.name());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(hearingFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments()).hasSize(0);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
    }

    @Test
    void shouldGenerate2Forms_whenListHave1PreviousForm() {
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

        List<CaseDocument> documents = new ArrayList<>();
        documents.add(document);
        when(hearingFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(documents);

        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        systemGeneratedCaseDocuments.add(element(document));
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(YES)
            .hearingDocuments(systemGeneratedCaseDocuments)
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_FORM.name());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(hearingFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments()).hasSize(2);
    }
}
