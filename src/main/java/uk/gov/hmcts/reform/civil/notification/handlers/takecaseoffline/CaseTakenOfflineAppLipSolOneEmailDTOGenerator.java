package uk.gov.hmcts.reform.civil.notification.handlers.takecaseoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.Map;

@Component
public class CaseTakenOfflineAppLipSolOneEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "case-taken-offline-applicant-notification-%s";
    private final NotificationsProperties notificationsProperties;

    public CaseTakenOfflineAppLipSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
            ? notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate()
            : notificationsProperties.getClaimantLipClaimUpdatedTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        properties.put(CLAIMANT_NAME, caseData.getApplicant1().getPartyName());
        properties.put(CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData));
        return properties;
    }
}
