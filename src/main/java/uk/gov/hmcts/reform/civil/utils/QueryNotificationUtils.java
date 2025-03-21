package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getUserRoleForQuery;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

public class QueryNotificationUtils {

    public static final String UNSUPPORTED_ROLE_ERROR = "Unsupported case role for query management.";
    public static final String EMAIL = "EMAIL";
    public static final String LEGAL_ORG = "LEGAL_ORG";

    private QueryNotificationUtils() {
        //NO-OP
    }

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

    public static boolean isOtherPartyApplicant(List<String> roles) {
        return isRespondentSolicitorOne(roles) || isRespondentSolicitorTwo(roles);
    }

    public static Map<String, String> getProperties(CaseData caseData, List<String> roles,
                                                    Map<String, String> properties,
                                                    OrganisationService organisationService) {
        if (isApplicantSolicitor(roles)) {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC,
                           getApplicantLegalOrganizationName(caseData, organisationService));
        } else if (isRespondentSolicitorOne(roles)) {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC,
                           getLegalOrganizationNameForRespondent(caseData, true, organisationService));
        } else if (isRespondentSolicitorTwo(roles)) {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC,
                           getLegalOrganizationNameForRespondent(caseData, false, organisationService));
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_ROLE_ERROR);
        }
        return properties;
    }

    public static List<Map<String, String>> getOtherPartyEmailDetails(
        CaseData caseData, OrganisationService organisationService,
        CoreCaseUserService coreCaseUserService, String queryId) {

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        List<String> roles = getUserRoleForQuery(caseData, coreCaseUserService, queryId);
        List<Map<String, String>> emailDetailsList = new ArrayList<>();

        switch (multiPartyScenario) {
            case ONE_V_ONE, TWO_V_ONE, ONE_V_TWO_ONE_LEGAL_REP -> {
                // When 1v1, 2v1,  or 1v2 same solicitor, "other party" will either be applicant 1, or respondent 1
                String email = isOtherPartyApplicant(roles)
                    ? caseData.getApplicantSolicitor1UserDetails().getEmail()
                    : caseData.getRespondentSolicitor1EmailAddress();
                String legalOrgName = isOtherPartyApplicant(roles)
                    ? getApplicantLegalOrganizationName(caseData, organisationService)
                    : getLegalOrganizationNameForRespondent(caseData, true, organisationService);

                emailDetailsList.add(createEmailDetails(email, legalOrgName));
            }
            case ONE_V_TWO_TWO_LEGAL_REP -> {
                if (isOtherPartyApplicant(roles)) {
                    // 1v2 different solicitor, and when "other party" is applicant 1.
                    emailDetailsList.add(createEmailDetails(
                        caseData.getApplicantSolicitor1UserDetails().getEmail(),
                        getApplicantLegalOrganizationName(caseData, organisationService)
                    ));
                } else {
                    // 1v2 different solicitor, and when "other party" is respondent 1 AND respondent 2.
                    emailDetailsList.add(createEmailDetails(
                        caseData.getRespondentSolicitor1EmailAddress(),
                        getLegalOrganizationNameForRespondent(caseData, true, organisationService)
                    ));
                    emailDetailsList.add(createEmailDetails(
                        caseData.getRespondentSolicitor2EmailAddress(),
                        getLegalOrganizationNameForRespondent(caseData, false, organisationService)
                    ));
                }
            }
            default -> {
            }
        }
        return emailDetailsList;
    }

    private static Map<String, String> createEmailDetails(String email, String legalOrg) {
        Map<String, String> details = new HashMap<>();
        details.put(EMAIL, email);
        details.put(LEGAL_ORG, legalOrg);
        return details;
    }

}
