package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    public ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            OrganisationService organisationService
    ) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.organisationService = organisationService;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getRespondentSolicitorClaimContinuingOnlineForSpec();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(
            Map<String, String> properties,
            CaseData caseData
    ) {
        Map<String, String> updated = super.addCustomProperties(properties, caseData);
        updated.put(
                CLAIM_LEGAL_ORG_NAME_SPEC,
                getLegalOrganizationNameForRespondent(caseData, false, organisationService)
        );
        return updated;
    }
}
