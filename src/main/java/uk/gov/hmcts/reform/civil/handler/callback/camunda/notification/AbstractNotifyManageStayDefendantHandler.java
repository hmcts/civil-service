package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static java.util.Objects.nonNull;

public abstract class AbstractNotifyManageStayDefendantHandler extends AbstractNotifyManageStayHandler {

    public AbstractNotifyManageStayDefendantHandler(NotificationService notificationService, NotificationsProperties notificationsProperties) {
        super(notificationService, notificationsProperties);
    }

    @Override
    protected String getPartyName(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return isRespondentSolicitor2(callbackParams)
            ? caseData.getRespondent2().getPartyName()
            : caseData.getRespondent1().getPartyName();
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

    protected abstract boolean isRespondentSolicitor2(CallbackParams callbackParams);

    @Override
    protected boolean isBilingual(CaseData caseData) {
        return caseData.isRespondentResponseBilingual();
    }
}
