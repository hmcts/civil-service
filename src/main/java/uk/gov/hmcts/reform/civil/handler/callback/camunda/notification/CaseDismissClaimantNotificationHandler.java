package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.List;

@Service
public class CaseDismissClaimantNotificationHandler extends AbstractCaseDismissNotificationHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        CaseEvent.NOTIFY_CLAIMANT_DISMISS_CASE
    );

    public CaseDismissClaimantNotificationHandler(NotificationService notificationService,
                                                  NotificationsProperties notificationsProperties) {
        super(notificationService, notificationsProperties);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected String getPartyName(CaseData caseData, CallbackParams callbackParams) {
        return caseData.getApplicant1().getPartyName();
    }

    protected String getReferenceTemplate() {
        return "dismiss-case-claimant-notification-%s";
    }

    protected String getRecipient(CallbackParams params) {
        CaseData caseData = params.getCaseData();
        return caseData.isApplicantLiP()
            ? caseData.getClaimantUserDetails().getEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    protected boolean isBilingual(CallbackParams params) {
        return params.getCaseData().isClaimantBilingual();
    }

    protected boolean isLiP(CallbackParams params) {
        return params.getCaseData().isApplicantLiP();
    }
}
