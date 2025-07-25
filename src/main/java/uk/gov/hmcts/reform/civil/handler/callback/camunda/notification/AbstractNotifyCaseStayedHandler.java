package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@RequiredArgsConstructor
public abstract class AbstractNotifyCaseStayedHandler extends CallbackHandler implements NotificationData {

    protected final NotificationService notificationService;
    protected final NotificationsProperties notificationsProperties;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    public CallbackResponse sendNotification(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        notificationService.sendMail(
            getRecipient(callbackParams),
            getNotificationTemplate(caseData),
            addPropertiesAll(callbackParams),
            String.format(getReferenceTemplate(), caseData.getCcdCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    protected abstract String getReferenceTemplate();

    protected abstract String getRecipient(CallbackParams callbackParams);

    protected String getNotificationTemplate(CaseData caseData) {
        if (isLiP(caseData)) {
            return isBilingual(caseData)
                ? notificationsProperties.getNotifyLipUpdateTemplateBilingual()
                : notificationsProperties.getNotifyLipUpdateTemplate();
        } else {
            return notificationsProperties.getNotifyLRCaseStayed();
        }
    }

    protected abstract boolean isLiP(CaseData caseData);

    protected abstract boolean isBilingual(CaseData caseData);

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return null;
    }

    public Map<String, String> addPropertiesAll(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_NAME, getPartyName(callbackParams),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

    protected abstract String getPartyName(CallbackParams callbackParams);
}
