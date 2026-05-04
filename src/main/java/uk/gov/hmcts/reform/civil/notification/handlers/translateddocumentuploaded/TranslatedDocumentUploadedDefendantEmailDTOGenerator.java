package uk.gov.hmcts.reform.civil.notification.handlers.translateddocumentuploaded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Component
public class TranslatedDocumentUploadedDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "translated-document-uploaded-defendant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected TranslatedDocumentUploadedDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
            ? notificationsProperties.getNotifyDefendantTranslatedDocumentUploaded()
            : notificationsProperties.getRespondent1LipClaimUpdatedTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(PARTY_NAME, caseData.getRespondent1().getPartyName());
        properties.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
        properties.put(RESPONDENT_NAME, caseData.getRespondent1().getPartyName());
        return properties;
    }
}
