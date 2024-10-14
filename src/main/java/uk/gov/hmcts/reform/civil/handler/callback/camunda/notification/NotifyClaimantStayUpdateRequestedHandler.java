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

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED;

@Service
public class NotifyClaimantStayUpdateRequestedHandler extends AbstractNotifyManageStayHandler {

    private static final String TASK_ID = "NotifyClaimantStayUpdateRequested";
    private static final String REFERENCE_TEMPLATE = "stay-update-requested-claimant-notification-%s";
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED);

    public NotifyClaimantStayUpdateRequestedHandler(NotificationService notificationService, NotificationsProperties notificationsProperties) {
        super(notificationService, notificationsProperties);
    }

    @Override
    public Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::sendNotification);
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
    protected String getRecipient(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return caseData.isApplicantLiP()
            ? caseData.getClaimantUserDetails().getEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
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
    protected boolean isLiP(CaseData caseData) {
        return caseData.isApplicantLiP();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected String getPartyName(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return caseData.getApplicant1().getPartyName();
    }
}
