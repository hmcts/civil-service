package uk.gov.hmcts.reform.dashboard.controllers;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dashboard.data.Notification;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(
    path = "/dashboard",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
)
public class DashboardController {

    private final TaskListService taskListService;
    private final DashboardNotificationService dashboardNotificationService;
    private final DashboardScenariosService dashboardScenariosService;

    @Autowired
    public DashboardController(TaskListService taskListService,
                               DashboardNotificationService dashboardNotificationService,
                               DashboardScenariosService dashboardScenariosService) {
        this.taskListService = taskListService;
        this.dashboardNotificationService = dashboardNotificationService;
        this.dashboardScenariosService = dashboardScenariosService;
    }

    @GetMapping(path = {
        "taskList/{ccd-case-identifier}/role/{role-type}",
    })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<List<TaskList>> getTaskListByCaseIdentifierAndRole(
        @PathVariable("ccd-case-identifier") String ccdCaseIdentifier,
        @PathVariable("role-type") String roleType,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info(
            "Get Task Lists for ccd-case-identifier: {}, role-type : {}",
            ccdCaseIdentifier, roleType
        );

        var taskListResponse = taskListService.getTaskList(ccdCaseIdentifier, roleType);

        return new ResponseEntity<>(taskListResponse, HttpStatus.OK);
    }

    @PutMapping(path = {
        "taskList/{task-item-identifier}",
    })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<TaskListEntity> updateTaskList(
        @PathVariable("task-item-identifier") UUID taskItemIdentifier,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info("Update TaskList item for task item identifier {}", taskItemIdentifier);

        var taskListResponse = taskListService.updateTaskListItem(taskItemIdentifier);

        return new ResponseEntity<>(taskListResponse, HttpStatus.OK);
    }

    @GetMapping(path = {
        "notifications/{uuid}",
    })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<Optional<DashboardNotificationsEntity>> getDashboardNotificationByUuid(
        @PathVariable("uuid") UUID uuid,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info("Get Notification for notification identifier: {}", uuid);

        var notificationResponse = dashboardNotificationService.getNotification(uuid);

        return new ResponseEntity<>(notificationResponse, HttpStatus.OK);
    }

    @GetMapping(path = {
        "notifications/{ccd-case-identifier}/role/{role-type}",
    })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<List<Notification>> getNotificationsByCaseIdentifierAndRole(
        @PathVariable("ccd-case-identifier") String ccdCaseIdentifier,
        @PathVariable("role-type") String roleType,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info("Get notifications for ccd-case-identifier: {}, role-type : {}", ccdCaseIdentifier, roleType);

        var notificationsResponse = dashboardNotificationService.getNotifications(ccdCaseIdentifier, roleType);

        return new ResponseEntity<>(notificationsResponse, HttpStatus.OK);
    }

    @GetMapping(path = {
        "notifications/ids/{ccd-case-identifiers}/role/{role-type}",
    })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<Map<String, List<Notification>>> getNotificationsByIdentifiersAndRole(
        @PathVariable("ccd-case-identifiers") String[] ccdCaseIdentifiers,
        @PathVariable("role-type") String roleType,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info("Get notifications for ccd-case-identifiers: {}, role-type : {}", ccdCaseIdentifiers, roleType);

        var notificationsResponse = dashboardNotificationService.getAllCasesNotifications(List.of(ccdCaseIdentifiers), roleType);

        return new ResponseEntity<>(notificationsResponse, HttpStatus.OK);
    }

    @PutMapping(path = {
        "notifications/{unique-notification-identifier}"
    })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<Void> recordClick(
        @PathVariable("unique-notification-identifier") UUID id,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info("Received UUID for recording click: {}", id);
        dashboardNotificationService.recordClick(id, authorisation);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path = {
        "notifications/{unique-notification-identifier}"
    })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<Void> deleteNotification(
        @PathVariable("unique-notification-identifier") UUID id,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info("Received UUID for deleting notification: {}", id);
        dashboardNotificationService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path = {
        "notifications/{ccd-case-identifier}/role/{role-type}"
    }, consumes = MediaType.ALL_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<Void> deleteNotificationsForCaseIdentifierAndRole(
        @PathVariable("ccd-case-identifier") String ccdCaseIdentifier,
        @PathVariable("role-type") String roleType,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info("Received UUID for deleting notification for case: {} and role {}", ccdCaseIdentifier, roleType);
        dashboardNotificationService.deleteByReferenceAndCitizenRole(ccdCaseIdentifier, roleType);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(path = "/scenarios/{scenario_ref}/{unique_case_identifier}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<Void> recordScenario(
        @PathVariable("unique_case_identifier") String uniqueCaseIdentifier,
        @PathVariable("scenario_ref") String scenarioReference,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @Valid @RequestBody ScenarioRequestParams scenarioRequestParams
    ) {
        log.info("Recording scenario {} for {}", uniqueCaseIdentifier, scenarioReference);
        dashboardScenariosService.recordScenarios(authorisation, scenarioReference,
                                                  uniqueCaseIdentifier, scenarioRequestParams
        );
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = {
        "taskList/{ccd-case-identifier}/role/{role-type}/status/{category}"
    })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<Void> makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
        @PathVariable("ccd-case-identifier") String ccdCaseIdentifier,
        @PathVariable("role-type") String roleType,
        @PathVariable("category") String category,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info("Received UUID for making progress-able tasks inactive for case: {} and role {} and category {}",
                 ccdCaseIdentifier, roleType, category);
        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(ccdCaseIdentifier, roleType, category);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = {
        "taskList/{ccd-case-identifier}/role/{role-type}/status"
    })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<Void> makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
        @PathVariable("ccd-case-identifier") String ccdCaseIdentifier,
        @PathVariable("role-type") String roleType,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info("Received UUID for making progress-able tasks inactive for case: {} and role {}",
                 ccdCaseIdentifier, roleType);
        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(ccdCaseIdentifier, roleType);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
