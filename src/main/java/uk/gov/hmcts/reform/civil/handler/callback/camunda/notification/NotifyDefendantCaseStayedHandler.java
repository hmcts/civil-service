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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_STAY_CASE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_TWO_STAY_CASE;

@Service
public class NotifyDefendantCaseStayedHandler extends AbstractNotifyCaseStayedHandler {

    private static final String TASK_ID_DEF_ONE = "NotifyDefendantStayCase";
    private static final String TASK_ID_DEF_TWO = "NotifyDefendant2StayCase";
    private static final String REFERENCE_TEMPLATE = "case-stayed-defendant-notification-%s";
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_DEFENDANT_STAY_CASE, NOTIFY_DEFENDANT_TWO_STAY_CASE);

    public NotifyDefendantCaseStayedHandler(NotificationService notificationService, NotificationsProperties notificationsProperties) {
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
            return isForRespondent1(callbackParams)
                ? caseData.getRespondentSolicitor1EmailAddress()
                : caseData.getRespondentSolicitor2EmailAddress();
        }
    }

    @Override
    protected boolean isLiP(CaseData caseData) {
        return caseData.isRespondent1LiP();
    }

    @Override
    protected boolean isBilingual(CaseData caseData) {
        return caseData.isRespondentResponseBilingual();
    }

    @Override
    protected String getPartyName(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return isForRespondent1(callbackParams)
            ? caseData.getRespondent1().getPartyName()
            : caseData.getRespondent2().getPartyName();
    }

    @Override
    public Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::sendNotification);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId().equals(NOTIFY_DEFENDANT_STAY_CASE.name())
            ? TASK_ID_DEF_ONE : TASK_ID_DEF_TWO;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private boolean isForRespondent1(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_DEFENDANT_STAY_CASE.name());
    }
}
