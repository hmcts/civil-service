package uk.gov.hmcts.reform.dashboard.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.dashboard.data.Notification;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private static final String AUTH = "Bearer token";
    private static final String CASE_ID = "CCD-123";
    private static final String ROLE = "DEFENDANT";
    private static final UUID TASK_ID = UUID.randomUUID();
    private static final UUID NOTIFICATION_ID = UUID.randomUUID();

    @Mock
    private TaskListService taskListService;
    @Mock
    private DashboardNotificationService notificationService;
    @Mock
    private DashboardScenariosService scenariosService;

    @InjectMocks
    private DashboardController controller;

    private TaskListEntity taskListEntity;
    private DashboardNotificationsEntity notificationEntity;

    @BeforeEach
    void init() {
        taskListEntity = TaskListEntity.builder().id(TASK_ID).build();
        notificationEntity = DashboardNotificationsEntity.builder().id(NOTIFICATION_ID).build();
    }

    @Test
    void shouldReturnTaskListForCaseAndRole() {
        TaskList taskList = TaskList.builder().reference(CASE_ID).build();
        when(taskListService.getTaskList(CASE_ID, ROLE)).thenReturn(List.of(taskList));

        ResponseEntity<List<TaskList>> response = controller.getTaskListByCaseIdentifierAndRole(CASE_ID, ROLE, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(taskList);
        verify(taskListService).getTaskList(CASE_ID, ROLE);
    }

    @Test
    void shouldUpdateTaskList() {
        when(taskListService.updateTaskListItem(TASK_ID)).thenReturn(taskListEntity);

        ResponseEntity<TaskListEntity> response = controller.updateTaskList(TASK_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(taskListEntity);
        verify(taskListService).updateTaskListItem(TASK_ID);
    }

    @Test
    void shouldReturnNotificationById() {
        when(notificationService.getNotification(NOTIFICATION_ID)).thenReturn(Optional.of(notificationEntity));

        ResponseEntity<Optional<DashboardNotificationsEntity>> response =
            controller.getDashboardNotificationByUuid(NOTIFICATION_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(notificationEntity);
        verify(notificationService).getNotification(NOTIFICATION_ID);
    }

    @Test
    void shouldReturnNotificationsForCaseAndRole() {
        Notification notification = Notification.builder().id(NOTIFICATION_ID).build();
        when(notificationService.getNotifications(CASE_ID, ROLE)).thenReturn(List.of(notification));

        ResponseEntity<List<Notification>> response =
            controller.getNotificationsByCaseIdentifierAndRole(CASE_ID, ROLE, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(notification);
        verify(notificationService).getNotifications(CASE_ID, ROLE);
    }

    @Test
    void shouldReturnNotificationsForMultipleCases() {
        Notification notification = Notification.builder().id(NOTIFICATION_ID).build();
        Map<String, List<Notification>> notifications = Map.of(CASE_ID, List.of(notification));
        when(notificationService.getAllCasesNotifications(List.of(CASE_ID), ROLE)).thenReturn(notifications);

        ResponseEntity<Map<String, List<Notification>>> response =
            controller.getNotificationsByIdentifiersAndRole(new String[] {CASE_ID}, ROLE, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(notifications);
        verify(notificationService).getAllCasesNotifications(List.of(CASE_ID), ROLE);
    }

    @Test
    void shouldRecordClick() {
        ResponseEntity<Void> response = controller.recordClick(NOTIFICATION_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).recordClick(NOTIFICATION_ID, AUTH);
    }

    @Test
    void shouldDeleteNotification() {
        ResponseEntity<Void> response = controller.deleteNotification(NOTIFICATION_ID, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).deleteById(NOTIFICATION_ID);
    }

    @Test
    void shouldDeleteNotificationsByCaseAndRole() {
        ResponseEntity<Void> response = controller.deleteNotificationsForCaseIdentifierAndRole(CASE_ID, ROLE, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).deleteByReferenceAndCitizenRole(CASE_ID, ROLE);
    }

    @Test
    void shouldDeleteNotificationsByTemplate() {
        ResponseEntity<Void> response =
            controller.deleteTemplateNotificationsForCaseIdentifierAndRole(CASE_ID, ROLE, "template", AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).deleteByNameAndReferenceAndCitizenRole("template", CASE_ID, ROLE);
    }

    @Test
    void shouldRecordScenario() {
        Map<String, Object> params = new HashMap<>();
        params.put("deadline", OffsetDateTime.now());
        ScenarioRequestParams request = new ScenarioRequestParams(new HashMap<>(params));

        ResponseEntity<Void> response =
            controller.recordScenario(CASE_ID, "scenario", AUTH, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(scenariosService).recordScenarios(AUTH, "scenario", CASE_ID, request);
    }

    @Test
    void shouldMarkTasksInactiveForCaseAndRole() {
        ResponseEntity<Void> response =
            controller.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(CASE_ID, ROLE, AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(CASE_ID, ROLE);
    }

    @Test
    void shouldMarkTasksInactiveExcludingCategory() {
        ResponseEntity<Void> response = controller
            .makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(CASE_ID, ROLE, "cat", AUTH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(taskListService)
            .makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(CASE_ID, ROLE, "cat");
    }
}
