package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerMoveApplicationOfflineCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE);

    private final GenAppStateHelperService helperService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::triggerMoveApplicationOffline
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse triggerMoveApplicationOffline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        try {
            if (caseData.getGeneralApplications() != null && !caseData.getGeneralApplications().isEmpty()) {
                helperService.triggerEvent(caseData, APPLICATION_PROCEEDS_IN_HERITAGE);
            }
        } catch (Exception e) {
            String errorMessage = "Could not trigger event to take application offline under the case: "
                    + caseData.getCcdCaseReference();
            log.error(errorMessage, e);
            return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of(errorMessage)).build();
        }
        return emptyCallbackResponse(callbackParams);
    }
}
