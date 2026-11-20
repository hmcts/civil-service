package uk.gov.hmcts.reform.civil.cftlib;

import uk.gov.hmcts.rse.ccd.lib.api.IdamUser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The users civil's tests sign in as, one of each kind of civil user, for the local stack's IDAM simulator. The login
 * page lists them by kind, and every one has the password 'password'.
 *
 * <p>They are the users of civil-ccd-definition's CodeceptJS and Playwright suites and of
 * bin/shared/add-org-roles-to-users.sh, which gives them their organisational roles in AAT and preview; locally
 * {@link OrganisationalRoles} reads the same script. Users the suites name that the script gives no roles take the
 * roles of the user in it they stand in for. The fee-paid judge, a kind of user no one in the script is, has roles of
 * its own.
 *
 * <p>In AAT their IDAM roles are set up by hand, so these are chosen to give each kind of user the access civil's
 * definitions grant it, and the role names ExUI reads a user's category from: judicial users hold a role naming
 * "judge", admin users one naming "admin", CTSC users "ctsc", and legal operations neither.
 */
final class CivilTestUsers {

    static final String SOLICITORS = "Solicitors";
    static final String JUDGES = "Judges";
    static final String HEARING_CENTRE_ADMINS = "Hearing centre admins";
    static final String LEGAL_OPERATIONS = "Legal operations";
    static final String CTSC = "CTSC";
    static final String NATIONAL_BUSINESS_CENTRE = "National Business Centre";
    static final String WELSH_LANGUAGE_UNIT = "Welsh Language Unit";
    static final String CITIZENS = "Citizens";
    static final String SYSTEM = "System and support";

    private static final String[] SOLICITOR = {
        "caseworker", "caseworker-civil", "caseworker-civil-solicitor", "pui-case-manager", "pui-organisation-manager",
        "pui-user-manager", "pui-finance-manager", "pui-caa", "payments"
    };
    private static final String[] JUDGE = {"caseworker", "caseworker-civil", "caseworker-civil-judge"};
    private static final String[] ADMIN = {"caseworker", "caseworker-civil", "caseworker-civil-admin"};
    private static final String[] LEGAL_OPS = {"caseworker", "caseworker-civil"};
    private static final String[] CTSC_STAFF = {"caseworker", "caseworker-civil", "ctsc"};
    private static final String[] CITIZEN = {"citizen"};

    static final String SYSTEM_UPDATE = "civil-system-update@mailnesia.com";

