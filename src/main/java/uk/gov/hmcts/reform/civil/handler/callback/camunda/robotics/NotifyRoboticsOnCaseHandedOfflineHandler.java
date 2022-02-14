package uk.gov.hmcts.reform.civil.handler.callback.camunda.robotics;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpec;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RPA_ON_CASE_HANDED_OFFLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RETRY_NOTIFY_RPA_ON_CASE_HANDED_OFFLINE;

@Service
public class NotifyRoboticsOnCaseHandedOfflineHandler extends NotifyRoboticsHandler {

    public NotifyRoboticsOnCaseHandedOfflineHandler(
        RoboticsNotificationService roboticsNotificationService,
        JsonSchemaValidationService jsonSchemaValidationService,
        RoboticsDataMapper roboticsDataMapper,
        RoboticsDataMapperForSpec roboticsDataMapperForSpec
    ) {
        super(roboticsNotificationService, jsonSchemaValidationService, roboticsDataMapper, roboticsDataMapperForSpec);
    }

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
        RETRY_NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
        NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_SPEC
    );
    public static final String TASK_ID = "NotifyRoboticsOnCaseHandedOffline";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRobotics
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
