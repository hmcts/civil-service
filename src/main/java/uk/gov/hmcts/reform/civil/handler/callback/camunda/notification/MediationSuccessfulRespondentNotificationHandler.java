package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_LIP;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_2_LR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class MediationSuccessfulRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private final OrganisationDetailsService organisationDetailsService;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL,
        NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_LIP,
        NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_2_LR
    );

    private static final String REFERENCE_TEMPLATE = "mediation-successful-respondent-notification-%s";
    private static final String LOG_MEDIATION_SUCCESSFUL_DEFENDANT_LIP = "notification-mediation-successful-defendant-LIP-%s";
    private static final String LOG_MEDIATION_SUCCESSFUL_DEFENDANT_TWO_V_ONE_LR = "notification-mediation-successful-defendant-2v1-LR-%s";
    private static final String LOG_MEDIATION_SUCCESSFUL_DEFENDANT_LR = "notification-mediation-successful-defendant-LR-%s";
    private static final String TASK_ID_MEDIATION_SUCCESSFUL_DEFENDANT_LIP = "SendMediationSuccessfulDefendantLip";
    private static final String TASK_ID_MEDIATION_SUCCESSFUL_DEFENDANT_1_LR = "SendMediationSuccessfulDefendant1LR";
    private static final String TASK_ID_MEDIATION_SUCCESSFUL_DEFENDANT_2_LR = "SendMediationSuccessfulDefendant2LR";

    private final Map<String, Callback> callbacksMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondent
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbacksMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        if (isRespondentLipNotification(callbackParams)) {
            return TASK_ID_MEDIATION_SUCCESSFUL_DEFENDANT_LIP;
        } else if (isRespondentSolicitor1Notification(callbackParams)) {
            return TASK_ID_MEDIATION_SUCCESSFUL_DEFENDANT_1_LR;
        }
        return TASK_ID_MEDIATION_SUCCESSFUL_DEFENDANT_2_LR;
    }

    private CallbackResponse notifyRespondent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Boolean isCarmEnabled = featureToggleService.isCarmEnabledForCase(caseData);
        if (isCarmEnabled) {
            MultiPartyScenario scenario = getMultiPartyScenario(caseData);
            String claimId = caseData.getLegacyCaseReference();
            if (caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne()) {
                String referenceTemplate = String.format(LOG_MEDIATION_SUCCESSFUL_DEFENDANT_LIP, claimId);
                sendEmail(
                    caseData.getRespondent1().getPartyEmail(),
                    caseData.isRespondentResponseBilingual()
                        ? notificationsProperties.getNotifyLipSuccessfulMediationWelsh()
                        : notificationsProperties.getNotifyLipSuccessfulMediation(),
                    lipProperties(caseData),
                    referenceTemplate
                );
            } else if (scenario.equals(TWO_V_ONE)) {
                String referenceTemplate = String.format(LOG_MEDIATION_SUCCESSFUL_DEFENDANT_TWO_V_ONE_LR, claimId);
                sendEmail(
                    caseData.getRespondentSolicitor1EmailAddress(),
                    notificationsProperties.getNotifyTwoVOneDefendantSuccessfulMediation(),
                    twoVOneDefendantProperties(caseData),
                    referenceTemplate
                );
            } else {
                // LR scenarios
                String referenceTemplate = String.format(LOG_MEDIATION_SUCCESSFUL_DEFENDANT_LR, claimId);
                sendEmail(
                    setUpLrEmailAddress(callbackParams),
                    notificationsProperties.getNotifyLrDefendantSuccessfulMediation(),
                    lrDefendantProperties(caseData),
                    referenceTemplate
                );
            }
        } else if (featureToggleService.isLipVLipEnabled()
            && caseData.isLipvLROneVOne()
            && isRespondentSolicitor1Notification(callbackParams)) {
            // Lip v LR scenario
            String referenceTemplate = String.format(LOG_MEDIATION_SUCCESSFUL_DEFENDANT_LR, caseData.getLegacyCaseReference());
            sendEmail(
                setUpLrEmailAddress(callbackParams),
                notificationsProperties.getNotifyLrDefendantSuccessfulMediationForLipVLrClaim(),
                lrDefendantProperties(caseData),
                referenceTemplate
            );
        } else {
            if (caseData.isRespondent1NotRepresented() && caseData.getRespondent1().getPartyEmail() != null) {
                String referenceTemplate = String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
                notificationService.sendMail(
                    caseData.getRespondent1().getPartyEmail(),
                    addTemplate(caseData),
                    addProperties(caseData),
                    referenceTemplate
                );
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();

    }

    private void sendEmail(String targetEmail, String emailTemplate, Map<String, String> properties, String referenceTemplate) {
        notificationService.sendMail(
            targetEmail,
            emailTemplate,
            properties,
            referenceTemplate
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }

    private String addTemplate(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
            ? notificationsProperties.getNotifyRespondentLiPMediationSuccessfulTemplateWelsh() :
            notificationsProperties.getNotifyRespondentLiPMediationSuccessfulTemplate();
    }

    public Map<String, String> lrDefendantProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent1LegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }

    public Map<String, String> twoVOneDefendantProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent1LegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_NAME_ONE, caseData.getApplicant1().getPartyName(),
            CLAIMANT_NAME_TWO, caseData.getApplicant2().getPartyName(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }

    public Map<String, String> lipProperties(CaseData caseData) {
        return Map.of(
            PARTY_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString()
        );
    }

    private boolean isRespondentLipNotification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_LIP.name());
    }

    private boolean isRespondentSolicitor1Notification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL.name());
    }

    private String setUpLrEmailAddress(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return isRespondentSolicitor1Notification(callbackParams)
            ? caseData.getRespondentSolicitor1EmailAddress()
            : caseData.getRespondentSolicitor2EmailAddress();
    }
}
