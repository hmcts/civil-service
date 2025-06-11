package uk.gov.hmcts.reform.civil.notification.handlers.discontinueclaimparties;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.*;

@Component
public class DiscontinueClaimPartiesRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;
    private static final String REFERENCE_TEMPLATE = "defendant-claim-discontinued-%s";

    public DiscontinueClaimPartiesRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                             OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        if (!getShouldNotify(caseData)) {
            return caseData.getRespondent1().getPartyEmail();
        }
        return caseData.getRespondentSolicitor1EmailAddress();
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        if (!getShouldNotify(caseData)) {
            return notificationsProperties.getNotifyClaimDiscontinuedLipTemplate();
        }
        return notificationsProperties.getNotifyClaimDiscontinuedLRTemplate();
    }

    @Override
    protected String getReferenceTemplate() { return REFERENCE_TEMPLATE; }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        properties.put(LEGAL_ORG_NAME, getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService));
        properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
        properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
        return properties;
    }
}
