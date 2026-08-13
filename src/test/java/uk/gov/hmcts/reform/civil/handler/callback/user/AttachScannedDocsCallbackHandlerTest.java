package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.scanneddocument.ScannedDocument;
import uk.gov.hmcts.reform.civil.model.scanneddocument.ScannedDocumentType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AttachScannedDocsCallbackHandlerTest extends BaseCallbackHandlerTest {

    private AttachScannedDocsCallbackHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new AttachScannedDocsCallbackHandler(objectMapper);
    }

    @Test
    void shouldReturnCorrectHandledEvents() {
        assertThat(handler.handledEvents()).containsExactly(uk.gov.hmcts.reform.civil.callback.CaseEvent.ATTACH_SCANNED_DOCS);
    }

    @Nested
    class AboutToSubmitCallback {

        @ParameterizedTest
        @ValueSource(strings = {"N9a", "N9b", "N11", "N225", "N180", "n9a", "N9A"})
        void shouldSetDefendantResponseMethodToOfflineWhenPaperResponseScannedSubtype(String subtype) {
            ScannedDocument scannedDocument = ScannedDocument.builder()
                .documentType(ScannedDocumentType.FORM)
                .subtype(subtype)
                .build();

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(1234567890L)
                .scannedDocuments(List.of(new Element<>(UUID.randomUUID(), scannedDocument)))
                .build();

            Map<String, Object> data = new HashMap<>();
            data.put("scannedDocuments", List.of(
                Map.of("id", "1", "value", Map.of("subtype", subtype, "type", "FORM"))
            ));

            CallbackParams params = callbackParamsOf(data, caseData, CallbackType.ABOUT_TO_SUBMIT, CaseState.CASE_ISSUED);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("respondent1ResponseMethod")).isEqualTo("OFFLINE");
        }

        @Test
        void shouldSetRespondentInRespondentsListToOfflineWhenCMCStyleRespondentsExist() {
            Map<String, Object> respondentValue = new HashMap<>();
            respondentValue.put("responseMethod", "DIGITAL");
            Map<String, Object> respondentElement = new HashMap<>();
            respondentElement.put("id", "1");
            respondentElement.put("value", respondentValue);

            List<Map<String, Object>> respondents = new ArrayList<>();
            respondents.add(respondentElement);

            Map<String, Object> data = new HashMap<>();
            data.put("respondents", respondents);
            data.put("scannedDocuments", List.of(
                Map.of("id", "1", "value", Map.of("subtype", "N9a"))
            ));

            CaseData caseData = CaseData.builder().ccdCaseReference(1234567890L).build();
            CallbackParams params = callbackParamsOf(data, caseData, CallbackType.ABOUT_TO_SUBMIT, CaseState.CASE_ISSUED);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> updatedRespondents = (List<Map<String, Object>>) response.getData().get("respondents");
            assertThat(updatedRespondents).isNotNull().isNotEmpty();

            @SuppressWarnings("unchecked")
            Map<String, Object> firstValue = (Map<String, Object>) updatedRespondents.get(0).get("value");
            assertThat(firstValue.get("responseMethod")).isEqualTo("OFFLINE");
        }

        @Test
        void shouldNotSetOfflineWhenNoPaperResponseDocument() {
            ScannedDocument scannedDocument = ScannedDocument.builder()
                .documentType(ScannedDocumentType.OTHER)
                .subtype("other_doc")
                .build();

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(1234567890L)
                .scannedDocuments(List.of(new Element<>(UUID.randomUUID(), scannedDocument)))
                .build();

            Map<String, Object> data = new HashMap<>();
            data.put("scannedDocuments", List.of(
                Map.of("id", "1", "value", Map.of("subtype", "other_doc"))
            ));

            CallbackParams params = callbackParamsOf(data, caseData, CallbackType.ABOUT_TO_SUBMIT, CaseState.CASE_ISSUED);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("respondent1ResponseMethod")).isNull();
        }
    }
}
