package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@AllArgsConstructor
public abstract class AppSolOneEmailDTOGenerator extends EmailDTOGenerator {

    private final OrganisationService organisationService;

    @Override
    public String getEmailAddress(CaseData caseData) {
        return caseData.getApplicantSolicitor1UserDetailsEmail();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        return properties;
    }
}
