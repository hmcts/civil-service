package uk.gov.hmcts.reform.civil.handler.callback.camunda.robotics;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.notification.robotics.RoboticsNotifier;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RPA_ON_CASE_HANDED_OFFLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RETRY_NOTIFY_RPA_ON_CASE_HANDED_OFFLINE;

@Service
public class NotifyRoboticsOnCaseHandedOfflineHandler extends CallbackHandler {

    private final RoboticsNotifier roboticsNotifier;

    public NotifyRoboticsOnCaseHandedOfflineHandler(
        RoboticsNotifier roboticsNotifier

    ) {
        this.roboticsNotifier = roboticsNotifier;
    }

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
        RETRY_NOTIFY_RPA_ON_CASE_HANDED_OFFLINE
    );
    public static final String TASK_ID = "NotifyRoboticsOnCaseHandedOffline";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRobotics
        );
    }

    private CallbackResponse notifyRobotics(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = (String) callbackParams.getParams().get(BEARER_TOKEN);

        roboticsNotifier.notifyRobotics(caseData, authToken);

        return SubmittedCallbackResponse.builder().build();
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

}
