package uk.gov.hmcts.reform.civil.notification.handlers;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCnbcContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

public abstract class EmailDTOGenerator implements NotificationData {

    protected final NotificationsSignatureConfiguration configuration;
    protected final FeatureToggleService featureToggleService;

    public EmailDTOGenerator(NotificationsSignatureConfiguration configuration, FeatureToggleService featureToggleService) {
        this.configuration = configuration;
        this.featureToggleService = featureToggleService;
    }

    protected abstract Boolean getShouldNotify(CaseData caseData);

    public EmailDTO buildEmailDTO(CaseData caseData) {
        Map<String, String> properties = addProperties(caseData);
        addCustomProperties(properties, caseData);
        return EmailDTO.builder()
            .targetEmail(getEmailAddress(caseData))
            .emailTemplate(getEmailTemplateId(caseData))
            .parameters(properties)
            .reference(String.format(getReferenceTemplate(),
                caseData.getLegacyCaseReference()))
            .build();
    }

    public Map<String, String> addProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        addCnbcContact(caseData, properties, configuration,
                       featureToggleService.isQueryManagementLRsEnabled());
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }

    protected abstract String getEmailAddress(CaseData caseData);

    protected abstract String getEmailTemplateId(CaseData caseData);

    protected abstract String getReferenceTemplate();

    protected abstract Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData);
}
