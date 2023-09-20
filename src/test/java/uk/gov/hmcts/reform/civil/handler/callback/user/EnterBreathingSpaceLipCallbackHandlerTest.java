package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ENTER_BREATHING_SPACE_LIP;

public class EnterBreathingSpaceLipCallbackHandlerTest extends BaseCallbackHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EnterBreathingSpaceLipCallbackHandler callbackHandler
        = new EnterBreathingSpaceLipCallbackHandler(objectMapper);

    @MockBean
    private Time time;

    @Nested
    class AboutToSubmitCallback {

        @Test
        public void shouldUpdateBusinessProcess_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(ENTER_BREATHING_SPACE_LIP.name(), "READY");
        }
    }
}
