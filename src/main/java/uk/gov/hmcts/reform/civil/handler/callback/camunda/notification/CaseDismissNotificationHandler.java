package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@RequiredArgsConstructor
public abstract class CaseDismissNotificationHandler extends CallbackHandler implements NotificationData {


    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::sendNotification);
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_NAME, getPartyName(caseData),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData)
        );
    }

    protected abstract String getPartyName(CaseData caseData);

    public CallbackResponse sendNotification(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        notificationService.sendMail(
            getRecipient(callbackParams),
            getNotificationTemplate(callbackParams),
            addProperties(caseData),
            String.format(getReferenceTemplate(), caseData.getCcdCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    protected abstract String getReferenceTemplate();

    protected abstract String getRecipient(CallbackParams params);

    protected String getNotificationTemplate(CallbackParams params) {
        if (isLiP(params)) {
            return isBilingual(params)
                ? notificationsProperties.getNotifyLipUpdateTemplateBilingual()
                : notificationsProperties.getNotifyLipUpdateTemplate();
        } else {
            return notificationsProperties.getNotifyLRCaseDismissed();
        }
    }

    protected abstract boolean isBilingual(CallbackParams params);

    protected abstract boolean isLiP(CallbackParams params);
}
