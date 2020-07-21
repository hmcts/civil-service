package uk.gov.hmcts.reform.unspec.callback;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.unspec.callback.CallbackVersion.V_2;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CREATE_CASE;

@ExtendWith(MockitoExtension.class)
public class CallbackHandlerFactoryTest {

    public static final String BEARER_TOKEN = "Bearer Token";
    public static final CallbackResponse RESPONSE = AboutToStartOrSubmitCallbackResponse.builder().build();

    private CallbackHandler sampleCallbackHandler = new CallbackHandler() {
        @Override
        protected Map<CallbackType, Callback> callbacks() {
            return ImmutableMap.of(
                ABOUT_TO_SUBMIT, this::createCitizenClaim
            );
        }

        private CallbackResponse createCitizenClaim(CallbackParams callbackParams) {
            return RESPONSE;
        }

        @Override
        public List<CaseEvent> handledEvents() {
            return Collections.singletonList(CREATE_CASE);
        }
    };

    private CallbackHandlerFactory callbackHandlerFactory;

    @BeforeEach
    public void setUp() {
        callbackHandlerFactory = new CallbackHandlerFactory(ImmutableList.of(sampleCallbackHandler));
    }

    @Test
    public void shouldThrowCallbackException_whenUnknownEvent() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId("nope")
            .build();
        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .version(V_2)
            .build();

        assertThatThrownBy(() -> callbackHandlerFactory.dispatch(params))
            .isInstanceOf(CallbackException.class)
            .hasMessage("Could not handle callback for event nope");
    }

    @Test
    public void shouldDispatchCallback_whenValidCaseEvent() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(CREATE_CASE.getValue())
            .build();
        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .type(ABOUT_TO_SUBMIT)
            .version(V_1)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        CallbackResponse callbackResponse = callbackHandlerFactory.dispatch(params);

        assertEquals(RESPONSE, callbackResponse);
    }
}
