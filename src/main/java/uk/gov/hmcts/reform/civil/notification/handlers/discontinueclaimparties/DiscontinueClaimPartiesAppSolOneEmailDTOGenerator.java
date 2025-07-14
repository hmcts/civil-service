package uk.gov.hmcts.reform.civil.notification.handlers.discontinueclaimparties;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Component
public class DiscontinueClaimPartiesAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;
    private static final String REFERENCE_TEMPLATE = "claimant-claim-discontinued-%s";

    public DiscontinueClaimPartiesAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                            OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyClaimDiscontinuedLRTemplate();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(LEGAL_ORG_NAME, getApplicantLegalOrganizationName(caseData, organisationService));
        return properties;
    }

}
