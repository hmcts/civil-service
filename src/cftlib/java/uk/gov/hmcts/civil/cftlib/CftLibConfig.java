package uk.gov.hmcts.civil.cftlib;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Component
public class CftLibConfig implements CFTLibConfigurer {

    @Override
    public void configure(CFTLib lib) throws Exception {
        var users = Map.of(
            "solicitor@example.com", List.of("caseworker","caseworker-civil","caseworker-civil-solicitor","pui-caa","pui-organisation-manager","pui-case-manager","pui-user-manager"),
            "hmcts.civil+organisation.1.superuser@gmail.com", List.of("caseworker","caseworker-civil","caseworker-civil-solicitor","pui-caa,pui-case-manager","pui-organisation-manager","pui-user-manager"),
            "hmcts.civil+organisation.1.solicitor.1@gmail.com", List.of("caseworker", "caseworker-divorce", "caseworker-divorce-solicitor"),
            "TEST_JUDGE@mailinator.com", List.of("caseworker", "caseworker-divorce", "caseworker-divorce-judge"),
            "dummysystemupdate@test.com", List.of("caseworker", "caseworker-divorce", "caseworker-divorce-systemupdate"),
            "role.assignment.admin@gmail.com", List.of("caseworker"),
            "data.store.idam.system.user@gmail.com", List.of("caseworker"),
            "divorce_as_caseworker_admin@mailinator.com", List.of("caseworker-divorce", "caseworker-divorce-superuser"));

        for (var entry : users.entrySet()) {
            lib.createIdamUser(entry.getKey(), entry.getValue().toArray(new String[0]));
            lib.createProfile(entry.getKey(), "DIVORCE", "NO_FAULT_DIVORCE", "Submitted");
        }

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
            "payments"
        );

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        var json = IOUtils.toString(resourceLoader.getResource("classpath:cftlib-am-role-assignments.json")
                                        .getInputStream(), Charset.defaultCharset());
        lib.configureRoleAssignments(json);

        var civilDefs = Files.readAllBytes(Path.of("build/ccd-def/civil-ccd-definition.xlsx"));
        lib.importDefinition(civilDefs);
    }
}
