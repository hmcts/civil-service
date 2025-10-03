package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.NON_LIVE_STATES;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAIN_CASE_CLOSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_CLOSED;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    MainCaseClosedEventCallbackHandler.class, JacksonAutoConfiguration.class, Time.class
})
class MainCaseClosedEventCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;

    @Autowired
    private MainCaseClosedEventCallbackHandler handler;

    @Nested
    class AboutToSubmitCallback {

        private LocalDateTime localDateTime;

        @BeforeEach
        void setup() {
            localDateTime = LocalDateTime.now();
            when(time.now()).thenReturn(localDateTime);
        }

        @ParameterizedTest(name = "The application is in {0} state")
        @EnumSource(value = CaseState.class)
        void shouldRespondWithStateChangedWhenApplicationIsLive(CaseState state) {
            CaseData caseData = CaseDataBuilder.builder()
                .ccdCaseReference(1234L)
                .ccdState(state).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            if (!NON_LIVE_STATES.contains(state)) {
                assertThat(response.getErrors()).isNull();
                assertThat(response.getState()).isEqualTo(APPLICATION_CLOSED.toString());
                assertThat(response.getData()).extracting("businessProcess").extracting("status").isEqualTo("FINISHED");
                assertThat(response.getData()).extracting("businessProcess").extracting("camundaEvent").isEqualTo(
                    "MAIN_CASE_CLOSED");
                assertThat(response.getData()).containsEntry(
                    "applicationClosedDate",
                    localDateTime.format(ISO_DATE_TIME)
                );
            } else {
                assertThat(response).isEqualTo(AboutToStartOrSubmitCallbackResponse.builder().build());
            }
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(MAIN_CASE_CLOSED);
    }
}
