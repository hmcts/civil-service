package uk.gov.hmcts.reform.civil.notification.handlers.caseproceedsincaseman;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
public class CaseProceedsInCasemanClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_CLAIMANT = "case-proceeds-in-caseman-applicant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected CaseProceedsInCasemanClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual() ? notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate() :
            notificationsProperties.getClaimantLipClaimUpdatedTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_CLAIMANT;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIMANT_NAME, caseData.getApplicant1().getPartyName());
        return properties;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return caseData.isLipvLROneVOne() ? Boolean.TRUE : Boolean.FALSE;
    }
}
