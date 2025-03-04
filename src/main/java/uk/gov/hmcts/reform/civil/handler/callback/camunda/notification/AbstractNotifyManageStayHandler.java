package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@RequiredArgsConstructor
public abstract class AbstractNotifyManageStayHandler extends CallbackHandler implements NotificationData {

    protected final NotificationService notificationService;
    protected final NotificationsProperties notificationsProperties;

    @Override
    public Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::sendNotification);
    }

    public CallbackResponse sendNotification(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        notificationService.sendMail(
            getRecipient(callbackParams),
            getNotificationTemplate(caseData),
            addPropertiesForStayLifted(callbackParams),
            String.format(getReferenceTemplate(), caseData.getCcdCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    protected String getNotificationTemplate(CaseData caseData) {
        if (isLiP(caseData)) {
            return isBilingual(caseData) ? notificationsProperties.getNotifyLipUpdateTemplateBilingual()
                : notificationsProperties.getNotifyLipUpdateTemplate();
        }
        return notificationsProperties.getNotifyLRStayLifted();
    }

    protected abstract String getReferenceTemplate();

    protected abstract String getRecipient(CallbackParams callbackParams);

    protected abstract boolean isBilingual(CaseData caseData);

    protected abstract boolean isLiP(CaseData caseData);

    protected Map<String, String> addPropertiesForStayLifted(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (isLiP(caseData)) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                PARTY_NAME, getPartyName(callbackParams),
                CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData)
            );
        } else {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                PARTY_NAME, getPartyName(callbackParams),
                CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference()
            );
        }
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return null;
    }

    protected abstract String getPartyName(CallbackParams callbackParams);

}
