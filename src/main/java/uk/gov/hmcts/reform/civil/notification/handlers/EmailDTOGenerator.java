package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class EmailDTOGenerator implements NotificationData {

    @Autowired
    protected TemplateCommonPropertiesHelper templateCommonPropertiesHelper;

    public abstract Boolean getShouldNotify(CaseData caseData);

    public EmailDTO buildEmailDTO(CaseData caseData, String taskId) {
        Map<String, String> properties = addProperties(caseData);
        addCustomProperties(properties, caseData);
        String emailReference = String.format(getReferenceTemplate(),
                                         caseData.getLegacyCaseReference());
        log.info("buildEmailDTO for taskId: {} and email: {} and reference: {}",
                 taskId, getEmailAddress(caseData), emailReference);
        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setTargetEmail(getEmailAddress(caseData));
        emailDTO.setEmailTemplate(getEmailTemplateId(caseData, taskId));
        emailDTO.setParameters(properties);
        emailDTO.setReference(emailReference);
        return emailDTO;
    }

    public Map<String, String> addProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>();
        templateCommonPropertiesHelper.addBaseProperties(caseData, properties);
        templateCommonPropertiesHelper.addCommonFooterSignature(properties);
        templateCommonPropertiesHelper.addCnbcContact(caseData, properties);
        templateCommonPropertiesHelper.addSpecAndUnspecContact(caseData, properties);
        templateCommonPropertiesHelper.addCommonFooterSignatureWelsh(properties);
        templateCommonPropertiesHelper.addLipContact(caseData, properties);
        templateCommonPropertiesHelper.addLipContactWelsh(caseData, properties);
        return properties;
    }

    protected abstract String getEmailAddress(CaseData caseData);

    protected abstract String getEmailTemplateId(CaseData caseData);

    //In some cases, the TaskId is required to determine the appropriate template.
    protected String getEmailTemplateId(CaseData caseData, String taskId) {
        return getEmailTemplateId(caseData);
    }

    protected abstract String getReferenceTemplate();

    protected abstract Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData);
}
