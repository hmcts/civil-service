package uk.gov.hmcts.reform.civil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.dashboard.data.Notification;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@FeignClient(name = "dashboard-api", url = "${dashboard.api.url}/dashboard", configuration =
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
    ResponseEntity<Optional<DashboardNotificationsEntity>> getDashboardNotificationByUuid(
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
    ResponseEntity<Void> recordClick(
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

    @DeleteMapping(path = {
        "notifications/{ccd-case-identifier}/role/{role-type}"
    })
    ResponseEntity<Void> deleteNotificationsForCaseIdentifierAndRole(
        @PathVariable("ccd-case-identifier") String ccdCaseIdentifier,
        @PathVariable("role-type") String roleType,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @PutMapping(path = {
        "taskList/{ccd-case-identifier}/role/{role-type}/status"
    }, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
        @PathVariable("ccd-case-identifier") String ccdCaseIdentifier,
        @PathVariable("role-type") String roleType,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @PutMapping(path = {
        "taskList/{ccd-case-identifier}/role/{role-type}/status/{category}"
    }, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
        @PathVariable("ccd-case-identifier") String ccdCaseIdentifier,
        @PathVariable("role-type") String roleType,
        @PathVariable("category") String category,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

}
