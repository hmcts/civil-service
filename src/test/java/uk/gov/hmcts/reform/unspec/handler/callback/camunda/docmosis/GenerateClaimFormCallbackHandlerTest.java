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
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.unspec.service.Time;
import uk.gov.hmcts.reform.unspec.service.docmosis.sealedclaim.SealedClaimFormGenerator;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.LocalDate.now;
import static java.time.LocalTime.MIDNIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.unspec.model.documents.DocumentType.SEALED_CLAIM;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateClaimFormCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class
})
class GenerateClaimFormCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private SealedClaimFormGenerator sealedClaimFormGenerator;

    @Autowired
    private GenerateClaimFormCallbackHandler handler;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    public static final CaseDocument DOCUMENT = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(SEALED_CLAIM)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    private final LocalDate issueDate = now();
    private final LocalDateTime deadline = now().atTime(MIDNIGHT);

    @BeforeEach
    void setup() {
        when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(DOCUMENT);
        when(deadlinesCalculator.addMonthsToDateAtMidnight(eq(4), any(LocalDate.class)))
            .thenReturn(deadline);
        when(time.now()).thenReturn(issueDate.atStartOfDay());
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldGenerateDocumentAndSetStateAsCaseIssued_whenRespondentIsRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseNotification()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(DOCUMENT);
            assertThat(updatedData.getIssueDate()).isEqualTo(issueDate);
            assertThat(updatedData.getClaimNotificationDeadline()).isEqualTo(deadline);
            assertThat(response.getState()).isEqualTo(CASE_ISSUED.toString());
        }

        @Test
        void shouldGenerateDocumentAndSetStateAsProceedsInHeritageSystem_whenRespondentIsNotRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendant().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(DOCUMENT);
            assertThat(response.getState()).isEqualTo(PROCEEDS_IN_HERITAGE_SYSTEM.toString());
        }

        @Test
        void shouldGenerateDocumentAndSetStateAsProceedsInHeritageSystem_whenRespondentSolicitorUnregistered() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseNotification()
                .respondent1OrgRegistered(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(DOCUMENT);
            assertThat(response.getState()).isEqualTo(PROCEEDS_IN_HERITAGE_SYSTEM.toString());
        }
    }
}
