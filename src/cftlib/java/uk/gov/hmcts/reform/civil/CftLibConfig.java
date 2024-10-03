package uk.gov.hmcts.reform.civil;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Component
public class CftLibConfig implements CFTLibConfigurer {

    @Override
    public void configure(CFTLib lib) throws Exception {
        lib.createRoles(
            "national-business-centre",
            "nbc-team-leader",
            "ctsc-team-leader",
            "ctsc",
            "task-supervisor",
            "cwd-user",
            "civil-national-business-centre",
            "ccd-import",
            "caseworker",
            "caseworker-civil",
            "caseworker-caa",
            "caseworker-approver",
            "prd-aac-system",
            "caseworker-civil-solicitor",
            "caseworker-civil-admin",
            "caseworker-civil-staff",
            "caseworker-civil-judge",
            "pui-case-manager",
            "pui-finance-manager",
            "pui-organisation-manager",
            "pui-user-manager",
            "pui-caa",
            "prd-admin",
            "payments",
            "caseworker-wa-task-configuration",
            "judge-profile",
            "payment-access",
            "hearing-centre-team-leader",
            "caseworker-ras-validation",
            "citizen-profile",
            "next-hearing-date-admin",
            "RES-SOL-TWO-SPEC-PROFILE",
            "legal-adviser",
            "full-access",
            "CITIZEN-DEFENDANT-PROFILE",
            "civil-administrator-basic",
            "civil-administrator-standard",
            "judge",
            "APP-SOL-SPEC-PROFILE",
            "hearing-schedule-access",
            "cui-nbc-profile",
            "RES-SOL-TWO-UNSPEC-PROFILE",
            "CITIZEN-CLAIMANT-PROFILE",
            "caseworker-civil-citizen-ui-pcqextractor",
            "caseflags-admin",
            "RES-SOL-ONE-UNSPEC-PROFILE",
            "caseworker-civil-systemupdate",
            "caseflags-viewer",
            "GS_profile",
            "hearing-centre-admin",
            "APP-SOL-UNSPEC-PROFILE",
            "RES-SOL-ONE-SPEC-PROFILE",
            "admin-access",
            "cui-admin-profile",
            "court-officer-order",
            "nbc-team-leader",
            "ctsc",
            "ctsc-team-leader"
        );

        var users = Map.of(
            "solicitor@example.com",
            List.of("caseworker",
                    "caseworker-civil",
                    "caseworker-civil-solicitor",
                    "pui-caa",
                    "pui-organisation-manager",
                    "pui-case-manager",
                    "pui-user-manager"),
            "hmcts.civil+organisation.1.superuser@gmail.com",
            List.of("caseworker",
                    "caseworker-civil",
                    "caseworker-civil-solicitor",
                    "pui-caa,pui-case-manager",
                    "pui-organisation-manager",
                    "pui-user-manager"),
            "hmcts.civil+organisation.1.solicitor.1@gmail.com",
            List.of("payments",
                    "pui-organisation-manager",
                    "payments-refund-approver",
                    "payments-refund",
                    "pui-case-manager",
                    "caseworker",
                    "caseworker-civil",
                    "caseworker-civil-solicitor")
        );

        for (var entry : users.entrySet()) {
            lib.createProfile(entry.getKey(), "CIVIL", "CIVIL", "Submitted");
        }

        var civilDefs = Files.readAllBytes(Path.of("build/ccd-def/civil-ccd-definition.xlsx"));
        lib.importDefinition(civilDefs);
    }
}
