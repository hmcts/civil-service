package uk.gov.hmcts.reform.civil.handler.callback.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateCaseDataCallbackHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private UpdateCaseDataCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateCaseDataCallbackHandler(objectMapper);
    }

    @Test
    void shouldReturnResponseWithState_whenNextStateIsPresent() {
        // Given
        String nextState = "AWAITING_APPLICANT_INTENTION";
        CaseData caseData = CaseData.builder()
            .nextState(nextState)
            .build();

        CallbackParams params = mock(CallbackParams.class);
        when(params.getCaseData()).thenReturn(caseData);
        when(params.getType()).thenReturn(CallbackType.ABOUT_TO_SUBMIT);
        when(params.getCaseData()).thenReturn(caseData);

        // When
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getState()).isEqualTo(nextState);
        assertThat(response.getData()).doesNotContainEntry("nextState", nextState);
    }

    @Test
    void shouldReturnResponseWithoutState_whenNextStateIsNull() {
        // Given
        CaseData caseData = CaseData.builder()
            .nextState(null)
            .build();

        CallbackParams params = mock(CallbackParams.class);
        when(params.getCaseData()).thenReturn(caseData);
        when(params.getType()).thenReturn(CallbackType.ABOUT_TO_SUBMIT);
        when(params.getCaseData()).thenReturn(caseData);

        // When
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getState()).isNull();
        assertThat(response.getData().get("nextState")).isNull();
    }

    @Test
    void shouldHandleOnlyUpdateCaseDataEvent() {
        // Then
        assertThat(handler.handledEvents())
            .hasSize(1)
            .first()
            .extracting(Enum::name)
            .isEqualTo("UPDATE_CASE_DATA");
    }
}
