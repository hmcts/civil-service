package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED;

@Service
public class NotifyDefendantStayUpdateRequestedHandler extends AbstractNotifyManageStayHandler {

    private static final String TASK_ID = "NotifyDefendantStayUpdateRequested";
    private static final String TASK_ID_DEFENDANT_2 = "NotifyDefendant2StayUpdateRequested";
    private static final String REFERENCE_TEMPLATE = "stay-update-requested-defendant-notification-%s";
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED,
                                                          NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED);

    public NotifyDefendantStayUpdateRequestedHandler(NotificationService notificationService, NotificationsProperties notificationsProperties) {
        super(notificationService, notificationsProperties);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected String getRecipient(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.isRespondent1LiP() && nonNull(caseData.getRespondent1().getPartyEmail())) {
            return caseData.getRespondent1().getPartyEmail();
        } else {
            if (isRespondentSolicitor2(callbackParams)) {
                return caseData.getRespondentSolicitor2EmailAddress();
            }
            return caseData.getRespondentSolicitor1EmailAddress();
        }
    }

    @Override
    protected boolean isLiP(CaseData caseData) {
        return caseData.isRespondent1LiP();
    }

    @Override
    protected String getNotificationTemplate(CaseData caseData) {
        if (isLiP(caseData)) {
            // TODO: add lip template
            return null;
        } else {
            return notificationsProperties.getNotifyLRStayUpdateRequested();
        }
    }

    @Override
    protected String getPartyName(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return isRespondentSolicitor2(callbackParams)
            ? caseData.getRespondent2().getPartyName()
            : caseData.getRespondent1().getPartyName();
    }

    @Override
    public Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::sendNotification);
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

    private boolean isRespondentSolicitor2(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        return NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED.equals(caseEvent);
    }
}
