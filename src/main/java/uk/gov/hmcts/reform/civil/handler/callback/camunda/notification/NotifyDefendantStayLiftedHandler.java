package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT2_STAY_LIFTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_STAY_LIFTED;

@Service
public class NotifyDefendantStayLiftedHandler extends AbstractNotifyStayLiftedHandler {

    private static final String TASK_ID = "NotifyDefendantStayLifted";
    private static final String TASK_ID_DEFENDANT_2 = "NotifyDefendant2StayLifted";
    private static final String REFERENCE_TEMPLATE = "stay-lifted-defendant-notification-%s";
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_DEFENDANT_STAY_LIFTED, NOTIFY_DEFENDANT2_STAY_LIFTED);

    public NotifyDefendantStayLiftedHandler(NotificationService notificationService, NotificationsProperties notificationsProperties) {
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
        }
        return isRespondentSolicitor2(callbackParams) ? caseData.getRespondentSolicitor2EmailAddress() : caseData.getRespondentSolicitor1EmailAddress();
    }

    @Override
    protected boolean isLiP(CallbackParams params) {
        return params.getCaseData().isRespondent1LiP();
    }

    @Override
    protected String getPartyName(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return isRespondentSolicitor2(callbackParams) ? caseData.getRespondent2().getPartyName() : caseData.getRespondent1().getPartyName();
    }

    @Override
    protected boolean isBilingual(CallbackParams params) {
        return params.getCaseData().isRespondentResponseBilingual();
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isRespondentSolicitor2(callbackParams) ? TASK_ID_DEFENDANT_2 : TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private boolean isRespondentSolicitor2(CallbackParams callbackParams) {
        return NOTIFY_DEFENDANT2_STAY_LIFTED.equals(CaseEvent.valueOf(callbackParams.getRequest().getEventId()));
    }
}
