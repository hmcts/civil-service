package uk.gov.hmcts.reform.unspec.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;

public class CallbackControllerTest extends BaseIntegrationTest {

    private static final String CALLBACK_URL = "/cases/callbacks/{callback-type}";

    @Test
    @SneakyThrows
    public void shouldReturnNotFoundWhenCallbackHandlerIsNotImplemented() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.CREATE_CASE.getValue())
            .caseDetails(CaseDetails.builder().build())
            .build();

        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_URL, ABOUT_TO_START.getValue())
            .andExpect(status().isNotFound());
    }
}
