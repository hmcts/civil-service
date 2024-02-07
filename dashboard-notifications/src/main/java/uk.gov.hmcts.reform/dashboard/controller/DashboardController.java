package uk.gov.hmcts.reform.dashboard.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.dashboard.data.NotificationEntity;
import uk.gov.hmcts.reform.dashboard.data.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.service.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.service.TaskListService;

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

    @Autowired
    public DashboardController(TaskListService taskListService, DashboardNotificationService dashboardNotificationService) {
        this.taskListService = taskListService;
        this.dashboardNotificationService = dashboardNotificationService;
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

    @GetMapping(path = {
        "notifications/{uuid}",
    })
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

    @DeleteMapping("/{unique-notification-identifier}]")
    public ResponseEntity recordClick(@PathVariable("unique-notification-identifier") UUID id) {
        log.info(
            "Received UUID for deletion: {}",
            id
        );
        dashboardNotificationService.delete(id);
        return new ResponseEntity(HttpStatus.OK);
    }

}