    private static final List<IdamUser> USERS = List.of(
        // civil-ccd-definition playwright-e2e/config/users/exui-users.ts and e2e/config.js
        user(SOLICITORS, "Claimant", "Solicitor", "hmcts.civil+organisation.1.solicitor.1@gmail.com", SOLICITOR),
        user(SOLICITORS, "Claimant Solicitor", "Bulk Claims", "hmcts.civil+organisation.1.solicitor.2@gmail.com",
             SOLICITOR),
        user(SOLICITORS, "Claimant Organisation", "Superuser", "hmcts.civil+organisation.1.superuser@gmail.com",
             SOLICITOR),
        user(SOLICITORS, "Defendant Solicitor", "One", "hmcts.civil+organisation.2.solicitor.1@gmail.com", SOLICITOR),
        user(SOLICITORS, "Defendant One Organisation", "Superuser", "hmcts.civil+organisation.2.superuser@gmail.com",
             SOLICITOR),
        user(SOLICITORS, "Defendant Solicitor", "Two", "hmcts.civil+organisation.3.solicitor.1@gmail.com", SOLICITOR),
        user(SOLICITORS, "Defendant Two Organisation", "Superuser", "hmcts.civil+organisation.3.superuser@gmail.com",
             SOLICITOR),
        user(SOLICITORS, "Other Solicitor", "One", "civil.damages.claims+organisation.1.solicitor.1@gmail.com",
             SOLICITOR),
        user(SOLICITORS, "Other Solicitor", "Two", "civil.damages.claims+organisation.2.solicitor.1@gmail.com",
             SOLICITOR),

        user(JUDGES, "Circuit Judge", "Region 1", "4917924EMP-@ejudiciary.net", JUDGE),
        user(JUDGES, "Amy", "Powell", "DJ.Amy.Powell@ejudiciary.net", JUDGE),
        user(JUDGES, "Circuit Judge", "Region 2", "EMP42506@ejudiciary.net", JUDGE),
        user(JUDGES, "Judge", "Region 3", "4924221EMP-@ejudiciary.net", JUDGE),
        user(JUDGES, "Judge", "Region 4", "4924246EMP-@ejudiciary.net", JUDGE),
        user(JUDGES, "Second Judge", "Region 4", "4925359EMP-@ejudiciary.net", JUDGE),
        user(JUDGES, "Angel", "Morgan", "DJ.Angel.Morgan@ejudiciary.net", JUDGE),
        user(JUDGES, "Local", "Judge", "judge-civil-02@example.com", JUDGE),
        user(JUDGES, "Leadership", "Judge", "EMP47622@ejudiciary.net", JUDGE),
        user(JUDGES, "Fee-paid", "Judge", "49932114EMP-@ejudiciary.net", JUDGE),

        user(HEARING_CENTRE_ADMINS, "Civil", "Admin", "civil-admin@mailnesia.com", ADMIN),
        user(HEARING_CENTRE_ADMINS, "Hearing Centre Admin", "Region 1", "hearing_center_admin_reg1@justice.gov.uk",
             ADMIN),
        user(HEARING_CENTRE_ADMINS, "Hearing Centre Admin", "Region 2", "hearing_center_admin_reg2@justice.gov.uk",
             ADMIN),
        user(HEARING_CENTRE_ADMINS, "Hearing Centre Admin", "Region 3", "hearing_center_admin_reg3@justice.gov.uk",
             ADMIN),
        user(HEARING_CENTRE_ADMINS, "Hearing Centre Admin", "Region 4", "hearing_center_admin_reg4@justice.gov.uk",
             ADMIN),
        user(HEARING_CENTRE_ADMINS, "Hearing Centre Admin", "Region 4 (CodeceptJS)",
             "hearing_center_admin_region4@justice.gov.uk", ADMIN),
        user(HEARING_CENTRE_ADMINS, "Hearing Centre Admin", "Region 5", "hearing_center_admin_reg5@justice.gov.uk",
             ADMIN),
        user(HEARING_CENTRE_ADMINS, "Hearing Centre Admin", "Region 6", "hearing_center_admin_reg6@justice.gov.uk",
             ADMIN),
        user(HEARING_CENTRE_ADMINS, "Hearing Centre Admin", "Region 7", "hearing_center_admin_reg7@justice.gov.uk",
             ADMIN),
        user(HEARING_CENTRE_ADMINS, "Local Hearing Centre", "Admin", "hearing-centre-admin-01@example.com", ADMIN),

        user(LEGAL_OPERATIONS, "Legal Caseworker", "Region 1", "tribunal_legal_caseworker_reg1@justice.gov.uk",
             LEGAL_OPS),
        user(LEGAL_OPERATIONS, "Legal Caseworker", "Region 2", "tribunal_legal_caseworker_reg2@justice.gov.uk",
             LEGAL_OPS),
        user(LEGAL_OPERATIONS, "Legal Caseworker", "Region 3", "tribunal_legal_caseworker_reg3@justice.gov.uk",
             LEGAL_OPS),
        user(LEGAL_OPERATIONS, "Legal Caseworker", "Region 4", "tribunal_legal_caseworker_reg4@justice.gov.uk",
             LEGAL_OPS),
        user(LEGAL_OPERATIONS, "Legal Caseworker", "Region 5", "tribunal_legal_caseworker_reg5@justice.gov.uk",
             LEGAL_OPS),
        user(LEGAL_OPERATIONS, "Legal Caseworker", "Region 6", "tribunal_legal_caseworker_reg6@justice.gov.uk",
             LEGAL_OPS),
        user(LEGAL_OPERATIONS, "Legal Caseworker", "Region 7", "tribunal_legal_caseworker_reg7@justice.gov.uk",
             LEGAL_OPS),
        user(LEGAL_OPERATIONS, "Legal Caseworker", "National",
             "tribunal_legal_caseworker_national@justice.gov.uk", LEGAL_OPS),
        user(LEGAL_OPERATIONS, "GA Legal Caseworker", "National",
             "ga_tribunal_legal_caseworker_national@justice.gov.uk", LEGAL_OPS),
        user(LEGAL_OPERATIONS, "Senior Legal Caseworker", "Region 4", "seniorcivil_tbcw_region4@justice.gov.uk",
             LEGAL_OPS),
        user(LEGAL_OPERATIONS, "Senior Tribunal Caseworker", "Region 4",
             "sr_tribunal_caseworker_region4@justice.gov.uk", LEGAL_OPS),
        user(LEGAL_OPERATIONS, "Local Legal", "Caseworker", "tribunal-caseworker-01@example.com", LEGAL_OPS),

        user(CTSC, "CTSC", "Admin", "ctsc_admin@justice.gov.uk", CTSC_STAFF),
        user(CTSC, "CTSC Team Leader", "Region 4", "ctsc_team_leader_region4@justice.gov.uk", CTSC_STAFF),
        user(CTSC, "GA CTSC Team Leader", "National", "ga_ctsc_team_leader_national@justice.gov.uk", CTSC_STAFF),

        user(NATIONAL_BUSINESS_CENTRE, "NBC Admin", "Region 1", "nbc_admin_region1@justice.gov.uk", ADMIN),
        user(NATIONAL_BUSINESS_CENTRE, "NBC Admin", "Region 2", "nbc_admin_region2@justice.gov.uk", ADMIN),
        user(NATIONAL_BUSINESS_CENTRE, "NBC Admin", "Region 4", "nbc_admin_region4@justice.gov.uk", ADMIN),
        user(NATIONAL_BUSINESS_CENTRE, "NBC Team Leader", "Region 1", "nbc_team_lead_reg1@justice.gov.uk", ADMIN),
        user(NATIONAL_BUSINESS_CENTRE, "NBC Team Leader", "Region 4", "nbc_team_leader_region4@justice.gov.uk",
             ADMIN),
        user(NATIONAL_BUSINESS_CENTRE, "GA NBC Admin", "National", "ga_nbc_admin_national@justice.gov.uk", ADMIN),
        user(NATIONAL_BUSINESS_CENTRE, "GA NBC Team Leader", "National",
             "ga_nbc_team_leader_national@justice.gov.uk", ADMIN),
        user(NATIONAL_BUSINESS_CENTRE, "Local NBC", "Team Leader", "nbc-team-leader@mailnesia.com", ADMIN),

        user(WELSH_LANGUAGE_UNIT, "WLU", "Caseworker", "casewrokerWLU@justice.gov.uk", ADMIN),
        user(WELSH_LANGUAGE_UNIT, "WLU", "Team Leader", "caseworkerwlutl@justice.gov.uk", ADMIN),

        // Citizens' journeys create their own users. These are the ones e2e/config.js names for local testing.
        user(CITIZENS, "Claimant", "Citizen", "civilmoneyclaimsdemo@gmail.com", CITIZEN),
        user(CITIZENS, "Defendant", "Citizen", "civiluatdefendant@gmail.com", CITIZEN),

        user(SYSTEM, "Civil", "System Update", SYSTEM_UPDATE,
             "caseworker", "caseworker-civil", "caseworker-civil-systemupdate", "caseworker-caa",
             "caseworker-approver", "next-hearing-date-admin", "prd-admin"),
        user(SYSTEM, "PRD", "Admin", "civil.prd.admin@example.com", "caseworker", "prd-admin"),
        user(SYSTEM, "Case Access", "Administrator", "caseworker-caa@mailnesia.com", "caseworker", "caseworker-caa")
    );

