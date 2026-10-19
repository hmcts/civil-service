package uk.gov.hmcts.reform.civil.cftlib;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.setup.CamundaDefinitionImporter;
import uk.gov.hmcts.reform.civil.setup.CamundaImportConfiguration;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Seeds the local CCD stack for civil: roles, the users civil's tests sign in as, the CIVIL and GENERALAPPLICATION
 * definitions, and civil's business processes in the cftlib Camunda engine.
 */
@Slf4j
@Component
public class CivilCftlibConfig implements CFTLibConfigurer {

    static final String SERVICE_URL = "http://localhost:4000";
    static final String CAMUNDA_URL = "http://localhost:8999";

    // Roles the definitions grant access to, as HighLevelDataSetupApp adds them in preview and AAT, plus the
    // IDAM roles professional users hold.
    private static final String[] ROLES = {
        "caseworker-civil",
        "caseworker-approver",
        "prd-admin",
        "caseworker-civil-admin",
        "caseworker-civil-solicitor",
        "caseworker-civil-judge",
        "caseworker-civil-staff",
        "caseworker-civil-systemupdate",
        "caseworker-caa",
        "judge-profile",
        "basic-access",
        "ga-basic-access",
        "GS_profile",
        "legal-adviser",
        "caseworker-ras-validation",
        "admin-access",
        "full-access",
        "hearing-schedule-access",
        "civil-administrator-standard",
        "civil-administrator-basic",
        "civil-administrator-judge",
        "APP-SOL-UNSPEC-PROFILE",
        "APP-SOL-SPEC-PROFILE",
        "RES-SOL-ONE-UNSPEC-PROFILE",
        "RES-SOL-ONE-SPEC-PROFILE",
        "RES-SOL-TWO-UNSPEC-PROFILE",
        "RES-SOL-TWO-SPEC-PROFILE",
        "payment-access",
        "caseflags-admin",
        "caseflags-viewer",
        "caseworker-wa-task-configuration",
        "CITIZEN-CLAIMANT-PROFILE",
        "CITIZEN-DEFENDANT-PROFILE",
        "cui-admin-profile",
        "cui-nbc-profile",
        "citizen-profile",
        "citizen",
        "caseworker-civil-citizen-ui-pcqextractor",
        "judge",
        "hearing-centre-admin",
        "national-business-centre",
        "hearing-centre-team-leader",
        "next-hearing-date-admin",
        "court-officer-order",
        "APPLICANT-PROFILE-SPEC",
        "RESPONDENT-ONE-PROFILE-SPEC",
        "nbc-team-leader",
        "ctsc",
        "ctsc-team-leader",
        "caseworker-civil-doc-removal",
        "caseworker-civil-system-field-reader",
        "caseworker-civil-rparobot",
        "wlu-admin",
        "caseworker",
        "pui-case-manager",
        "pui-organisation-manager",
        "pui-user-manager",
        "pui-finance-manager",
        "pui-caa",
        "payments"
    };

    private static final String[] SOLICITOR = {
        "caseworker", "caseworker-civil", "caseworker-civil-solicitor", "pui-case-manager", "pui-organisation-manager",
        "pui-user-manager", "pui-finance-manager", "pui-caa", "payments"
    };

    // The professional users civil's Playwright tests sign in as (civil-ccd-definition playwright-e2e/config/users).
    private static final Map<String, String[]> USERS = Map.ofEntries(
        Map.entry("hmcts.civil+organisation.1.solicitor.1@gmail.com", SOLICITOR),
        Map.entry("hmcts.civil+organisation.1.solicitor.2@gmail.com", SOLICITOR),
        Map.entry("hmcts.civil+organisation.1.superuser@gmail.com", SOLICITOR),
        Map.entry("hmcts.civil+organisation.2.solicitor.1@gmail.com", SOLICITOR),
        Map.entry("hmcts.civil+organisation.2.superuser@gmail.com", SOLICITOR),
        Map.entry("hmcts.civil+organisation.3.solicitor.1@gmail.com", SOLICITOR),
        Map.entry("hmcts.civil+organisation.3.superuser@gmail.com", SOLICITOR),
        Map.entry("civil.damages.claims+organisation.1.solicitor.1@gmail.com", SOLICITOR),
        Map.entry("civil.damages.claims+organisation.2.solicitor.1@gmail.com", SOLICITOR),
        Map.entry("civil-system-update@mailnesia.com", new String[] {
            "caseworker", "caseworker-civil", "caseworker-civil-systemupdate", "caseworker-caa", "caseworker-approver",
            "next-hearing-date-admin", "prd-admin"
        }),
        Map.entry("civil.prd.admin@example.com", new String[] {"caseworker", "prd-admin"}),
        Map.entry("caseworker-caa@mailnesia.com", new String[] {"caseworker", "caseworker-caa"})
    );

    @Override
    public void configure(CFTLib lib) throws Exception {
        lib.createRoles(ROLES);
        USERS.forEach((email, roles) -> {
            lib.createIdamUser(email, roles);
            lib.createProfile(email, "CIVIL", "CIVIL", "CASE_ISSUED");
        });

        File definitions = new File(System.getenv("CIVIL_CFTLIB_CCD_DEFINITIONS_DIR"));
        Map<String, String> substitutions = Map.of("CCD_DEF_CASE_SERVICE_BASE_URL", SERVICE_URL);
        for (String caseType : List.of("civil", "generalapplication")) {
            // The variants preview excludes: production-only sheets and the shuttered authorisations.
            lib.importJsonDefinition(new File(definitions, caseType), null, substitutions,
                "*-prod.json", "AuthorisationCaseType-shuttered.json");
        }

        deployBusinessProcesses();
    }

    private void deployBusinessProcesses() throws Exception {
        awaitCamunda();
        var configuration = CamundaImportConfiguration.fromEnvironment(
            Map.of(
                "CAMUNDA_BASE_URL", CAMUNDA_URL,
                "S2S_URL_BASE", "http://localhost:" + System.getenv().getOrDefault("RSE_LIB_S2S_PORT", "8489"),
                "S2S_SECRET", "AABBCCDDEEFFGGHH"
            ),
            Path.of(System.getenv("CIVIL_CFTLIB_CAMUNDA_DIR"))
        );
        new CamundaDefinitionImporter(configuration).importDefinitions();
    }

    private static void awaitCamunda() throws InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(CAMUNDA_URL + "/engine-rest/engine")).build();
        Instant deadline = Instant.now().plus(Duration.ofMinutes(5));
        while (Instant.now().isBefore(deadline)) {
            try {
                if (client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200) {
                    return;
                }
            } catch (java.io.IOException e) {
                log.info("Waiting for Camunda at {}", CAMUNDA_URL);
            }
            Thread.sleep(2000);
        }
        throw new IllegalStateException("Camunda did not start at " + CAMUNDA_URL);
    }
}
