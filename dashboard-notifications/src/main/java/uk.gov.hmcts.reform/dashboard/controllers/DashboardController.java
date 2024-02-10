package uk.gov.hmcts.reform.dashboard.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;
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
    public ResponseEntity<Optional<List<TaskListEntity>>> getTaskListByCaseIdentifierAndRole(
        @PathVariable("ccd-case-identifier") String ccdCaseIdentifier,
        @PathVariable("role-type") String roleType,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info(
            "Received ccd-case-identifier: {}, role-type : {}",
            ccdCaseIdentifier, roleType
        );

        var taskListResponse = taskListService.getTaskList(ccdCaseIdentifier, roleType);

        return new ResponseEntity<>(taskListResponse, HttpStatus.OK);
    }

    @GetMapping(path = {"notifications/{uuid}"})
    public ResponseEntity<Optional<NotificationEntity>> getDashboardNotificationByUuid(
        @PathVariable("uuId") UUID uuid,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info(
            "Received UUID: {}",
            uuid
        );

        var notificationResponse = dashboardNotificationService.getNotification(uuid);

        return new ResponseEntity<>(notificationResponse, HttpStatus.OK);
    }

    @PostMapping(path = "/scenarios/{scenario_ref}/{unique_case_identifier}")
    public void recordScenario(
        @PathVariable("unique_case_identifier") String uniqueCaseIdentifier,
        @PathVariable("scenario_ref") String scenarioReference,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @Valid @RequestBody ScenarioRequestParams scenarioRequestParams
    ) {
        log.info("Recording scenario {} for {}", uniqueCaseIdentifier, scenarioReference);
        dashboardScenariosService.recordScenarios(authorisation, scenarioReference,
                                                  uniqueCaseIdentifier, scenarioRequestParams);
    }
}
