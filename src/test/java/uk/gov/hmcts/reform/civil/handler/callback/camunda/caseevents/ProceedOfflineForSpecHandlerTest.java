package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PROCEEDS_IN_HERITAGE_SYSTEM_SPEC;

class ProceedOfflineForSpecHandlerTest extends BaseCallbackHandlerTest {

    ProceedOfflineForSpecCallbackHandler handler;
    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        handler = new uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.ProceedOfflineForSpecCallbackHandler(new com.fasterxml.jackson.databind.ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()));
    }

    @Test
    void shouldCaptureTakenOfflineDate_whenProceedInHeritageSystemRequested() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().build();
        CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getData()).extracting("takenOfflineDate").isNotNull();
    }

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(params)).isEqualTo("ProceedOfflineForSpec");
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(PROCEEDS_IN_HERITAGE_SYSTEM_SPEC);
    }
}
