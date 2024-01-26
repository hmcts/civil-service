package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.RequestReconsiderationGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_CLAIM_FORM_RECONSIDERATION;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateClaimFormForReconsiderationCallbackHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
    AssignCategoryId.class
})
class GenerateClaimFormForReconsiderationCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private GenerateClaimFormForReconsiderationCallbackHandler handler;
    @Autowired
    private AssignCategoryId assignCategoryId;
    @MockBean
    private Time time;
    @MockBean
    private RequestReconsiderationGeneratorService requestReconsiderationGeneratorService;
    @MockBean
    private FeatureToggleService featureToggleService;
    private static final CaseDocument document = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(SDO_ORDER)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    private final LocalDate issueDate = now();
    private CaseData caseData;

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
            caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued().build().toBuilder()
                .specRespondent1Represented(YES)
                .systemGeneratedCaseDocuments(systemGeneratedCaseDocuments)
                .specResponseTimelineDocumentFiles(ResponseDocument.builder()
                                                       .file(Document.builder()
                                                                 .documentUrl("fake-url")
                                                                 .documentFileName("file-name")
                                                                 .documentBinaryUrl("binary-url")
                                                                 .build()).build())
                .respondent1SpecDefenceResponseDocument(ResponseDocument.builder().file(Document.builder()
                                                                                            .documentUrl("fake-url")
                                                                                            .documentFileName("file-name")
                                                                                            .documentBinaryUrl("binary-url")
                                                                                            .build()).build())
                .build();
            when(time.now()).thenReturn(issueDate.atStartOfDay());
        }

        @Test
        void shouldGenerateForm() {

            when(requestReconsiderationGeneratorService.generate(any(CaseData.class), anyString())).thenReturn(document);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().setEventId(GENERATE_CLAIM_FORM_RECONSIDERATION.name());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(requestReconsiderationGeneratorService).generate(any(CaseData.class), eq("BEARER_TOKEN"));
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().size()).isEqualTo(1);
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(GENERATE_CLAIM_FORM_RECONSIDERATION);
    }

    @Test
    void shouldReturnSpecCamundaTask_whenSpecEvent() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "GENERATE_CLAIM_FORM_RECONSIDERATION").build()).build())).isEqualTo("GenerateClaimFormForRecon");
    }

}
