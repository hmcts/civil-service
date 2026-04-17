package uk.gov.hmcts.reform.civil.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.aspect.EventAllowed;
import uk.gov.hmcts.reform.civil.aspect.EventEmitter;
import uk.gov.hmcts.reform.civil.aspect.NoOngoingBusinessProcess;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
public class CallbackHandlerFactory {

    private final HashMap<String, CallbackHandler> eventHandlers = new HashMap<>();
    private final CaseDetailsConverter caseDetailsConverter;
    private final CaseTypeHandlerKeyFactory caseTypeHandlerKeyFactory;
    private final ObjectMapper objectMapper;

    @Autowired
    public CallbackHandlerFactory(CaseDetailsConverter caseDetailsConverter,
                                  CaseTypeHandlerKeyFactory caseTypeHandlerKeyFactory,
                                  ObjectMapper objectMapper,
                                  CallbackHandler... callbackHandlers) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.caseTypeHandlerKeyFactory = caseTypeHandlerKeyFactory;
        this.objectMapper = objectMapper;
        Arrays.asList(callbackHandlers).forEach(
            callbackHandler ->
                callbackHandler.handledEvents().forEach(
                    handleEvent -> registerHandlerForEvent(callbackHandler, handleEvent)
                ));
    }

    private void registerHandlerForEvent(CallbackHandler callbackHandler, CaseEvent handledEvent) {
        caseTypeHandlerKeyFactory.createRegistrationKeys(callbackHandler, handledEvent)
            .forEach(handlerKey -> eventHandlers.put(handlerKey, callbackHandler));
    }

    @EventAllowed
    @NoOngoingBusinessProcess
    @EventEmitter
    public CallbackResponse dispatch(CallbackParams callbackParams) {
        final String eventId = caseTypeHandlerKeyFactory.createDispatchKey(callbackParams);

        return ofNullable(eventHandlers.get(eventId))
            .map(h -> processEvent(h, callbackParams, eventId))
            .orElseThrow(() -> new CallbackException("Could not handle callback for event " + eventId));
    }

    private CallbackResponse processEvent(CallbackHandler handler, CallbackParams callbackParams, String eventId) {
        return Optional.ofNullable(callbackParams.getRequest().getCaseDetailsBefore())
            .map(caseDetailsConverter::toCaseData)
            .filter(caseData -> Optional.ofNullable(caseData.getBusinessProcess())
                .map(businessProcess -> handler.isEventAlreadyProcessed(callbackParams, businessProcess))
                .orElse(false))
            .map(caseData -> eventAlreadyProcessedResponse(eventId, caseData))
            .orElseGet(() -> handler.handle(callbackParams));
    }

    private CallbackResponse eventAlreadyProcessedResponse(String eventId, CaseData caseData) {
        log.info("Event is already processed for the case {} .. eventId {}", caseData.getCcdCaseReference(), eventId);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
