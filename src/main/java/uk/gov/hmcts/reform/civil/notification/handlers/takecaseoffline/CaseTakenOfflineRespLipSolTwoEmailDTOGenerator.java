package uk.gov.hmcts.reform.civil.notification.handlers.takecaseoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class CaseTakenOfflineRespLipSolTwoEmailDTOGenerator extends DefendantTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "case-taken-offline-respondent-notification-%s";
    private final NotificationsProperties notificationsProperties;

    public CaseTakenOfflineRespLipSolTwoEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual() ? notificationsProperties.getNotifyDefendantTranslatedDocumentUploaded()
            : notificationsProperties.getRespondent1LipClaimUpdatedTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent2()));
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        return properties;
    }
}
