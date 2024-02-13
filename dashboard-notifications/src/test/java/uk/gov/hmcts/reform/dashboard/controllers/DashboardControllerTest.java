package uk.gov.hmcts.reform.dashboard.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.dashboard.model.Notification;
import uk.gov.hmcts.reform.dashboard.model.TaskList;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getNotificationList;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getTaskListList;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private TaskListService taskListService;

    @Mock
    private DashboardNotificationService dashboardNotificationService;

    @InjectMocks
    private DashboardController dashboardController;

    private static final String AUTHORISATION = "Bearer: aaa";

    @Test
    public void shouldReturnTaskListForCaseReferenceAndRole() {

        List<TaskList> taskList = getTaskListList();
        //given
        when(taskListService.getTaskList(any(),any()))
            .thenReturn(taskList);

        //when
        ResponseEntity<List<TaskList>> output = dashboardController.getTaskListByCaseIdentifierAndRole("123", "Claimant", AUTHORISATION);

        //then
        verify(taskListService).getTaskList("123", "Claimant");
        assertThat(output.getBody()).isEqualTo(taskList);
    }

    @Test
    public void shouldReturnNotificationsForCaseReferenceAndRole() {

        List<Notification> notifications = getNotificationList();
        //given
        when(dashboardNotificationService.getNotifications(any(),any()))
            .thenReturn(notifications);

        //when
        ResponseEntity<List<Notification>> output = dashboardController.getNotificationsByCaseIdentifierAndRole("123", "Claimant", AUTHORISATION);

        //then
        assertThat(output.getBody()).isEqualTo(notifications);
    }

}
