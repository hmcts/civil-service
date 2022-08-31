package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAIN_CASE_CLOSED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_APPLICATION_CLOSURE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerApplicationClosureCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(TRIGGER_APPLICATION_CLOSURE);

    private final CoreCaseDataService coreCaseDataService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::triggerGeneralApplicationClosure
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse triggerGeneralApplicationClosure(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getGeneralApplications() != null && !caseData.getGeneralApplications().isEmpty()) {
            caseData.getGeneralApplications()
                    .forEach(application ->
                            triggerEvent(parseLong(application.getValue().getCaseLink().getCaseReference())));
        }
        return emptyCallbackResponse(callbackParams);
    }

    private void triggerEvent(Long caseId) {
        try {
            log.info("Triggering MAIN_CASE_CLOSED event to close the underlying general application: [{}]",
                    caseId);
            coreCaseDataService.triggerGeneralApplicationEvent(caseId, MAIN_CASE_CLOSED);
        } catch (Exception e) {
            log.error("Could not trigger event to close application [{}]", caseId, e);
        }
    }
}
