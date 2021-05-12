package uk.gov.hmcts.reform.civil.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;

public class CallbackControllerTest extends BaseIntegrationTest {

    private static final String CALLBACK_URL = "/cases/callbacks/{callback-type}";

    @Test
    @SneakyThrows
    public void shouldReturnNotFoundWhenCallbackHandlerIsNotImplemented() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CREATE_CLAIM.name())
            .caseDetails(CaseDetailsBuilder.builder().atStateAwaitingRespondentAcknowledgement().build())
            .build();

        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_URL, "invalid-callback-type")
            .andExpect(status().isNotFound());
    }
}
