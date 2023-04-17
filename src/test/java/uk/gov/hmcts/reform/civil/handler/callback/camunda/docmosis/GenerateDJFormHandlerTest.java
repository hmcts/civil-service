package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateDJFormHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
    AssignCategoryId.class
})
public class GenerateDJFormHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private GenerateDJFormHandler handler;
    @Autowired
    private AssignCategoryId assignCategoryId;
    @MockBean
    private DefaultJudgmentFormGenerator defaultJudgmentFormGenerator;
    @MockBean
    private FeatureToggleService featureToggleService;

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
        public void shouldGenerateTwoForm_when1v2() {
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
            assertThat(updatedData.getDefaultJudgmentDocuments().size()).isEqualTo(2);
        }

        @Test
        public void shouldNotGenerateTwoForm_when1v2And1DefSelectedSpecified() {
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
            assertThat(updatedData.getDefaultJudgmentDocuments().size()).isZero();

        }

        @Test
        public void shouldNotGenerateOneForm_when1v1Specified() {
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
            assertThat(updatedData.getDefaultJudgmentDocuments().size()).isEqualTo(1);
        }

        @Test
        public void shouldGenerateTwoForm_when1v2Specified() {
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
            assertThat(updatedData.getDefaultJudgmentDocuments().size()).isEqualTo(2);
        }

        @Test
        public void shouldNotGenerateTwoForm_when1v2And1DefSelected() {
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
            assertThat(updatedData.getDefaultJudgmentDocuments().size()).isZero();

        }

        @Test
        public void shouldNotGenerateOneForm_when1v1() {
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
            assertThat(updatedData.getDefaultJudgmentDocuments().size()).isEqualTo(1);

        }

        @Test
        public void shouldAssignCategoryId_whenInvoked() {
            //Given
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
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
