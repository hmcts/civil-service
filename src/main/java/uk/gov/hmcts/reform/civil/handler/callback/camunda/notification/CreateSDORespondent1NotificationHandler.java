package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED;

/**
 * Can handle both respondent 1 and respondent 2 notification for sdo created.
 */
@Service
@RequiredArgsConstructor
public class CreateSDORespondent1NotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED, NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED
    );

    public static final String TASK_ID_1 = "CreateSDONotifyRespondentSolicitor1";
    public static final String TASK_ID_2 = "CreateSDONotifyRespondentSolicitor2";

    private final CreateSDORespondent1LiPNotificationSender lip1NotificationSender;
    private final CreateSDORespondent2LiPNotificationSender lip2NotificationSender;
    private final CreateSDORespondent1LRNotificationSender lr1NotificationSender;
    private final CreateSDORespondent2LRNotificationSender lr2NotificationSender;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitor1SDOTriggered
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        if (NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED.name().equals(callbackParams.getRequest().getEventId())) {
            return TASK_ID_1;
        } else {
            return TASK_ID_2;
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitor1SDOTriggered(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED.name().equals(callbackParams.getRequest().getEventId())) {
            if (caseData.isRespondent1Represented()) {
                lr1NotificationSender.notifyRespondentPartySDOTriggered(caseData);
            } else {
                lip1NotificationSender.notifyRespondentPartySDOTriggered(caseData);
            }
        } else if (caseData.getRespondent2() != null) {
            if (caseData.isRespondent2Represented()) {
                lr2NotificationSender.notifyRespondentPartySDOTriggered(caseData);
            } else {
                lip2NotificationSender.notifyRespondentPartySDOTriggered(caseData);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}

