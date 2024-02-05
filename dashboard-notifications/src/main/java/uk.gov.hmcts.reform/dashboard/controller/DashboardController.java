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

import uk.gov.hmcts.reform.dashboard.data.TaskListEntity;
//import uk.gov.hmcts.reform.dashboard.service.NotificationService;
import uk.gov.hmcts.reform.dashboard.service.NotificationTemplateService;
import uk.gov.hmcts.reform.dashboard.service.TaskListService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(
    path = "/dashboard",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class DashboardController {

    private final TaskListService taskListService;
    private final NotificationTemplateService notificationTemplateService;

    @Autowired
    public DashboardController(TaskListService taskListService, NotificationTemplateService notificationTemplateService) {
        this.taskListService = taskListService;
        this.notificationTemplateService = notificationTemplateService;
    }

    @GetMapping(path = {
        "taskList/{uuid}",
    })
    public ResponseEntity<Optional<TaskListEntity>> getTaskListByUuid(
        @PathVariable("uuid") UUID uuid,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info(
            "Received UUID: {}",
            uuid
        );

        var taskListResponse = taskListService.getTaskList(uuid);

        return new ResponseEntity<>(taskListResponse, HttpStatus.OK);
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

        //var notificationResponse = notificationTemplateService.getById(uuId);

        //return new ResponseEntity<>(notificationResponse, HttpStatus.OK);
        return null;
    }


}
