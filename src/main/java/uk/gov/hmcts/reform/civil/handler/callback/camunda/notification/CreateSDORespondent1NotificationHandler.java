package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Collections;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED;

/**
 * When an SDO is created it is notified to applicants and defendants.
 * This handler notifies the first defendant.
 */
@Service
public class CreateSDORespondent1NotificationHandler extends AbstractCreateSDORespondentNotificationHandler {

    private static final String TASK_ID_1 = "CreateSDONotifyRespondentSolicitor1";

    public CreateSDORespondent1NotificationHandler(
        CreateSDORespondent1LiPNotificationSender lipNotificationSender,
        CreateSDORespondent1LRNotificationSender lrNotificationSender) {
        super(lipNotificationSender,
              lrNotificationSender,
              TASK_ID_1,
              Collections.singletonList(NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED));
    }

    @Override
    protected boolean isRespondentLiP(CaseData caseData) {
        return caseData.isRespondent1LiP();
    }
}

