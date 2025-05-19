package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT2_MEDIATION_AGREEMENT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addLipContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.shouldSendMediationNotificationDefendant1LRCarm;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimantDefendantAgreedMediationRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_RESPONDENT_MEDIATION_AGREEMENT,
                                                          NOTIFY_RESPONDENT2_MEDIATION_AGREEMENT);
    private static final String REFERENCE_TEMPLATE = "mediation-agreement-respondent-notification-%s";
    public static final String TASK_ID_LIP = "ClaimantDefendantAgreedMediationNotifyRespondent";
    public static final String TASK_ID_LIP_2 = "ClaimantDefendantAgreedMediationNotifyRespondent2";
    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendantMediationAgreement
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        if (CaseEvent.valueOf(callbackParams.getRequest().getEventId())
            .equals(NOTIFY_RESPONDENT2_MEDIATION_AGREEMENT)) {
            return TASK_ID_LIP_2;
        }
        return TASK_ID_LIP;
    }

    private CallbackResponse notifyDefendantMediationAgreement(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean carmEnabled = featureToggleService.isCarmEnabledForCase(caseData);
        if (CaseEvent.valueOf(callbackParams.getRequest().getEventId())
            .equals(NOTIFY_RESPONDENT2_MEDIATION_AGREEMENT)) {
            boolean shouldNotifyRespondent2LRCarm = shouldSendMediationNotificationDefendant2LRCarm(
                caseData, carmEnabled);
            if (shouldNotifyRespondent2LRCarm) {
                notificationService.sendMail(
                    addEmailRespondent2(caseData),
                    notificationsProperties.getNotifyDefendantLRForMediation(),
                    addPropertiesRespondent2Carm(caseData),
                    String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
                );
            }
        } else {
            if (caseData.getRespondent1().getPartyEmail() != null || caseData.getRespondentSolicitor1EmailAddress() != null) {
                boolean shouldNotifyRespondent1LRCarm = shouldSendMediationNotificationDefendant1LRCarm(caseData, carmEnabled);
                notificationService.sendMail(
                    addEmail(caseData),
                    addTemplate(caseData),
                    shouldNotifyRespondent1LRCarm ? addPropertiesRespondent1Carm(caseData) : addProperties(caseData),
                    String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
                );
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();

    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference()
            ));
            addCommonFooterSignature(properties, configuration);
            addSpecAndUnspecContact(caseData, properties, configuration,
                                    featureToggleService.isQueryManagementLRsEnabled());
            addLipContact(caseData, properties, featureToggleService.isQueryManagementLRsEnabled(),
                          featureToggleService.isLipQueryManagementEnabled(caseData));
            return properties;
        } else {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                CLAIM_LEGAL_ORG_NAME_SPEC,
                getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService),
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference()
            ));
            addCommonFooterSignature(properties, configuration);
            addSpecAndUnspecContact(caseData, properties, configuration,
                                    featureToggleService.isQueryManagementLRsEnabled());
            return properties;
        }
    }

    public Map<String, String> addPropertiesRespondent1Carm(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIM_LEGAL_ORG_NAME_SPEC,
            getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService),
            CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }

    public Map<String, String> addPropertiesRespondent2Carm(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIM_LEGAL_ORG_NAME_SPEC,
            getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }

    private String addEmail(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return caseData.getRespondent1().getPartyEmail();
        } else {
            return caseData.getRespondentSolicitor1EmailAddress();
        }
    }

    private String addEmailRespondent2(CaseData caseData) {
        if (caseData.isRespondent2NotRepresented()) {
            return caseData.getRespondent2().getPartyEmail();
        } else {
            return caseData.getRespondentSolicitor2EmailAddress();
        }
    }

    private String addTemplate(CaseData caseData) {
        if (shouldSendMediationNotificationDefendant1LRCarm(caseData,
                                                featureToggleService.isCarmEnabledForCase(caseData))) {
            return notificationsProperties.getNotifyDefendantLRForMediation();
        }
        if (caseData.isRespondent1NotRepresented()) {
            return getNotifyRespondentLiPMediationAgreementTemplate(caseData);
        } else {
            return notificationsProperties.getNotifyRespondentLRMediationAgreementTemplate();
        }
    }

    private String getNotifyRespondentLiPMediationAgreementTemplate(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
            ? notificationsProperties.getNotifyRespondentLiPMediationAgreementTemplateWelsh() :
            notificationsProperties.getNotifyRespondentLiPMediationAgreementTemplate();
    }
}
