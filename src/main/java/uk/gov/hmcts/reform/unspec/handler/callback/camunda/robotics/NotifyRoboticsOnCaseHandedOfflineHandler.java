package uk.gov.hmcts.reform.unspec.handler.callback.camunda.robotics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.service.robotics.RoboticsNotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_RPA_ON_CASE_HANDED_OFFLINE;

@Service
@RequiredArgsConstructor
public class NotifyRoboticsOnCaseHandedOfflineHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_RPA_ON_CASE_HANDED_OFFLINE);
    public static final String TASK_ID = "NotifyRoboticsOnCaseHandedOffline";

    private final RoboticsNotificationService roboticsNotificationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRoboticsForCaseHandedOffline
        );
    }

    @Override
    public String camundaActivityId() {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRoboticsForCaseHandedOffline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        roboticsNotificationService.notifyRobotics(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
