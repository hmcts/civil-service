package uk.gov.hmcts.reform.civil.callback;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;

@Service
public class CaseTypeHandlerKeyFactory {

    public String createHandlerKey(CallbackHandler callbackHandler, CaseEvent handledEvent) {
        return callbackHandler.getCaseType().equals(GENERALAPPLICATION_CASE_TYPE)
            ? createGeneralApplicationHandlerKey(handledEvent.name())
            : handledEvent.name();
    }

    public String createHandlerKey(CallbackParams callbackParams) {
        final String eventId = callbackParams.getRequest().getEventId();
        return callbackParams.isGeneralApplicationCase()
            ? createGeneralApplicationHandlerKey(eventId)
            : eventId;
    }

    @NotNull
    private String createGeneralApplicationHandlerKey(String eventId) {
        return String.format("%s-%s", GENERALAPPLICATION_CASE_TYPE, eventId);
    }
}
