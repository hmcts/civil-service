package uk.gov.hmcts.reform.civil.notification.handlers.translatedordernoticeuploaded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
public class TranslatedOrderNoticeUploadedDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "translated-order-notice-uploaded-defendant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected TranslatedOrderNoticeUploadedDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyLiPOrderTranslatedTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return caseData.isRespondent1LiP() && caseData.isRespondentResponseBilingual();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(PARTY_NAME, caseData.getRespondent1().getPartyName());
        return properties;
    }
}
