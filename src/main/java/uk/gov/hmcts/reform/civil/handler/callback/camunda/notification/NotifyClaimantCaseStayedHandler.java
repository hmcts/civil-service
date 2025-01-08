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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_STAY_CASE;

@Service
public class NotifyClaimantCaseStayedHandler extends AbstractNotifyCaseStayedHandler {

    private static final String TASK_ID = "NotifyClaimantStayCase";
    private static final String REFERENCE_TEMPLATE = "case-stayed-claimant-notification-%s";
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIMANT_STAY_CASE);

    public NotifyClaimantCaseStayedHandler(NotificationService notificationService, NotificationsProperties notificationsProperties) {
        super(notificationService, notificationsProperties);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected String getRecipient(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return caseData.isApplicantLiP()
            ? caseData.getClaimantUserDetails().getEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    @Override
    protected boolean isLiP(CaseData caseData) {
        return caseData.isApplicantLiP();
    }

    @Override
    protected boolean isBilingual(CaseData caseData) {
        return caseData.isClaimantBilingual();
    }

    @Override
    protected String getPartyName(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return caseData.getApplicant1().getPartyName();
    }

    @Override
    public Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::sendNotification);
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
