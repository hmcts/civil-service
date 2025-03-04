package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

public class QueryNotificationUtils {

    public static final String UNSUPPORTED_ROLE_ERROR = "Unsupported case role for query management.";

    public static String getEmail(CaseData caseData, List<String> roles) {
        if (isApplicantSolicitor(roles)) {
            return caseData.getApplicantSolicitor1UserDetails().getEmail();
        } else if (isRespondentSolicitorOne(roles)) {
            return caseData.getRespondentSolicitor1EmailAddress();
        } else if (isRespondentSolicitorTwo(roles)) {
            return caseData.getRespondentSolicitor2EmailAddress();
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_ROLE_ERROR);
        }
    }

    public static String getLegalOrganizationName(String id, CaseData caseData,
                                                  OrganisationService organisationService) {

        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

    public static Map<String, String> getProperties(CaseData caseData, List<String> roles,
                                                    Map<String, String> properties,
                                                    OrganisationService organisationService) {
        if (isApplicantSolicitor(roles)) {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationName(
                caseData.getApplicant1OrganisationPolicy()
                    .getOrganisation().getOrganisationID(),
                caseData, organisationService));
        } else if (isRespondentSolicitorOne(roles)) {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationName(
                caseData.getRespondent1OrganisationPolicy()
                    .getOrganisation().getOrganisationID(), caseData, organisationService));
        } else if (isRespondentSolicitorTwo(roles)) {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationName(
                caseData.getRespondent2OrganisationPolicy()
                    .getOrganisation().getOrganisationID(), caseData, organisationService));
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_ROLE_ERROR);
        }
        return properties;
    }
}
