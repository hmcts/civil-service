package uk.gov.hmcts.reform.civil.handler.callback.camunda.robotics;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpec;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RPA_DJ_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RPA_DJ_UNSPEC;

@Service
public class NotifyDefaultJudgmentHandler extends NotifyRoboticsHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RPA_DJ_UNSPEC,
        NOTIFY_RPA_DJ_SPEC
    );

    public NotifyDefaultJudgmentHandler(
        RoboticsNotificationService roboticsNotificationService,
        JsonSchemaValidationService jsonSchemaValidationService,
        RoboticsDataMapper roboticsDataMapper,
        RoboticsDataMapperForSpec roboticsDataMapperForSpec,
        FeatureToggleService toggleService
    ) {
        super(
            roboticsNotificationService,
            jsonSchemaValidationService,
            roboticsDataMapper,
            roboticsDataMapperForSpec,
            toggleService
        );
    }

    public static final String TASK_ID = "NotifyRPADJ";
    public static final String TASK_ID_SPEC = "NotifyRPADJSPEC";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRobotics
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isSpecHandler(callbackParams) ? TASK_ID_SPEC : TASK_ID;

    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected void sendNotifications(CallbackParams callbackParams, CaseData caseData, boolean multiPartyScenario) {
        if (toggleService.isPinInPostEnabled() && caseData.isRespondent1NotRepresented()) {
            roboticsNotificationService.notifyJudgementLip(caseData);
        } else {
            roboticsNotificationService.notifyRobotics(caseData, multiPartyScenario,
                                                       callbackParams.getParams().get(BEARER_TOKEN).toString()
            );
        }
    }

    private boolean isSpecHandler(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(CaseEvent.NOTIFY_RPA_DJ_SPEC.name());
    }
}
