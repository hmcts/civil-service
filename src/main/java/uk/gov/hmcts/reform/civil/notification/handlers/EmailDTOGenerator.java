package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;
import java.util.Map;

public abstract class EmailDTOGenerator implements NotificationData {

    @Autowired
    protected TemplateCommonPropertiesHelper templateCommonPropertiesHelper;

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
        HashMap<String, String> properties = new HashMap<>();
        templateCommonPropertiesHelper.addBaseProperties(caseData, properties);
        templateCommonPropertiesHelper.addCommonFooterSignature(properties);
        templateCommonPropertiesHelper.addCnbcContact(caseData, properties);
        templateCommonPropertiesHelper.addSpecAndUnspecContact(caseData, properties);
        return properties;
    }

    protected abstract String getEmailAddress(CaseData caseData);

    protected abstract String getEmailTemplateId(CaseData caseData);

    protected abstract String getReferenceTemplate();

    protected abstract Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData);
}
