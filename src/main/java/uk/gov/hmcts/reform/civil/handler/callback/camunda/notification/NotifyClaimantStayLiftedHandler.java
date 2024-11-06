package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_STAY_LIFTED;

@Service
public class NotifyClaimantStayLiftedHandler extends AbstractNotifyManageStayClaimantHandler {

    private static final String TASK_ID = "NotifyClaimantStayLifted";
    private static final String REFERENCE_TEMPLATE = "stay-lifted-claimant-notification-%s";
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIMANT_STAY_LIFTED);

    public NotifyClaimantStayLiftedHandler(NotificationService notificationService, NotificationsProperties notificationsProperties) {
        super(notificationService, notificationsProperties);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
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
