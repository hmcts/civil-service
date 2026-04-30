package uk.gov.hmcts.reform.civil.handler.callback.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateGACaseStateCallbackHandlerTest {

    private ObjectMapper objectMapper;
    private UpdateGACaseStateCallbackHandler handler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new UpdateGACaseStateCallbackHandler(objectMapper);
    }

    @Test
    void shouldReturnResponseWithState_whenNextStateIsPresent() {
        // Arrange
        String nextState = "APPLICATION_SUBMITTED";
        GeneralApplicationCaseData caseData = mock(GeneralApplicationCaseData.class);
        when(caseData.getNextState()).thenReturn(nextState);
        when(caseData.toMap(objectMapper)).thenReturn(Map.of());

        CallbackParams params = mock(CallbackParams.class);
        when(params.getType()).thenReturn(CallbackType.ABOUT_TO_SUBMIT);
        when(params.getGeneralApplicationCaseData()).thenReturn(caseData);

        // Act
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Assert
        assertThat(response.getState()).isEqualTo(nextState);
        assertThat(response.getData()).doesNotContainKey("nextState");
    }

    @Test
    void shouldReturnResponseWithoutState_whenNextStateIsNull() {
        // Arrange
        GeneralApplicationCaseData caseData = mock(GeneralApplicationCaseData.class);
        when(caseData.getNextState()).thenReturn(null);
        when(caseData.toMap(objectMapper)).thenReturn(Map.of());

        CallbackParams params = mock(CallbackParams.class);
        when(params.getType()).thenReturn(CallbackType.ABOUT_TO_SUBMIT);
        when(params.getGeneralApplicationCaseData()).thenReturn(caseData);

        // Act
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Assert
        assertThat(response.getState()).isNull();
        assertThat(response.getData()).doesNotContainKey("nextState");
    }

    @Test
    void shouldReturnEmptyResponseOnSubmittedCallback() {
        CallbackParams params = mock(CallbackParams.class);
        when(params.getType()).thenReturn(CallbackType.SUBMITTED);

        Object response = handler.handle(params);

        assertThat(response).isNotNull();
    }

    @Test
    void shouldReturnHandledEvents() {
        assertThat(handler.handledEvents())
            .containsExactly(CaseEvent.UPDATE_CASE_DATA);
    }
}
