package uk.gov.hmcts.reform.dashboard.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dashboard.model.Notification;
import uk.gov.hmcts.reform.dashboard.model.TaskList;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getNotificationEntityList;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getNotificationList;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getTaskListEntityList;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getTaskListList;

@ExtendWith(MockitoExtension.class)
class DashboardNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private DashboardNotificationService dashboardNotificationService;


    @Test
    void shouldReturnEmpty_whenNotificationListIsNotPresent() {
        when(notificationRepository.findByReferenceAndCitizenRole(any(), any())).thenReturn(List.of());
        List<Notification> actual = dashboardNotificationService.getNotifications("123", "Claimant"
        );

        assertThat(actual).isEqualTo(List.of());
    }

    @Test
    void shouldReturnTaskList_whenTaskListIsPresent() {
        when(notificationRepository.findByReferenceAndCitizenRole(any(), any())).thenReturn(getNotificationEntityList());
        List<Notification> actual = dashboardNotificationService.getNotifications("123","Claimant"
        );

        assertThat(actual).isEqualTo(getNotificationList());
    }

}
