package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CLOSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Component
@Slf4j
public class TemplateCommonPropertiesHelper {

    public static final Set<CaseState> qmNotAllowedStates = EnumSet.of(PENDING_CASE_ISSUED, CLOSED,
                                                                       PROCEEDS_IN_HERITAGE_SYSTEM, CASE_DISMISSED);

    protected final NotificationsSignatureConfiguration configuration;
    protected final FeatureToggleService featureToggleService;

    public TemplateCommonPropertiesHelper(NotificationsSignatureConfiguration configuration, FeatureToggleService featureToggleService) {
        this.configuration = configuration;
        this.featureToggleService = featureToggleService;
    }

    public Map<String, String> addCommonFooterSignature(Map<String, String> properties) {
        properties.putAll(Map.of(HMCTS_SIGNATURE, configuration.getHmctsSignature(),
                                 PHONE_CONTACT, configuration.getPhoneContact(),
                                 OPENING_HOURS, configuration.getOpeningHours()));
        return properties;
    }

    public Map<String, String> addCommonFooterSignatureWelsh(Map<String, String> properties) {
        properties.putAll(Map.of(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature(),
                                 WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact(),
                                 WELSH_OPENING_HOURS, configuration.getWelshOpeningHours()));
        return properties;
    }

    public Map<String, String> addSpecAndUnspecContact(CaseData caseData, Map<String, String> properties) {
        if (isQueryManagementAllowedForLRCase(caseData)) {
            properties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
        } else {
            properties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
        }
        return properties;
    }

    public Map<String, String> addLipContact(CaseData caseData, Map<String, String> properties) {
        if (isQueryManagementAllowedForLipCase(caseData)) {
            properties.put(LIP_CONTACT, configuration.getRaiseQueryLip());
        } else {
            properties.put(LIP_CONTACT, configuration.getLipContactEmail());
        }
        return properties;
    }

    public Map<String, String> addLipContactWelsh(CaseData caseData, Map<String, String> properties) {
        if (isQueryManagementAllowedForLipCase(caseData)) {
            properties.put(LIP_CONTACT_WELSH, configuration.getRaiseQueryLipWelsh());
        } else {
            properties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
        }
        return properties;
    }

    public Map<String, String> addCnbcContact(CaseData caseData, Map<String, String> properties) {
        if (isQueryManagementAllowedForLRCase(caseData)) {
            properties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
        } else {
            properties.put(CNBC_CONTACT, configuration.getCnbcContact());
        }
        return properties;
    }

    private boolean queryNotAllowedCaseStates(CaseData caseData) {
        return qmNotAllowedStates.contains(caseData.getCcdState());
    }

    public boolean isQueryManagementAllowedForLRCase(CaseData caseData) {
        return featureToggleService.isQueryManagementLRsEnabled()
            && !queryNotAllowedCaseStates(caseData)
            && !caseData.isLipCase();
    }

    public boolean isQueryManagementAllowedForLipCase(CaseData caseData) {
        return featureToggleService.isLipQueryManagementEnabled(caseData)
            && featureToggleService.isQueryManagementLRsEnabled()
            && !queryNotAllowedCaseStates(caseData)
            && caseData.isLipCase();
    }

    public Map<String, String> addBaseProperties(CaseData caseData, Map<String, String> properties) {
        properties.putAll(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        return properties;
    }
}
