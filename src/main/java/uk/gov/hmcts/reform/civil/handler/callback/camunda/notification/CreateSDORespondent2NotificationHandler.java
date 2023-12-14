package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Collections;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED;

/**
 * When an SDO is created it is notified to applicants and defendants.
 * This handler notifies the second defendant, if any.
 */
@Service
public class CreateSDORespondent2NotificationHandler extends AbstractCreateSDORespondentNotificationHandler {

    public static final String TASK_ID_2 = "CreateSDONotifyRespondentSolicitor2";

    public CreateSDORespondent2NotificationHandler(
        CreateSDORespondent2LiPNotificationSender lipNotificationSender,
        CreateSDORespondent2LRNotificationSender lrNotificationSender) {
        super(
            lipNotificationSender,
            lrNotificationSender,
            TASK_ID_2,
            Collections.singletonList(NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED)
        );
    }

    @Override
    protected boolean isRespondentLiP(CaseData caseData) {
        return caseData.isRespondent2LiP();
    }

    protected CallbackResponse notifyRespondentSolicitorSDOTriggered(CallbackParams callbackParams) {
        if (callbackParams.getCaseData().getRespondent2() != null) {
            return super.notifyRespondentSolicitorSDOTriggered(callbackParams);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}

