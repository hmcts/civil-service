package uk.gov.hmcts.reform.dashboard.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.dashboard.data.NotificationEntity;
import uk.gov.hmcts.reform.dashboard.data.TaskItemTemplateEntity;

import uk.gov.hmcts.reform.dashboard.service.NotificationService;
import uk.gov.hmcts.reform.dashboard.service.TaskListService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(
    path = "/dashboard",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class DashboardController {

    private final TaskListService taskListService;

    @Autowired
    public TaskListController(TaskListService taskListService) {
        this.taskListService = taskListService;
    }

    public DashboardController() {
    }

    @GetMapping(path = {
        "taskList/{caseId}",
    })
    public ResponseEntity<List<TaskItemTemplateEntity>> getCaseId(
        @PathVariable("caseId") Long caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info(
            "Received CaseId: {}",
            caseId
        );

        var taskListResponse = taskListService.getTaskList(caseId);

        return new ResponseEntity<>(taskListResponse, HttpStatus.OK);
    }

    private final NotificationService notificationService;
    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(path = {
        "notifications/{caseId}",
    })
    public ResponseEntity<List<NotificationEntity>> getCaseId(
        @PathVariable("uuId") UUID uuId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info(
            "Received CaseId: {}",
            uuId
        );

        var notificationResponse = notificationService.getById(uuId);

        return new ResponseEntity<>(notificationResponse, HttpStatus.OK);
    }


}
