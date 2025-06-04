package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CaseNoteService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_CASE_NOTE;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddCaseNoteCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(ADD_CASE_NOTE);

    private final CaseNoteService caseNoteService;
    private final ObjectMapper objectMapper;
    private final SimpleStateFlowEngine stateFlowEngine;

    @Value("${azure.service-bus.ccd-events-topic.enabled:false}")
    private boolean ccdEventsServiceBusEnabled;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::test,
            callbackKey(ABOUT_TO_SUBMIT), this::moveNotesIntoList,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    private CallbackResponse test(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("STATE HISTORY: {}", stateFlowEngine.evaluate(caseData).getStateHistory().toString());
        log.info("FLOW FLAG HISTORY: {}", stateFlowEngine.evaluate(caseData).getFlags());
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse moveNotesIntoList(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseNote caseNote = caseNoteService.buildCaseNote(
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            caseData.getCaseNote()
        );

        List<Element<CaseNote>> caseNotes = caseNoteService.addNoteToListStart(caseNote, caseData.getCaseNotes());

        CaseData.CaseDataBuilder updatedCaseDataBuilder = caseData.toBuilder()
            .caseNotes(caseNotes)
            .caseNote(null);

        if (!ccdEventsServiceBusEnabled) {
            updatedCaseDataBuilder.businessProcess(BusinessProcess.ready(ADD_CASE_NOTE));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
