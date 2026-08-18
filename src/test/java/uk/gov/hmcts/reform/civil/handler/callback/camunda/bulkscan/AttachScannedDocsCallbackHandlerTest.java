package uk.gov.hmcts.reform.civil.handler.callback.camunda.bulkscan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ScannedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AttachScannedDocsCallbackHandlerTest extends BaseCallbackHandlerTest {

    private AttachScannedDocsCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AttachScannedDocsCallbackHandler(
            new ObjectMapper().registerModule(new JavaTimeModule())
        );
    }

    @Test
    void shouldHandleAttachScannedDocsEvent() {
        assertThat(handler.handledEvents()).containsExactly(CaseEvent.ATTACH_SCANNED_DOCS);
    }

    @ParameterizedTest
    @ValueSource(strings = {"N9a", "N9b", "N11", "N225", "N180"})
    void shouldReturnScannedDocumentsWithoutChangingState_whenPaperResponseSubtypePresent(String subtype) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .build()
            .toBuilder()
            .scannedDocuments(List.of(
                scannedDocElement(subtype)
            ))
            .build();

        CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getData()).extracting("scannedDocuments").isNotNull();
        assertThat(response.getState()).isNull();
        assertThat(response.getData()).extracting("takenOfflineDate").isNull();
    }

    @Test
    void shouldNotChangeState_whenSubtypeIsNotPaperResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .build()
            .toBuilder()
            .scannedDocuments(List.of(
                scannedDocElement("other-form")
            ))
            .build();

        CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getData()).extracting("scannedDocuments").isNotNull();
        assertThat(response.getState()).isNull();
        assertThat(response.getData()).extracting("takenOfflineDate").isNull();
    }

    @Test
    void shouldNotChangeState_whenScannedDocumentsIsNull() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .build()
            .toBuilder()
            .scannedDocuments(null)
            .build();

        CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getState()).isNull();
        assertThat(response.getData()).extracting("takenOfflineDate").isNull();
    }

    @Test
    void shouldNotChangeState_whenScannedDocumentsIsEmpty() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .build()
            .toBuilder()
            .scannedDocuments(List.of())
            .build();

        CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getState()).isNull();
        assertThat(response.getData()).extracting("takenOfflineDate").isNull();
    }

    @Test
    void shouldNotChangeState_whenSubtypeIsNull() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .build()
            .toBuilder()
            .scannedDocuments(List.of(
                scannedDocElement(null)
            ))
            .build();

        CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getState()).isNull();
        assertThat(response.getData()).extracting("takenOfflineDate").isNull();
    }

    private Element<ScannedDocument> scannedDocElement(String subtype) {
        return new Element<>(UUID.randomUUID(), ScannedDocument.builder().subtype(subtype).build());
    }
}
