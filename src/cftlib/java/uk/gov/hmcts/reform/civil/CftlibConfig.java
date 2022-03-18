package uk.gov.hmcts.reform.civil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

@Component
public class CftlibConfig implements CFTLibConfigurer {

  @Override
  public void configure(CFTLib lib) throws Exception {

    lib.createIdamUser("test@user.com", "caseworker", "caseworker-civil", "caseworker-civil-solicitor");
    lib.createRoles(
      "caseworker-civil-staff",
      "caseworker-civil-admin",
      "caseworker-civil-solicitor",
      "caseworker-civil-systemupdate",
      "caseworker-caa",
      "caseworker-approver"
    );

    var def = Files.readAllBytes(Path.of("civil-ccd-definition/build/ccd-release-config/civil-ccd-prod.xlsx"));
    lib.importDefinition(def);
  }
}
