package uk.gov.hmcts.reform.civil.callback;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;

import java.util.List;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;

@Service
public class CaseTypeHandlerKeyFactory {

    public List<String> createRegistrationKeys(CallbackHandler callbackHandler, CaseEvent handledEvent) {
        // Multi-case handlers must be registered under both Civil and GA keys; dispatch resolves by case type.
        String eventKey = handledEvent.name();
        return switch (callbackHandler) {
            case MultiCaseTypeCallbackHandler ignored -> List.of(eventKey, createGeneralApplicationHandlerKey(eventKey));
            case GeneralApplicationCallbackHandler ignored -> List.of(createGeneralApplicationHandlerKey(eventKey));
            default -> List.of(eventKey);
        };
    }

    public String createDispatchKey(CallbackParams callbackParams) {
        // Dispatch key depends on the incoming case type, not the handler type.
        final String eventId = callbackParams.getRequest().getEventId();
        return callbackParams.isGeneralApplicationCaseType()
            ? createGeneralApplicationHandlerKey(eventId)
            : eventId;
    }

    @NotNull
    public String createGeneralApplicationHandlerKey(String eventId) {
        return String.format("%s-%s", GENERALAPPLICATION_CASE_TYPE, eventId);
    }
}
