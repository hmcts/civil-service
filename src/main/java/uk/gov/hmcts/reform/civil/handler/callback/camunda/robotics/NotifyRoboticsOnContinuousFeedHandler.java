package uk.gov.hmcts.reform.civil.handler.callback.camunda.robotics;

import lombok.extern.slf4j.Slf4j;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RPA_ON_CONTINUOUS_FEED;

@Slf4j
@Service
public class NotifyRoboticsOnContinuousFeedHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_RPA_ON_CONTINUOUS_FEED);
    public static final String TASK_ID = "NotifyRoboticsOnContinuousFeed";
    private final RoboticsNotifier roboticsNotifier;

    public NotifyRoboticsOnContinuousFeedHandler(
        RoboticsNotifier roboticsNotifier
    ) {
        this.roboticsNotifier = roboticsNotifier;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRobotics
        );
    }

    private CallbackResponse notifyRobotics(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = (String) callbackParams.getParams().get(BEARER_TOKEN);
        String caseReference = caseData.getLegacyCaseReference();

        log.info("Notifying RPA on continuous feed for case: {}", caseReference);
        try {
            roboticsNotifier.notifyRobotics(caseData, authToken);
        } catch (Exception e) {
            log.error("Failed to notify RPA on continuous feed for case: {}, error: {}",
                      caseReference, e.getMessage(), e);
            throw e;
        }

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
