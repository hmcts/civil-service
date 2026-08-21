package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Spy;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.scanneddocument.ScannedDocument;
import uk.gov.hmcts.reform.civil.model.scanneddocument.ScannedDocumentType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AttachScannedDocsCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);

    private AttachScannedDocsCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AttachScannedDocsCallbackHandler(caseDetailsConverter);
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
                .id(UUID.randomUUID().toString())
                .documentType(ScannedDocumentType.FORM)
                .subtype(subtype)
                .build();

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(1234567890L)
                .scannedDocuments(List.of(new Element<>(UUID.randomUUID(), scannedDocument)))
                .build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            params.isCivilCaseType(true);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("respondent1ResponseMethod")).isEqualTo("OFFLINE");
        }

        @Test
        void shouldNotSetCivilRespondentFieldForNonCivilCaseType() {
            String subtype = "N9a";
            ScannedDocument scannedDocument = ScannedDocument.builder()
                .id(UUID.randomUUID().toString())
                .documentType(ScannedDocumentType.FORM)
                .subtype(subtype)
                .build();

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(1234567890L)
                .scannedDocuments(List.of(new Element<>(UUID.randomUUID(), scannedDocument)))
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("respondent1ResponseMethod")).isNull();
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
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT, CaseState.CASE_ISSUED);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("respondent1ResponseMethod")).isNull();
        }

        @Test
        void shouldSetOfflineWhenPaperResponseDetectedFromRawFormSubtype() {
            ScannedDocument scannedDocument = ScannedDocument.builder()
                .documentType(ScannedDocumentType.FORM)
                .subtype("N9a")
                .formSubtype("N9b")
                .build();
            CaseData caseData = CaseData.builder()
                .ccdCaseReference(1234567890L)
                .scannedDocuments(List.of(new Element<>(UUID.randomUUID(), scannedDocument)))
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            params.isCivilCaseType(true);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("respondent1ResponseMethod")).isEqualTo("OFFLINE");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "PAPER_RESPONSE_FULL_ADMIT"
        })
        void shouldSetOfflineWhenPaperResponseDetectedFromStaffUploadedDocuments(ScannedDocumentType documentType) {
            ScannedDocument scannedDocument = ScannedDocument.builder()
                .id(UUID.randomUUID().toString())
                .documentType(documentType)
                .build();

            CaseData caseData = CaseData.builder()
                .ccdCaseReference(1234567890L)
                .scannedDocuments(List.of(new Element<>(UUID.randomUUID(), scannedDocument)))
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            params.isCivilCaseType(true);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("respondent1ResponseMethod")).isEqualTo("OFFLINE");
        }

        @Test
        void shouldReturnEmptyDataWhenRequestIsNullAndCaseDataHasNoScannedDocuments() {
            CallbackParams params = callbackParamsOf(
                new HashMap<>(),
                CaseData.builder().ccdCaseReference(1234567890L).build(),
                CallbackType.ABOUT_TO_SUBMIT,
                CaseState.CASE_ISSUED
            );
            CallbackRequest request = null;
            params.request(request);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).isEmpty();
        }
    }
}
