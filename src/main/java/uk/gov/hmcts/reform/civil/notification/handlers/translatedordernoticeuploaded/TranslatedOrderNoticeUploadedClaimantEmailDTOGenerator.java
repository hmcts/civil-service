package uk.gov.hmcts.reform.civil.notification.handlers.translatedordernoticeuploaded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
public class TranslatedOrderNoticeUploadedClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "translated-order-notice-uploaded-claimant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected TranslatedOrderNoticeUploadedClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
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
        return caseData.isApplicantLiP() && caseData.isClaimantBilingual();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(PARTY_NAME, caseData.getApplicant1().getPartyName());
        return properties;
    }
}
