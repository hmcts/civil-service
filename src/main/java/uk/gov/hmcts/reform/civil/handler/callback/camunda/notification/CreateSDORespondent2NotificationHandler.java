package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Collections;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED;

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
}

