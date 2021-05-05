package uk.gov.hmcts.reform.unspec.handler.callback.camunda.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.unspec.service.Time;
import uk.gov.hmcts.reform.unspec.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.unspec.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.unspec.service.robotics.exception.JsonSchemaValidationException;
import uk.gov.hmcts.reform.unspec.service.robotics.exception.RoboticsDataException;
import uk.gov.hmcts.reform.unspec.service.robotics.mapper.RoboticsDataMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_RPA_ON_CASE_HANDED_OFFLINE;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.RETRY_NOTIFY_RPA_ON_CASE_HANDED_OFFLINE;

@Service
@RequiredArgsConstructor
public class NotifyRoboticsOnCaseHandedOfflineHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
                                                          RETRY_NOTIFY_RPA_ON_CASE_HANDED_OFFLINE);
    public static final String TASK_ID = "NotifyRoboticsOnCaseHandedOffline";

    private final RoboticsNotificationService roboticsNotificationService;
    private final JsonSchemaValidationService jsonSchemaValidationService;
    private final RoboticsDataMapper roboticsDataMapper;
    private final Time time;
    private final ObjectMapper mapper;

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

        try {
            RoboticsCaseData roboticsCaseData = roboticsDataMapper.toRoboticsCaseData(caseData);
            Set<ValidationMessage> errors = jsonSchemaValidationService.validate(roboticsCaseData.toJsonString());
            if (errors.isEmpty()) {
                roboticsNotificationService.notifyRobotics(caseData);
            } else {
                throw new JsonSchemaValidationException(
                    format("Invalid RPA Json payload for %s", caseData.getLegacyCaseReference()),
                    errors
                );
            }
        } catch (JsonProcessingException e) {
            throw new RoboticsDataException(e.getMessage(), e);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
