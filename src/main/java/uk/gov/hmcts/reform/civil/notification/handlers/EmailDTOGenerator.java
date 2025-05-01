package uk.gov.hmcts.reform.civil.notification.handlers;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

public abstract class EmailDTOGenerator implements NotificationData {

    protected abstract Boolean getShouldNotify(CaseData caseData);

    public EmailDTO buildEmailDTO(CaseData caseData, String taskId) {
        Map<String, String> properties = addProperties(caseData);
        addCustomProperties(properties, caseData);
        return EmailDTO.builder()
            .targetEmail(getEmailAddress(caseData))
            .emailTemplate(getEmailTemplateId(caseData, taskId))
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

    // In some cases, the TaskId is required to determine the appropriate template.
    protected String getEmailTemplateId(CaseData caseData, String taskId) {
        return getEmailTemplateId(caseData);
    }

    protected abstract String getReferenceTemplate();

    protected abstract Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData);
}
