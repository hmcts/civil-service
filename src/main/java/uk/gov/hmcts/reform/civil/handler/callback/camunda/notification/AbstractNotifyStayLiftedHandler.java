package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractNotifyStayLiftedHandler extends CallbackHandler implements NotificationData {

    protected final NotificationService notificationService;
    protected final NotificationsProperties notificationsProperties;

    public CallbackResponse sendNotification(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (isLiP(caseData)) { // TODO: remove when lip notification is developed
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        notificationService.sendMail(
            getRecipient(callbackParams),
            getNotificationTemplate(caseData),
            addPropertiesForStayLifted(callbackParams),
            String.format(getReferenceTemplate(), caseData.getCcdCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    protected abstract String getReferenceTemplate();

    protected abstract String getRecipient(CallbackParams callbackParams);

    protected String getNotificationTemplate(CaseData caseData) {
        if (isLiP(caseData)) {
            // TODO: add lip template
            return null;
        } else {
            return notificationsProperties.getNotifyLRStayLifted();
        }
    }

    protected abstract boolean isLiP(CaseData caseData);

    protected Map<String, String> addPropertiesForStayLifted(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_NAME, getPartyName(callbackParams)
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return null;
    }

    protected abstract String getPartyName(CallbackParams callbackParams);
}
