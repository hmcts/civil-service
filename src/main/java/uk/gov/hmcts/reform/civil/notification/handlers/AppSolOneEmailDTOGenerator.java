package uk.gov.hmcts.reform.civil.notification.handlers;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

public abstract class AppSolOneEmailDTOGenerator extends EmailDTOGenerator {

    private final OrganisationService organisationService;

    public AppSolOneEmailDTOGenerator(NotificationsSignatureConfiguration configuration,
                                      FeatureToggleService featureToggleService,
                                      OrganisationService organisationService) {
        super(configuration, featureToggleService);
        this.organisationService = organisationService;
    }

    @Override
    public String getEmailAddress(CaseData caseData) {
        return caseData.getApplicantSolicitor1UserDetailsEmail();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        return properties;
    }

    protected Boolean getShouldNotify(CaseData caseData) {
        return caseData.isApplicantLiP() ? Boolean.FALSE : Boolean.TRUE;
    }
}
