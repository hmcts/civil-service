package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
@AllArgsConstructor
public class NoCOtherSolicitorOneEmailDTOGenerator extends EmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;
    private final NoCHelper noCHelper;

    @Override
    public boolean getShouldNotify(CaseData caseData) {
        return !(noCHelper.isOtherParty1Lip(caseData));
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNoticeOfChangeOtherParties();
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return noCHelper.getOtherSolicitor1Email(caseData);
    }

    @Override
    protected String getReferenceTemplate() {
        return NoCHelper.REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(noCHelper.getProperties(caseData, false));
        return properties;
    }
}
