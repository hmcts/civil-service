package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperBuilder;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.NON_LIVE_STATES;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAIN_CASE_CLOSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_CLOSED;

@ExtendWith(MockitoExtension.class)
class MainCaseClosedEventCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Spy
    private ObjectMapper objectMapper = ObjectMapperBuilder.instance();

    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);

    @Mock
    private Time time;

    @InjectMocks
    private MainCaseClosedEventCallbackHandler handler;

    @Nested
    class AboutToSubmitCallback {

        private final LocalDateTime localDateTime = LocalDateTime.now();

        @ParameterizedTest(name = "The application is in {0} state")
        @EnumSource(value = CaseState.class)
        void shouldRespondWithStateChangedWhenApplicationIsLive(CaseState state) {
            if (!NON_LIVE_STATES.contains(state)) {
                when(time.now()).thenReturn(localDateTime);
            }

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
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
