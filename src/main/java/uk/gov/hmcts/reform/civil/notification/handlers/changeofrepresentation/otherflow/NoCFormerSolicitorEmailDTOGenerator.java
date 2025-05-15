package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.otherflow;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.common.NotificationHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
@AllArgsConstructor
public class NoCFormerSolicitorEmailDTOGenerator extends EmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;
    private final NoCHelper noCHelper;

    @Override
    public boolean getShouldNotify(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getOrganisationToRemoveID() != null;
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return NotificationHelper.getPreviousSolicitorEmail(caseData);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNoticeOfChangeFormerSolicitor();
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
