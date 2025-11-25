package uk.gov.hmcts.reform.civil.callback;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;

@Service
public class CaseTypeHandlerKeyFactory {

    public String createHandlerKey(CallbackHandler callbackHandler, CaseEvent handledEvent) {
        return callbackHandler instanceof GeneralApplicationCallbackHandler
            ? createGeneralApplicationHandlerKey(handledEvent.name())
            : handledEvent.name();
    }

    public String createHandlerKey(CallbackParams callbackParams) {
        final String eventId = callbackParams.getRequest().getEventId();
        return callbackParams.isGeneralApplicationCaseType()
            ? createGeneralApplicationHandlerKey(eventId)
            : eventId;
    }

    @NotNull
    private String createGeneralApplicationHandlerKey(String eventId) {
        return String.format("%s-%s", GENERALAPPLICATION_CASE_TYPE, eventId);
    }
}
