package uk.gov.hmcts.reform.civil.client;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.dashboard.data.Notification;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@FeignClient(name = "dashboard-api", url = "${dashboard.api.url}", configuration =
    FeignClientProperties.FeignClientConfiguration.class)
public interface DashboardApiClient {

    @GetMapping(path = {
        "taskList/{ccd-case-identifier}/role/{role-type}",
    })
    ResponseEntity<List<TaskList>> getTaskListByCaseIdentifierAndRole(
        @PathVariable("ccd-case-identifier") String ccdCaseIdentifier,
        @PathVariable("role-type") String roleType,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @GetMapping(path = {
        "notifications/{uuid}",
    })
    ResponseEntity<Optional<NotificationEntity>> getDashboardNotificationByUuid(
        @PathVariable("uuid") UUID uuid,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @GetMapping(path = {
        "notifications/{ccd-case-identifier}/role/{role-type}",
    })
    ResponseEntity<List<Notification>> getNotificationsByCaseIdentifierAndRole(
        @PathVariable("ccd-case-identifier") String ccdCaseIdentifier,
        @PathVariable("role-type") String roleType,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @DeleteMapping(path = {
        "notifications/{unique-notification-identifier}"
    })
    ResponseEntity recordClick(
        @PathVariable("unique-notification-identifier") UUID id,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @PostMapping(path = "/scenarios/{scenario_ref}/{unique_case_identifier}")
    ResponseEntity<Void> recordScenario(
        @PathVariable("unique_case_identifier") String uniqueCaseIdentifier,
        @PathVariable("scenario_ref") String scenarioReference,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @Valid @RequestBody ScenarioRequestParams scenarioRequestParams
    );

}
