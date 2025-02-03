package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HearingFeePaidCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private HearingFeePaidCallbackHandler handler;

    @Mock
    private ObjectMapper objectMapper;

    @Nested
    class AboutToSubmit {

        @Test
        void shouldUpdateBusinessProcessToReadyWithEvent_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateHearingFeeDuePaid().build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo("PREPARE_FOR_HEARING_CONDUCT_HEARING");
        }
    }
}
