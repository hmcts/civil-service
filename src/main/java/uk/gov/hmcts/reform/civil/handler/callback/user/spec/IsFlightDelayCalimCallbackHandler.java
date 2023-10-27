package uk.gov.hmcts.reform.civil.handler.callback.user.spec;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class IsFlightDelayCalimCallbackHandler extends CallbackHandler {

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(MID, "is-flight-delay-claim"), this::isFlightDelayClaim)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.emptyList();
    }

    private CallbackResponse isFlightDelayClaim(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}


