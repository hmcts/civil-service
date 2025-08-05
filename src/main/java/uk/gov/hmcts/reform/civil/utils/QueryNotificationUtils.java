package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getUserRoleForQuery;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPClaimant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Slf4j
public class QueryNotificationUtils {

    public static final String UNSUPPORTED_ROLE_ERROR = "Unsupported case role for query management.";
    public static final String EMAIL = "EMAIL";
    public static final String LEGAL_ORG = "LEGAL_ORG";
    public static final String LIP_NAME = "LIP_NAME";
    public static final String IS_LIP_OTHER_PARTY = "IS_LIP_OTHER_PARTY";
    public static final String IS_LIP_OTHER_PARTY_WELSH = "IS_LIP_OTHER_PARTY_WELSH";

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
        } else if (isLIPClaimant(roles)) {
            return caseData.getApplicant1Email();
        } else if (isLIPDefendant(roles)) {
            return caseData.getDefendantUserDetails().getEmail();
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
            properties.put(
                CLAIM_LEGAL_ORG_NAME_SPEC,
                getApplicantLegalOrganizationName(caseData, organisationService)
            );
        } else if (isRespondentSolicitorOne(roles)) {
            properties.put(
                CLAIM_LEGAL_ORG_NAME_SPEC,
                getLegalOrganizationNameForRespondent(caseData, true, organisationService)
            );
        } else if (isRespondentSolicitorTwo(roles)) {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC,
                           getLegalOrganizationNameForRespondent(caseData, false, organisationService));
        } else if (isLIPClaimant(roles)) {
            properties.put(PARTY_NAME, caseData.getApplicant1().getPartyName());
        } else if (isLIPDefendant(roles)) {
            properties.put(PARTY_NAME, caseData.getRespondent1().getPartyName());
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_ROLE_ERROR);
        }
        return properties;
    }

    //ToDo: Remove this and all its usages after public queries release.
    public static List<Map<String, String>> getOtherPartyEmailDetails(
        CaseData caseData, OrganisationService organisationService,
        CoreCaseUserService coreCaseUserService, String queryId) {
        List<String> roles = getUserRoleForQuery(caseData, coreCaseUserService, queryId);
        return getOtherPartyRecipientList(caseData, roles, organisationService);
    }

    public static List<Map<String, String>> getOtherPartyEmailDetailsPublicQuery(
        CaseData caseData, OrganisationService organisationService,
        CoreCaseUserService coreCaseUserService, String queryId) {
        List<String> roles = getUserRoleForQuery(caseData, coreCaseUserService, queryId);
        if (caseData.isLipCase()) {
            String otherParty = getOtherParty(caseData, roles);
            return getOtherPartyLipOnCaseRecipientList(caseData, otherParty, organisationService);
        } else {
            return getOtherPartyRecipientList(caseData, roles, organisationService);
        }
    }

    private static String getOtherParty(CaseData caseData, List<String> roles) {
        if (isApplicantSolicitor(roles)) {
            log.info("Applicant solicitor found");
            return caseData.getRespondent1Represented() != YesOrNo.NO ? "lrDefendant" : "lipRespondent";
        }
        if (isRespondentSolicitorOne(roles) || isRespondentSolicitorTwo(roles)) {
            log.info("Respondent solicitor found");
            return caseData.getApplicant1Represented() != YesOrNo.NO ? "lrApplicant" : "lipApplicant";
        }
        if (isLIPClaimant(roles)) {
            log.info("LIP claimant found");
            return caseData.getRespondent1Represented() != YesOrNo.NO ? "lrDefendant" : "lipRespondent";
        }
        if (isLIPDefendant(roles)) {
            log.info("LIP defendant found");
            return caseData.getApplicant1Represented() != YesOrNo.NO ? "lrApplicant" : "lipApplicant";
        }
        throw new IllegalArgumentException(UNSUPPORTED_ROLE_ERROR);
    }

    private static List<Map<String, String>> getOtherPartyLipOnCaseRecipientList(CaseData caseData, String otherParty, OrganisationService organisationService) {
        List<Map<String, String>> emailDetailsList = new ArrayList<>();

        String email;
        String lipName;
        String legalOrgName;
        String lipOtherPartyWelsh;

        switch (otherParty) {
            case "lipApplicant":
                email = caseData.getApplicant1Email();
                lipName = caseData.getApplicant1().getPartyName();
                lipOtherPartyWelsh = caseData.isClaimantBilingual() ? "WELSH" : "NON_WELSH";
                emailDetailsList.add(createLipOnCaseEmailDetails(email, lipName, lipOtherPartyWelsh));
                break;
            case "lipRespondent":
                if (ofNullable(caseData.getRespondent1().getPartyEmail()).isEmpty()) {
                    break;
                }
                email = caseData.getRespondent1().getPartyEmail();
                lipName = caseData.getRespondent1().getPartyName();
                lipOtherPartyWelsh = caseData.isRespondentResponseBilingual() ? "WELSH" : "NON_WELSH";
                emailDetailsList.add(createLipOnCaseEmailDetails(email, lipName, lipOtherPartyWelsh));
                break;
            case "lrApplicant":
                email = caseData.getApplicantSolicitor1UserDetails().getEmail();
                legalOrgName = getApplicantLegalOrganizationName(caseData, organisationService);
                emailDetailsList.add(createEmailDetails(email, legalOrgName));
                break;
            case "lrDefendant":
                if (getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                    if (ofNullable(caseData.getRespondentSolicitor1EmailAddress()).isEmpty()) {
                        break;
                    }
                    emailDetailsList.add(createEmailDetails(
                        caseData.getRespondentSolicitor1EmailAddress(),
                        getLegalOrganizationNameForRespondent(caseData, true, organisationService)
                    ));
                    if (ofNullable(caseData.getRespondentSolicitor2EmailAddress()).isEmpty()) {
                        break;
                    }
                    emailDetailsList.add(createEmailDetails(
                        caseData.getRespondentSolicitor2EmailAddress(),
                        getLegalOrganizationNameForRespondent(caseData, false, organisationService)
                    ));
                } else {
                    if (ofNullable(caseData.getRespondentSolicitor1EmailAddress()).isEmpty()) {
                        break;
                    }
                    email = caseData.getRespondentSolicitor1EmailAddress();
                    legalOrgName = getLegalOrganizationNameForRespondent(caseData, true, organisationService);
                    emailDetailsList.add(createEmailDetails(email, legalOrgName));
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown party type: " + otherParty);
        }

        return emailDetailsList;
    }

    private static List<Map<String, String>> getOtherPartyRecipientList(CaseData caseData, List<String> roles,
                                                                        OrganisationService organisationService) {
        List<Map<String, String>> emailDetailsList = new ArrayList<>();
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
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
                if (isApplicantSolicitor(roles)) {
                    emailDetailsList.add(createEmailDetails(
                        caseData.getRespondentSolicitor1EmailAddress(),
                        getLegalOrganizationNameForRespondent(caseData, true, organisationService)
                    ));
                    emailDetailsList.add(createEmailDetails(
                        caseData.getRespondentSolicitor2EmailAddress(),
                        getLegalOrganizationNameForRespondent(caseData, false, organisationService)
                    ));
                } else if (isRespondentSolicitorOne(roles)) {
                    emailDetailsList.add(createEmailDetails(
                        caseData.getApplicantSolicitor1UserDetails().getEmail(),
                        getApplicantLegalOrganizationName(caseData, organisationService)
                    ));
                    emailDetailsList.add(createEmailDetails(
                        caseData.getRespondentSolicitor2EmailAddress(),
                        getLegalOrganizationNameForRespondent(caseData, false, organisationService)
                    ));
                } else if (isRespondentSolicitorTwo(roles)) {
                    emailDetailsList.add(createEmailDetails(
                        caseData.getApplicantSolicitor1UserDetails().getEmail(),
                        getApplicantLegalOrganizationName(caseData, organisationService)
                    ));
                    emailDetailsList.add(createEmailDetails(
                        caseData.getRespondentSolicitor1EmailAddress(),
                        getLegalOrganizationNameForRespondent(caseData, true, organisationService)
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
        details.put(IS_LIP_OTHER_PARTY, "FALSE");
        return details;
    }

    private static  Map<String, String> createLipOnCaseEmailDetails(String email, String lipName, String lipOtherPartyWelsh) {
        Map<String, String> details = new HashMap<>();
        details.put(EMAIL, email);
        details.put(LIP_NAME, lipName);
        details.put(IS_LIP_OTHER_PARTY, "TRUE");
        details.put(IS_LIP_OTHER_PARTY_WELSH, lipOtherPartyWelsh);
        return details;
    }

}
