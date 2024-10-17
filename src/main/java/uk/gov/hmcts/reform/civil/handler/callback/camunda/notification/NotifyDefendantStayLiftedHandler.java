package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT2_STAY_LIFTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_STAY_LIFTED;

@Service
public class NotifyDefendantStayLiftedHandler extends AbstractNotifyManageStayDefendantHandler {

    private static final String TASK_ID = "NotifyDefendantStayLifted";
    private static final String TASK_ID_DEFENDANT_2 = "NotifyDefendant2StayLifted";
    private static final String REFERENCE_TEMPLATE = "stay-lifted-defendant-notification-%s";
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_DEFENDANT_STAY_LIFTED,
                                                          NOTIFY_DEFENDANT2_STAY_LIFTED);

    public NotifyDefendantStayLiftedHandler(NotificationService notificationService, NotificationsProperties notificationsProperties) {
        super(notificationService, notificationsProperties);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        if (isRespondentSolicitor2(callbackParams)) {
            return TASK_ID_DEFENDANT_2;
        } else {
            return TASK_ID;
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected boolean isRespondentSolicitor2(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        return NOTIFY_DEFENDANT2_STAY_LIFTED.equals(caseEvent);
    }
}
