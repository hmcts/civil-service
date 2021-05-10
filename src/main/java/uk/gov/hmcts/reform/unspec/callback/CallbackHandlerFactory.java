package uk.gov.hmcts.reform.unspec.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.aspect.EventAllowed;
import uk.gov.hmcts.reform.unspec.aspect.EventEmitter;
import uk.gov.hmcts.reform.unspec.aspect.NoOngoingBusinessProcess;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
public class CallbackHandlerFactory {

    private final HashMap<String, CallbackHandler> eventHandlers = new HashMap<>();
    private final CaseDetailsConverter caseDetailsConverter;

    @Autowired
    public CallbackHandlerFactory(CaseDetailsConverter caseDetailsConverter, CallbackHandler... beans) {
        this.caseDetailsConverter = caseDetailsConverter;
        Arrays.asList(beans).forEach(bean -> bean.register(eventHandlers));
    }

    @EventAllowed
    @NoOngoingBusinessProcess
    @EventEmitter
    public CallbackResponse dispatch(CallbackParams callbackParams) {
        String eventId = callbackParams.getRequest().getEventId();
        return ofNullable(eventHandlers.get(eventId))
            .map(h -> processEvent(h, callbackParams, eventId))
            .orElseThrow(() -> new CallbackException("Could not handle callback for event " + eventId));
    }

    private CallbackResponse processEvent(CallbackHandler handler, CallbackParams callbackParams, String eventId) {
        return Optional.ofNullable(callbackParams.getRequest().getCaseDetailsBefore())
            .map(caseDetailsConverter::toCaseData)
            .map(CaseData::getBusinessProcess)
            .map(businessProcess -> handler.isEventAlreadyProcessed(callbackParams, businessProcess))
            .filter(isProcessed -> isProcessed)
            .map(isProcessed -> eventAlreadyProcessedResponse(eventId))
            .orElse(handler.handle(callbackParams));
    }

    private CallbackResponse eventAlreadyProcessedResponse(String eventId) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of(String.format("Event %s is already processed", eventId)))
            .build();
    }
}
