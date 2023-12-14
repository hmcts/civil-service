package uk.gov.hmcts.reform.civil.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;

public class CallbackControllerTest extends BaseIntegrationTest {

    private static final String CALLBACK_URL = "/cases/callbacks/{callback-type}";
    private static final String CALLBACK_PAGE_ID_URL = "/cases/callbacks/{callback-type}/{page-id}";
    private static final String CALLBACK_VERSION_URL = "/cases/callbacks/version/{version}/{callback-type}";
    private static final String CALLBACK_VERSION_PAGE_ID_URL =
        "/cases/callbacks/version/{version}/{callback-type}/{page-id}";
    private static final String VALID_CALLBACK_TYPE = "about-to-submit";
    private static final String VALID_CALLBACK_TYPE2 = "about-to-start";
    private static final String INVALID_CALLBACK_TYPE = "invalid-callback-type";
    private static final String VALID_PAGE_ID = "start-claim";
    private static final String INVALID_PAGE_ID = "invalid-page-id";
    private static final String V1 = "V_1";
    private static final String INVALID_VERSION = "b2";

    private CallbackRequest callbackRequest;

    @BeforeEach
    void setup() {
        callbackRequest = CallbackRequest.builder()
            .eventId(CREATE_CLAIM.name())
            .caseDetails(CaseDetailsBuilder.builder().atStateAwaitingRespondentAcknowledgement().build())
            .build();
    }

    @Test
    @SneakyThrows
    void shouldReturnNotFoundWhenCallbackHandlerIsNotImplemented() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_URL, "invalid-callback-type")
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldReturnOkWithValidCallbackType() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_URL, VALID_CALLBACK_TYPE2)
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnOkWithValidCallbackTypeAndPageId() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_PAGE_ID_URL, VALID_CALLBACK_TYPE, VALID_PAGE_ID)
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnNotFoundWithInvalidCallbackTypeButValidPage() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_PAGE_ID_URL, INVALID_CALLBACK_TYPE, VALID_PAGE_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldReturnNotFoundWithValidCallbackTypeButInvalidPage() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_PAGE_ID_URL, VALID_CALLBACK_TYPE, INVALID_PAGE_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldReturnNotFoundWithInvalidCallbackTypeAndPage() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_PAGE_ID_URL, INVALID_CALLBACK_TYPE, INVALID_PAGE_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldReturnOkWithValidCallbackTypeAndVersion() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_VERSION_URL, V1, VALID_CALLBACK_TYPE)
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnBadRequestWithInvalidVersion() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_VERSION_URL, INVALID_VERSION, VALID_CALLBACK_TYPE)
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void shouldReturnNotFoundWithValidVersionAndInvalidType() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_VERSION_URL, V1, INVALID_CALLBACK_TYPE)
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldReturnOkWithAllValidParams() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_VERSION_PAGE_ID_URL, V1, VALID_CALLBACK_TYPE, VALID_PAGE_ID)
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnBadRequestWithInvalidVersionOnly() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_VERSION_PAGE_ID_URL, INVALID_VERSION, VALID_CALLBACK_TYPE, VALID_PAGE_ID)
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void shouldReturnBadRequestWithNullBody() {
        doPost(BEARER_TOKEN, null, CALLBACK_PAGE_ID_URL, VALID_CALLBACK_TYPE, VALID_PAGE_ID)
            .andExpect(status().isBadRequest());
    }
}
