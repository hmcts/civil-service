package uk.gov.hmcts.reform.dashboard.services;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dashboard.data.Notification;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationActionEntity;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationActionRepository;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getNotification;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getNotificationEntityList;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getNotificationList;

@ExtendWith(MockitoExtension.class)
public class DashboardNotificationServiceTest {

    @Mock
    private DashboardNotificationsRepository dashboardNotificationsRepository;
    @Mock
    private NotificationActionRepository notificationActionRepository;

    @Mock
    private IdamApi idamApi;

    @InjectMocks
    private DashboardNotificationService dashboardNotificationService;

    private final UUID id = UUID.randomUUID();

    @Nested
    class GetTests {
        @Test
        void shouldReturnEmpty_whenNotificationListIsNotPresent() {
            when(dashboardNotificationsRepository.findByReferenceAndCitizenRole(any(), any()))
                .thenReturn(List.of());
            List<Notification> actual = dashboardNotificationService
                .getNotifications("123", "Claimant");

            assertThat(actual).isEqualTo(List.of());
        }

        @Test
        void should_return_notifications_for_case_and_role() {
            when(dashboardNotificationsRepository.findByReferenceAndCitizenRole(any(), any()))
                .thenReturn(getNotificationEntityList());
            List<Notification> actual = dashboardNotificationService
                .getNotifications("123", "Claimant");

            assertThat(actual).isEqualTo(getNotificationList());
        }

        @Test
        void should_return_notification_for_notification_id() {
            UUID notificationId = UUID.randomUUID();
            DashboardNotificationsEntity expected = getNotification(notificationId);
            when(dashboardNotificationsRepository.findById(notificationId))
                .thenReturn(Optional.of(expected));

            Optional<DashboardNotificationsEntity> actual = dashboardNotificationService.getNotification(notificationId);

            assertThat(actual).isPresent().isEqualTo(Optional.of(expected));
        }

        @Test
        void should_return_all_ga_notifications() {
            List<DashboardNotificationsEntity> expectedList = getNotificationEntityList();
            when(dashboardNotificationsRepository.findByReferenceAndCitizenRole(any(), any())).thenReturn(expectedList);
            List<String> gaCaseIds = new ArrayList<>();
            gaCaseIds.add("123");
            gaCaseIds.add("234");
            Map<String, List<Notification>> notificationslist = dashboardNotificationService
                .getAllCasesNotifications(gaCaseIds, "Claimant");

            assertThat(notificationslist.get("123")).isEqualTo(getNotificationList());
            assertThat(notificationslist.get("234")).isEqualTo(getNotificationList());
            assertThat(notificationslist.size()).isEqualTo(2);
        }

        @Test
        void should_return_all_notification() {
            List<DashboardNotificationsEntity> expectedList = getNotificationEntityList();
            when(dashboardNotificationsRepository.findAll()).thenReturn(expectedList);

            List<DashboardNotificationsEntity> actual = dashboardNotificationService.getAll();

            assertThat(actual).isNotEmpty().isEqualTo(expectedList);
        }
    }

    @Nested
    class DeleteTests {

        @Test
        void shouldReturnOkWhenDeletingEntity() {
            //when
            dashboardNotificationService.deleteById(id);

            //then
            verify(dashboardNotificationsRepository).deleteById(id);
        }

        @Test
        void deleteAllNotificationsToClaimant() {
            String reference = "reference";
            String claimant = "CLAIMANT";
            dashboardNotificationService.deleteByReferenceAndCitizenRole(reference, claimant);
            Mockito.verify(dashboardNotificationsRepository).deleteByReferenceAndCitizenRole(reference, claimant);
        }

        @Test
        void deleteAllNotificationsToDefendant() {
            String reference = "reference";
            String defendant = "DEFENDANT";
            dashboardNotificationService.deleteByReferenceAndCitizenRole(reference, defendant);
            Mockito.verify(dashboardNotificationsRepository).deleteByReferenceAndCitizenRole(reference, defendant);
        }
    }

    @Nested
    class RecordClickOnNotification {

        @Test
        void shouldReturnOkWhenRecordingNotificationClick() {
            String authToken = "Auth-token";

            when(idamApi.retrieveUserDetails(authToken))
                .thenReturn(UserDetails.builder().forename("Claimant").surname("user").build());

            DashboardNotificationsEntity notification = getNotification(id);
            when(dashboardNotificationsRepository.findById(id)).thenReturn(Optional.of(notification));

            NotificationActionEntity notificationAction = NotificationActionEntity.builder()
                .reference(notification.getReference())
                .dashboardNotification(notification)
                .actionPerformed("Click")
                .createdBy(idamApi.retrieveUserDetails(authToken).getFullName())
                .createdAt(any())
                .build();
            notification.setNotificationAction(notificationAction);

            //when
            dashboardNotificationService.recordClick(id, authToken);

            //verify
            verify(dashboardNotificationsRepository).save(notification);
        }
    }
}