    /**
     * The users the suites name that the script gives no roles, and the user in the script each stands in for.
     */
    private static final Map<String, String> STAND_INS = Map.ofEntries(
        Map.entry("judge-civil-02@example.com", "DJ.Amy.Powell@ejudiciary.net"),
        Map.entry("4925359EMP-@ejudiciary.net", "4924246EMP-@ejudiciary.net"),
        // A circuit and leadership judge.
        Map.entry("EMP47622@ejudiciary.net", "4917924EMP-@ejudiciary.net"),
        Map.entry("civil-admin@mailnesia.com", "hearing_center_admin_reg1@justice.gov.uk"),
        Map.entry("hearing-centre-admin-01@example.com", "hearing_center_admin_reg1@justice.gov.uk"),
        Map.entry("hearing_center_admin_region4@justice.gov.uk", "hearing_center_admin_reg4@justice.gov.uk"),
        Map.entry("tribunal-caseworker-01@example.com", "tribunal_legal_caseworker_reg1@justice.gov.uk"),
        Map.entry("tribunal_legal_caseworker_national@justice.gov.uk",
                  "ga_tribunal_legal_caseworker_national@justice.gov.uk"),
        // Senior tribunal caseworkers, as the script's region 4 legal caseworker is.
        Map.entry("seniorcivil_tbcw_region4@justice.gov.uk", "tribunal_legal_caseworker_reg4@justice.gov.uk"),
        Map.entry("sr_tribunal_caseworker_region4@justice.gov.uk", "tribunal_legal_caseworker_reg4@justice.gov.uk"),
        // A CTSC team leader, as the script's CTSC admin is.
        Map.entry("ctsc_team_leader_region4@justice.gov.uk", "ctsc_admin@justice.gov.uk"),
        Map.entry("nbc_admin_region4@justice.gov.uk", "nbc_admin_region1@justice.gov.uk"),
        // NBC team leaders at region 1's location, as the script's is.
        Map.entry("nbc_team_lead_reg1@justice.gov.uk", "nbc_team_leader_region4@justice.gov.uk"),
        Map.entry("nbc-team-leader@mailnesia.com", "nbc_team_leader_region4@justice.gov.uk")
    );

