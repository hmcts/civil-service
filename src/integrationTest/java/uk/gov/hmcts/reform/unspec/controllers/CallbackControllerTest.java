package uk.gov.hmcts.reform.unspec.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID_SECONDARY;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.REQUEST_EXTENSION;

public class CallbackControllerTest extends BaseIntegrationTest {

    private static final String CALLBACK_URL = "/cases/callbacks/{callback-type}";

    @Test
    @SneakyThrows
    public void shouldReturnNotFoundWhenCallbackHandlerIsNotImplemented() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(REQUEST_EXTENSION.getValue())
            .caseDetails(CaseDetailsBuilder.builder().atStateExtensionRequested().build())
            .build();

        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_URL, MID_SECONDARY.getValue())
            .andExpect(status().isNotFound());
    }
}
