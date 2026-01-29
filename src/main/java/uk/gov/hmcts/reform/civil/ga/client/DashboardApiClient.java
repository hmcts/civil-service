package uk.gov.hmcts.reform.civil.ga.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import javax.validation.Valid;

@FeignClient(name = "dashboard-api", url = "${dashboard.api.url}/dashboard", configuration =
    FeignClientProperties.FeignClientConfiguration.class)
public interface DashboardApiClient {

    @PostMapping(path = "/scenarios/{scenario_ref}/{unique_case_identifier}")
    ResponseEntity<Void> recordScenario(
        @PathVariable("unique_case_identifier") String uniqueCaseIdentifier,
        @PathVariable("scenario_ref") String scenarioReference,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @Valid @RequestBody ScenarioRequestParams scenarioRequestParams
    );

    @DeleteMapping(path = "notifications/{ccd-case-identifier}/role/{role-type}")
    ResponseEntity<Void> deleteNotificationsForCaseIdentifierAndRole(
        @PathVariable("ccd-case-identifier") String ccdCaseIdentifier,
        @PathVariable("role-type") String roleType,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @DeleteMapping(path = "notifications/{ccd-case-identifier}/role/{role-type}/{template-name}")
    ResponseEntity<Void> deleteTemplateNotificationsForCaseIdentifierAndRole(
        @PathVariable("ccd-case-identifier") String ccdCaseIdentifier,
        @PathVariable("role-type") String roleType,
        @PathVariable("template-name") String templateName,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );
}
