package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_LOCATION_UPDATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_UPDATE_GA_LOCATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_TASK_RECONFIG;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_TASK_RECONFIG_GA;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerGenAppLocationUpdateCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(TRIGGER_UPDATE_GA_LOCATION,
                                                          TRIGGER_TASK_RECONFIG_GA);

    private final GenAppStateHelperService helperService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::triggerGaEvent
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse triggerGaEvent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        try {
            if (caseData.getGeneralApplications() != null && !caseData.getGeneralApplications().isEmpty()) {
                caseData = helperService.updateApplicationLocationDetailsInClaim(caseData);
                if (callbackParams.getRequest().getEventId().equals(TRIGGER_UPDATE_GA_LOCATION.name())) {
                    helperService.triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
                } else if (callbackParams.getRequest().getEventId().equals(TRIGGER_TASK_RECONFIG_GA.name())) {
                    helperService.triggerEvent(caseData, TRIGGER_TASK_RECONFIG);
                }
            }
        } catch (Exception e) {
            String errorMessage = "Could not trigger event to update location on application under case: "
                + caseData.getCcdCaseReference();
            log.error(errorMessage, e);
            return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of(errorMessage)).build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toBuilder().build().toMap(objectMapper))
            .build();
    }

}
