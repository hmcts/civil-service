package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentFormGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import org.junit.jupiter.api.BeforeEach;

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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DJ_FORM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DJ_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT;

@ExtendWith(MockitoExtension.class)
class GenerateDJFormHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper mapper;
    @InjectMocks
    private GenerateDJFormHandler handler;
    @InjectMocks
    private AssignCategoryId assignCategoryId;
    @Mock
    private DefaultJudgmentFormGenerator defaultJudgmentFormGenerator;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new GenerateDJFormHandler(assignCategoryId, defaultJudgmentFormGenerator, mapper);
        mapper.registerModule(new JavaTimeModule());
    }

    @Nested
    class AboutToSubmitCallback {

        CaseDocument document = CaseDocument.builder()
            .createdBy("John")
            .documentName("document name")
            .documentSize(0L)
            .documentType(DEFAULT_JUDGMENT)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

        @Test
        void shouldGenerateTwoForm_when1v2() {
            List<CaseDocument> documents = new ArrayList<>();
            documents.add(document);
            documents.add(document);
            when(defaultJudgmentFormGenerator.generate(any(CaseData.class), anyString(),
                                                       eq(GENERATE_DJ_FORM.name()))).thenReturn(documents);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetails(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("Both")
                                                 .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GENERATE_DJ_FORM.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(defaultJudgmentFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"),
                                                          eq(GENERATE_DJ_FORM.name()));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments()).hasSize(2);
        }

        @Test
        void shouldNotGenerateTwoForm_when1v2And1DefSelectedSpecified() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("One")
                                                 .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GENERATE_DJ_FORM.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments()).isEmpty();

        }

        @Test
        void shouldNotGenerateOneForm_when1v1Specified() {
            List<CaseDocument> documents = new ArrayList<>();
            documents.add(document);
            when(defaultJudgmentFormGenerator.generate(any(CaseData.class), anyString(),
                                                       eq(GENERATE_DJ_FORM.name()))).thenReturn(documents);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addRespondent2(NO)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("One")
                                                 .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GENERATE_DJ_FORM.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(defaultJudgmentFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"),
                                                          eq(GENERATE_DJ_FORM.name()));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments()).hasSize(1);
        }

        @Test
        void shouldNotGenerateOneForm_whenLRvLiPSpecified() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .specClaim1v1LrVsLip().build().toBuilder()
                .addRespondent2(NO)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("One")
                                                     .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GENERATE_DJ_FORM_SPEC.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments()).isEmpty();
        }

        @Test
        void shouldNotGenerateOneForm_whenLipvLRSpecified() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .specClaim1v1LipvLr().build().toBuilder()
                .addRespondent2(NO)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("One")
                                                     .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GENERATE_DJ_FORM_SPEC.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments()).isEmpty();
        }

        @Test
        void shouldGenerateTwoForm_when1v2Specified() {
            List<CaseDocument> documents = new ArrayList<>();
            documents.add(document);
            documents.add(document);
            when(defaultJudgmentFormGenerator.generate(any(CaseData.class), anyString(),
                                                       eq(GENERATE_DJ_FORM.name()))).thenReturn(documents);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("Both")
                                                 .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GENERATE_DJ_FORM.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(defaultJudgmentFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"),
                                                          eq(GENERATE_DJ_FORM.name()));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments()).hasSize(2);
        }

        @Test
        void shouldNotGenerateTwoForm_when1v2And1DefSelected() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetails(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("One")
                                                 .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GENERATE_DJ_FORM.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments()).isEmpty();

        }

        @Test
        void shouldNotGenerateOneForm_when1v1() {
            List<CaseDocument> documents = new ArrayList<>();
            documents.add(document);
            when(defaultJudgmentFormGenerator.generate(any(CaseData.class), anyString(),
                                                       eq(GENERATE_DJ_FORM.name()))).thenReturn(documents);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addRespondent2(NO)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetails(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("One")
                                                 .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GENERATE_DJ_FORM.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(defaultJudgmentFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"),
                                                          eq(GENERATE_DJ_FORM.name()));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments()).hasSize(1);

        }

        @Test
        void shouldAssignCategoryId_whenInvoked() {
            //Given
            List<CaseDocument> documents = new ArrayList<>();
            documents.add(document);
            when(defaultJudgmentFormGenerator.generate(any(CaseData.class), anyString(),
                                                       eq(GENERATE_DJ_FORM.name()))).thenReturn(documents);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("One")
                                                     .build()).build())
                .build();
            //When
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GENERATE_DJ_FORM.name());
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments().get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("detailsOfClaim");
        }

    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(GENERATE_DJ_FORM, GENERATE_DJ_FORM_SPEC);
    }

    @Test
    void shouldReturnSpecCamundaTask_whenSpecEvent() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "GENERATE_DJ_FORM_SPEC").build()).build())).isEqualTo("GenerateDJFormSpec");
    }

    @Test
    void shouldReturnUnSpecCamundaTask_whenUnSpecEvent() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "GENERATE_DJ_FORM").build()).build())).isEqualTo("GenerateDJForm");
    }

}