    /**
     * Roles no user in the script has, for the suites' users that need them.
     */
    private static final Map<String, List<Map<String, Object>>> OWN_ROLES = Map.of(
        // civil's definitions give fee-paid judges their own access; no other test user is one.
        "49932114EMP-@ejudiciary.net", List.of(
            organisational("hmcts-judiciary", "BASIC", "JUDICIAL", Map.of("primaryLocation", "20262")),
            organisational("fee-paid-judge", "STANDARD", "JUDICIAL", Map.of(
                "jurisdiction", "CIVIL", "primaryLocation", "20262",
                "workTypes", "hearing_work,decision_making_work,applications")),
            organisational("hearing-viewer", "STANDARD", "JUDICIAL", Map.of(
                "jurisdiction", "CIVIL", "primaryLocation", "20262"))
        )
    );

    private CivilTestUsers() {
    }

    static List<IdamUser> all() {
        return USERS;
    }

    static boolean isCitizen(IdamUser user) {
        return CITIZENS.equals(user.quickLoginLabel());
    }

    /**
     * Each user's organisational roles: those the script gives them, or those of the user they stand in for. The
     * script and this list are kept in step: a user in the script without a login here fails startup.
     */
    static Map<String, List<Map<String, Object>>> organisationalRoles(
        Map<String, List<Map<String, Object>>> fromScript) {
        List<String> emails = USERS.stream().map(IdamUser::email).toList();
        List<String> missing = fromScript.keySet().stream().filter(email -> !emails.contains(email)).toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("bin/shared/add-org-roles-to-users.sh gives roles to users "
                + "CivilTestUsers doesn't create: " + missing);
        }
        return USERS.stream()
            .flatMap(user -> rolesOf(user.email(), fromScript).stream().map(roles -> Map.entry(user.email(), roles)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (first, second) -> first, LinkedHashMap::new));
    }

    private static Optional<List<Map<String, Object>>> rolesOf(String email,
                                                                Map<String, List<Map<String, Object>>> fromScript) {
        if (OWN_ROLES.containsKey(email)) {
            return Optional.of(OWN_ROLES.get(email));
        }
        return Stream.of(email, STAND_INS.get(email))
            .filter(Objects::nonNull)
            .map(fromScript::get)
            .filter(Objects::nonNull)
            .findFirst();
    }

    private static Map<String, Object> organisational(String roleName, String grantType, String roleCategory,
                                                      Map<String, String> attributes) {
        return Map.of(
            "roleType", "ORGANISATION",
            "roleName", roleName,
            "classification", "PUBLIC",
            "grantType", grantType,
            "roleCategory", roleCategory,
            "readOnly", false,
            "attributes", attributes,
            "authorisations", List.of()
        );
    }

    private static IdamUser user(String label, String forename, String surname, String email, String... roles) {
        return IdamUser.quickLogin(label, forename, surname, email, roles);
    }
}
