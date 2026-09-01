package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_APPLICANT_BREATHING_SPACE_ENTER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_RESPONDENT1_BREATHING_SPACE_ENTER;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantEmail;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Slf4j
@Service
@RequiredArgsConstructor
public class BreathingSpaceEnterLipNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_LIP_APPLICANT_BREATHING_SPACE_ENTER,
        NOTIFY_LIP_RESPONDENT1_BREATHING_SPACE_ENTER
    );

    private static final String REFERENCE_TEMPLATE = "breathing-space-enter-lip-notification-%s";
    public static final String TASK_ID_APPLICANT = "BreathingSpaceEnterNotifyLipApplicant";
    public static final String TASK_ID_RESPONDENT = "BreathingSpaceEnterNotifyLipRespondent1";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
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
        return isApplicantEvent(callbackParams) ? TASK_ID_APPLICANT : TASK_ID_RESPONDENT;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyLipParty(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean applicantEvent = isApplicantEvent(callbackParams);
        String recipient = applicantEvent
            ? getApplicantEmail(caseData, true)
            : caseData.getRespondent1Email();

        if (StringUtils.isBlank(recipient)) {
            log.info("Skipping breathing space enter LiP notification for case {} — no email address",
                     caseData.getLegacyCaseReference());
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        notificationService.sendMail(
            recipient,
            getTemplateId(caseData, applicantEvent),
            addProperties(caseData, applicantEvent),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getTemplateId(CaseData caseData, boolean applicantEvent) {
        if (applicantEvent) {
            return caseData.isClaimantBilingual()
                ? notificationsProperties.getNotifyApplicant1EnteredBreathingSpaceLipWelsh()
                : notificationsProperties.getNotifyApplicant1EnteredBreathingSpaceLip();
        }
        return caseData.isRespondentResponseBilingual()
            ? notificationsProperties.getNotifyEnteredBreathingSpaceForDefendantLipWelsh()
            : notificationsProperties.getNotifyEnteredBreathingSpaceForDefendantLip();
    }

    private Map<String, String> addProperties(CaseData caseData, boolean applicantEvent) {
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

    private boolean isApplicantEvent(CallbackParams callbackParams) {
        return NOTIFY_LIP_APPLICANT_BREATHING_SPACE_ENTER.name()
            .equals(callbackParams.getRequest().getEventId());
    }
}
