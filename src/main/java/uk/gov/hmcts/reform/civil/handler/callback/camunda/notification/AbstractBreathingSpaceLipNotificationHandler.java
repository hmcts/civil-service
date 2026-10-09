package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantEmail;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractBreathingSpaceLipNotificationHandler extends CallbackHandler implements NotificationData {

    protected final NotificationService notificationService;
    protected final NotificationsProperties notificationsProperties;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::notifyLipParty
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isApplicantEvent(callbackParams) ? getApplicantTaskId() : getRespondentTaskId();
    }

    protected CallbackResponse notifyLipParty(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean applicantEvent = isApplicantEvent(callbackParams);
        String recipient = applicantEvent
            ? getApplicantEmail(caseData, true)
            : caseData.getRespondent1Email();

        if (StringUtils.isBlank(recipient)) {
            log.info("Skipping breathing space LiP notification for case {} — no email address",
                     caseData.getLegacyCaseReference());
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        notificationService.sendMail(
            recipient,
            getTemplateId(caseData, applicantEvent),
            addProperties(caseData, applicantEvent),
            String.format(getReferenceTemplate(), caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    protected String getTemplateId(CaseData caseData, boolean applicantEvent) {
        if (applicantEvent) {
            return caseData.isClaimantBilingual()
                ? getApplicantWelshTemplateId()
                : getApplicantTemplateId();
        }
        return caseData.isRespondentResponseBilingual()
            ? getRespondentWelshTemplateId()
            : getRespondentTemplateId();
    }

    protected Map<String, String> addProperties(CaseData caseData, boolean applicantEvent) {
        HashMap<String, String> properties = new HashMap<>();
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        properties.put(PARTY_NAME, applicantEvent
            ? getPartyNameBasedOnType(caseData.getApplicant1())
            : getPartyNameBasedOnType(caseData.getRespondent1()));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return addProperties(caseData, true);
    }

    protected abstract boolean isApplicantEvent(CallbackParams callbackParams);

    protected abstract String getApplicantTaskId();

    protected abstract String getRespondentTaskId();

    protected abstract String getReferenceTemplate();

    protected abstract String getApplicantTemplateId();

    protected abstract String getApplicantWelshTemplateId();

    protected abstract String getRespondentTemplateId();

    protected abstract String getRespondentWelshTemplateId();
}
