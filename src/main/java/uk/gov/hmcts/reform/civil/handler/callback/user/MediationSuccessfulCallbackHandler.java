package uk.gov.hmcts.reform.civil.handler.callback.user;

import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MEDIATION_SUCCESSFUL;

public class MediationSuccessfulCallbackHandler extends CallbackHandler {
    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(CallbackType.ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(CallbackType.MID, "validate-date"), this::emptyCallbackResponse,
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::emptyCallbackResponse,
            callbackKey(CallbackType.SUBMITTED), this::emptyCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return List.of(MEDIATION_SUCCESSFUL);
    }
}
