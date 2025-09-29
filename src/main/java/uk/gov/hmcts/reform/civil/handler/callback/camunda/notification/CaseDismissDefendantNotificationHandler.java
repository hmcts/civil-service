package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

@Service
public class CaseDismissDefendantNotificationHandler extends AbstractCaseDismissNotificationHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        CaseEvent.NOTIFY_DEFENDANT_DISMISS_CASE,
        CaseEvent.NOTIFY_DEFENDANT_TWO_DISMISS_CASE
    );

    public CaseDismissDefendantNotificationHandler(NotificationService notificationService, NotificationsProperties notificationsProperties,
                                                   FeatureToggleService featureToggleService,
                                                   NotificationsSignatureConfiguration configuration) {
        super(notificationService, notificationsProperties, configuration, featureToggleService);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected String getPartyName(CaseData caseData, CallbackParams callbackParams) {
        return isForRespondent1(callbackParams) ? caseData.getRespondent1().getPartyName() : caseData.getRespondent2().getPartyName();
    }

    protected String getReferenceTemplate() {
        return "dismiss-case-defendant-notification-%s";
    }

    protected String getRecipient(CallbackParams params) {
        CaseData caseData = params.getCaseData();
        if (caseData.isRespondent1LiP() && StringUtils.isNotBlank(caseData.getRespondent1().getPartyEmail())) {
            return caseData.getRespondent1().getPartyEmail();
        } else {
            return isForRespondent1(params) ? caseData.getRespondentSolicitor1EmailAddress() : caseData.getRespondentSolicitor2EmailAddress();
        }
    }

    protected boolean isBilingual(CallbackParams params) {
        return params.getCaseData().isRespondentResponseBilingual();
    }

    protected boolean isLiP(CallbackParams params) {
        return params.getCaseData().isRespondent1LiP();
    }

    private boolean isForRespondent1(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(CaseEvent.NOTIFY_DEFENDANT_DISMISS_CASE.name());
    }
}
