package uk.gov.hmcts.reform.civil.notification.handlers.dismisscase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
public class DismissCaseDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "dismiss-case-defendant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected DismissCaseDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual() ? notificationsProperties.getNotifyLipUpdateTemplateBilingual()
            : notificationsProperties.getNotifyLipUpdateTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        return super.addCustomProperties(properties, caseData);
    }

}
