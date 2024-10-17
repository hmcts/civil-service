package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED;

@Service
public class NotifyClaimantStayUpdateRequestedHandler extends AbstractNotifyManageStayClaimantHandler {

    private static final String TASK_ID = "NotifyClaimantStayUpdateRequested";
    private static final String REFERENCE_TEMPLATE = "stay-update-requested-claimant-notification-%s";
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED);

    public NotifyClaimantStayUpdateRequestedHandler(NotificationService notificationService, NotificationsProperties notificationsProperties) {
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
    protected String getNotificationTemplate(CaseData caseData) {
        if (isLiP(caseData)) {
            return isBilingual(caseData)
                ? notificationsProperties.getNotifyLipBilingualStayUpdateRequested()
                : notificationsProperties.getNotifyLipStayUpdateRequested();
        } else {
            return notificationsProperties.getNotifyLRStayUpdateRequested();
        }
    }

    protected boolean isBilingual(CaseData caseData) {
        return caseData.isClaimantBilingual();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

}
