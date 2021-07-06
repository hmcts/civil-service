package uk.gov.hmcts.reform.civil.handler.callback.camunda.robotics;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RPA_ON_CONTINUOUS_FEED;

@Service
public class NotifyRoboticsOnContinuousFeedHandler extends NotifyRoboticsHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_RPA_ON_CONTINUOUS_FEED);
    public static final String TASK_ID = "NotifyRoboticsOnContinuousFeed";

    public NotifyRoboticsOnContinuousFeedHandler(
        RoboticsNotificationService roboticsNotificationService,
        JsonSchemaValidationService jsonSchemaValidationService,
        RoboticsDataMapper roboticsDataMapper
    ) {
        super(roboticsNotificationService, jsonSchemaValidationService, roboticsDataMapper);
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRoboticsForCaseHandedOffline
        );
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
