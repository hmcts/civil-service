package uk.gov.hmcts.reform.civil.notification.handlers;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

public abstract class EmailDTOGenerator implements NotificationData {

    protected abstract Boolean getShouldNotify(CaseData caseData);

    protected final NotificationsProperties notificationsProperties;

    protected EmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

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
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    protected abstract String getEmailAddress(CaseData caseData);

    protected abstract String getEmailTemplateId(CaseData caseData);

    protected abstract String getReferenceTemplate();

    protected abstract Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData);
}
