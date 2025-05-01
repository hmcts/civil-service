package uk.gov.hmcts.reform.dashboard.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.dashboard.data.Notification;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getNotificationList;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getTaskListList;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String CASE_ID = "SomeUniqueIdentifier";
    private static final UUID ID = UUID.randomUUID();
    public static final String NOTIFICATION_DRAFT_CLAIM_START = "notification.draft.claim.start";
    public static final ScenarioRequestParams SCENARIO_REQUEST_PARAMS = new ScenarioRequestParams(new HashMap<>(Map.of(
        "url",
        "http://testUrl",
        "status",
        "InProgress",
        "helpText",
        "Should be helpful!",
        "animal",
        "Tiger",
        "target",
        "Safari"
    )));

    @Mock
    private TaskListService taskListService;

    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    void shouldReturnTaskListForCaseReferenceAndRole() {

        //given
        List<TaskList> taskList = getTaskListList();
        when(taskListService.getTaskList(any(), any())).thenReturn(taskList);

        //when
        ResponseEntity<List<TaskList>> output = dashboardController.getTaskListByCaseIdentifierAndRole(
            "123",
            "Claimant",
            AUTHORISATION
        );

        //then
        verify(taskListService).getTaskList("123", "Claimant");
        assertThat(output.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(output.getBody()).isEqualTo(taskList);
    }

    @Test
    void shouldReturnEmptyTaskListForCaseReferenceAndRoleIfNotPresent() {

        //given
        when(taskListService.getTaskList(any(), any()))
            .thenReturn(List.of());

        //when
        ResponseEntity<List<TaskList>> output = dashboardController.getTaskListByCaseIdentifierAndRole(
            "123",
            "Claimant",
            AUTHORISATION
        );

        //then
        verify(taskListService).getTaskList("123", "Claimant");
        assertThat(output.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(output.getBody().isEmpty()).isTrue();
    }

    @Test
    void shouldThrow500ErrorForCaseReferenceAndRoleIfException() {

        //given
        when(taskListService.getTaskList(any(), any()))
            .thenThrow(new RuntimeException());

        //then
        assertThrows(RuntimeException.class, () -> dashboardController.getTaskListByCaseIdentifierAndRole(
            "123",
            "Claimant",
            AUTHORISATION
        ));
    }

    @Test
    void shouldReturnNotificationsForCaseReferenceAndRole() {

        List<Notification> notifications = getNotificationList();
        //given
        when(dashboardNotificationService.getNotifications(any(), any()))
            .thenReturn(notifications);

        //when
        ResponseEntity<List<Notification>> output = dashboardController.getNotificationsByCaseIdentifierAndRole(
            "123",
            "Claimant",
            AUTHORISATION
        );

        //then
        assertThat(output.getBody()).isEqualTo(notifications);
    }

    @Test
    void shouldReturnNotificationsForListOfCaseReferenceAndRole() {

        Map<String, List<Notification>> notificationslist = new HashMap<>();
        notificationslist.put("123", getNotificationList());
        notificationslist.put("1234", getNotificationList());

        String[] gaCaseIds = new String[]{"123", "1234"};

        //given
        when(dashboardNotificationService.getAllCasesNotifications(any(), any()))
            .thenReturn(notificationslist);

        //when
        ResponseEntity<Map<String, List<Notification>>> output = dashboardController.getNotificationsByIdentifiersAndRole(
            gaCaseIds,
            "Claimant",
            AUTHORISATION
        );

        //then
        assertThat(output.getBody()).isEqualTo(notificationslist);
    }

    @Test
    void should_create_scenario() {
        doNothing().when(dashboardScenariosService)
            .recordScenarios(AUTHORISATION, NOTIFICATION_DRAFT_CLAIM_START, CASE_ID, SCENARIO_REQUEST_PARAMS);

        final ResponseEntity responseEntity = dashboardController
            .recordScenario(CASE_ID, NOTIFICATION_DRAFT_CLAIM_START, AUTHORISATION, SCENARIO_REQUEST_PARAMS);

        assertEquals(OK, responseEntity.getStatusCode());

        verify(dashboardScenariosService)
            .recordScenarios(AUTHORISATION, NOTIFICATION_DRAFT_CLAIM_START, CASE_ID, SCENARIO_REQUEST_PARAMS);
    }

    @Test
    void shouldReturnOkWhenNotificationDeleted() {

        //when
        final ResponseEntity responseEntity = dashboardController.deleteNotification(ID, AUTHORISATION);

        //then
        assertEquals(OK, responseEntity.getStatusCode());
        verify(dashboardNotificationService).deleteById(ID);
    }

    @Test
    void shouldReturn401WhenNotificationDeletedUnauthorised() {

        //given
        doThrow(new RuntimeException()).when(dashboardNotificationService).deleteById(ID);

        //then
        assertThrows(RuntimeException.class, () -> dashboardController.deleteNotification(ID, AUTHORISATION));
    }

    @Test
    void shouldReturnOkWhenMakeProgressAbleTasksInactiveForCaseIdentifierAndRoleInvoked() {

        //when
        final ResponseEntity responseEntity = dashboardController
            .makeProgressAbleTasksInactiveForCaseIdentifierAndRole("123", "Claimant", AUTHORISATION);

        //then
        assertEquals(OK, responseEntity.getStatusCode());
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole("123", "Claimant");
    }

    @Test
    void shouldReturn401WhenMakeProgressAbleTasksInactiveForCaseIdentifierAndRoleUnauthorised() {

        //given
        doThrow(new RuntimeException()).when(taskListService)
            .makeProgressAbleTasksInactiveForCaseIdentifierAndRole("123", "Claimant");

        //then
        assertThrows(RuntimeException.class, () -> dashboardController
            .makeProgressAbleTasksInactiveForCaseIdentifierAndRole("123", "Claimant", AUTHORISATION));
    }
}
