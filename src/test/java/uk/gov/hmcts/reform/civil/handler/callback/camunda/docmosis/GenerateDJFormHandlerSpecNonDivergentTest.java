package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.nondivergent.NonDivergentSpecDefaultJudgmentFormGenerator;

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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_CLAIMANT1;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_CLAIMANT2;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class GenerateDJFormHandlerSpecNonDivergentTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper mapper;
    @InjectMocks
    private GenerateDJFormHandlerSpecNonDivergent handler;
    @Mock
    private NonDivergentSpecDefaultJudgmentFormGenerator defaultJudgmentFormGenerator;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new GenerateDJFormHandlerSpecNonDivergent(defaultJudgmentFormGenerator, mapper);
        mapper.registerModule(new JavaTimeModule());
    }

    @Nested
    class AboutToSubmitCallback {

        CaseDocument documentClaimant = CaseDocument.builder()
            .createdBy("John")
            .documentName("document name")
            .documentSize(0L)
            .documentType(DEFAULT_JUDGMENT_CLAIMANT1)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();
        CaseDocument documentClaimant2 = CaseDocument.builder()
            .createdBy("John")
            .documentName("document name")
            .documentSize(0L)
            .documentType(DEFAULT_JUDGMENT_CLAIMANT2)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();
        CaseDocument documentDefendant = CaseDocument.builder()
            .createdBy("John")
            .documentName("document name")
            .documentSize(0L)
            .documentType(DEFAULT_JUDGMENT_DEFENDANT1)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();
        CaseDocument documentDefendant2 = CaseDocument.builder()
            .createdBy("John")
            .documentName("document name")
            .documentSize(0L)
            .documentType(DEFAULT_JUDGMENT_DEFENDANT2)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

        @Test
        void shouldGenerateClaimantForm_when1v1ClaimantEvent() {
            List<CaseDocument> documents = new ArrayList<>();
            documents.add(documentClaimant);
            when(defaultJudgmentFormGenerator.generateNonDivergentDocs(any(CaseData.class), anyString(),
                                                       eq(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name()))).thenReturn(documents);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetails(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("Both")
                                                 .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(defaultJudgmentFormGenerator).generateNonDivergentDocs(any(CaseData.class), eq("BEARER_TOKEN"),
                                                          eq(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name()));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments()).hasSize(1);
        }

        @Test
        void shouldGenerateDefendantForm_when1v1DefendantEvent() {
            List<CaseDocument> documents = new ArrayList<>();
            documents.add(documentClaimant);
            when(defaultJudgmentFormGenerator.generateNonDivergentDocs(any(CaseData.class), anyString(),
                                                                       eq(GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name()))).thenReturn(documents);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetails(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("Both")
                                                 .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(defaultJudgmentFormGenerator).generateNonDivergentDocs(any(CaseData.class), eq("BEARER_TOKEN"),
                                                                          eq(GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name()));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments()).hasSize(1);
        }

        @Test
        void shouldGenerateTwoDefendantForm_when1v2DefendantEvent() {
            List<CaseDocument> documents = new ArrayList<>();
            documents.add(documentDefendant);
            documents.add(documentDefendant2);
            when(defaultJudgmentFormGenerator.generateNonDivergentDocs(any(CaseData.class), anyString(),
                                                                       eq(GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name()))).thenReturn(documents);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .defendantDetails(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("Both")
                                                 .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(defaultJudgmentFormGenerator).generateNonDivergentDocs(any(CaseData.class), eq("BEARER_TOKEN"),
                                                                          eq(GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name()));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments()).hasSize(2);
        }

        @Test
        void shouldGenerateTwoApplicantForm_when2v1ClaimantEvent() {
            List<CaseDocument> documents = new ArrayList<>();
            documents.add(documentClaimant);
            documents.add(documentClaimant2);
            when(defaultJudgmentFormGenerator.generateNonDivergentDocs(any(CaseData.class), anyString(),
                                                                       eq(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name()))).thenReturn(documents);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .applicant2(PartyBuilder.builder().individual().build())
                .addApplicant2(YES)
                .defendantDetails(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("Both")
                                                 .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(defaultJudgmentFormGenerator).generateNonDivergentDocs(any(CaseData.class), eq("BEARER_TOKEN"),
                                                                          eq(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name()));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments()).hasSize(2);
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(
            GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT,
            GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT);
    }

    @Test
    void shouldReturnClaimantCamundaTask_whenSpecEvent() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT").build()).build())).isEqualTo("GenerateDJFormNondivergentSpecClaimant");
    }

    @Test
    void shouldReturnDefendantCamundaTask_whenUnSpecEvent() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT").build()).build())).isEqualTo("GenerateDJFormNondivergentSpecDefendant");
    }

}
