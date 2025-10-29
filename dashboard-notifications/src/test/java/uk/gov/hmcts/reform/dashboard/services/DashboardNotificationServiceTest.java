package uk.gov.hmcts.reform.dashboard.services;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

        @Test
        void should_sort_notifications_by_creation_time_descending() {
            UUID earlierId = UUID.randomUUID();
            UUID laterId = UUID.randomUUID();
            DashboardNotificationsEntity earlier = DashboardNotificationsEntity.builder()
                .id(earlierId)
                .createdAt(OffsetDateTime.now().minusDays(1))
                .build();
            DashboardNotificationsEntity later = DashboardNotificationsEntity.builder()
                .id(laterId)
                .createdAt(OffsetDateTime.now())
                .build();

            when(dashboardNotificationsRepository.findByReferenceAndCitizenRole("case", "role"))
                .thenReturn(List.of(earlier, later));

            List<Notification> notifications = dashboardNotificationService.getNotifications("case", "role");

            assertThat(notifications).extracting(Notification::getId).containsExactly(laterId, earlierId);
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

        @Test
        void deleteNotificationsByNameAndReference() {
            String templateName = "template.name";
            String reference = "reference";
            dashboardNotificationService.deleteByNameAndReference(templateName, reference);
            Mockito.verify(dashboardNotificationsRepository).deleteByNameAndReference(templateName, reference);
        }
    }

    @Test
    public void saveOrUpdate() {

        DashboardNotificationsEntity notification1 = createDashboardNotificationsEntity();
        DashboardNotificationsEntity notification2 = createDashboardNotificationsEntity();
        when(
            dashboardNotificationsRepository
                .findByReferenceAndCitizenRoleAndName(
                    any(), any(), any())).thenReturn(List.of(notification1, notification2));
        DashboardNotificationsEntity notification = DashboardNotificationsEntity.builder()
            .id(UUID.randomUUID())
            .name("template.name")
            .reference("reference")
            .citizenRole("CLAIMANT")
            .build();
        dashboardNotificationService.saveOrUpdate(notification);
        verify(notificationActionRepository, times(2)).deleteByDashboardNotificationAndActionPerformed(any(DashboardNotificationsEntity.class), any());
        final ArgumentCaptor<DashboardNotificationsEntity> captor = ArgumentCaptor.forClass(DashboardNotificationsEntity.class);
        verify(dashboardNotificationsRepository).save(captor.capture());
        assertThat(captor.getValue()).isNotNull();
        assertThat(captor.getValue().getId()).isEqualTo(notification1.getId());
    }

    @Test
    void saveOrUpdateShouldPersistWithoutTemplateLookupWhenNameMissing() {
        DashboardNotificationsEntity notification = DashboardNotificationsEntity.builder()
            .id(UUID.randomUUID())
            .reference("reference")
            .citizenRole("CLAIMANT")
            .build();

        when(dashboardNotificationsRepository.save(notification)).thenReturn(notification);

        DashboardNotificationsEntity saved = dashboardNotificationService.saveOrUpdate(notification);

        assertThat(saved).isSameAs(notification);
        verify(dashboardNotificationsRepository).save(notification);
        verify(dashboardNotificationsRepository, never()).findByReferenceAndCitizenRoleAndName(any(), any(), any());
        verifyNoInteractions(notificationActionRepository);
    }

    private DashboardNotificationsEntity createDashboardNotificationsEntity() {
        return DashboardNotificationsEntity.builder().id(UUID.randomUUID()).build();
    }

    @Nested
    class RecordClickOnNotification {

        @Test
        void shouldReturnOkWhenRecordingNotificationClick() {
            String authToken = "Auth-token";

            DashboardNotificationsEntity notification = getNotification(id);
            when(dashboardNotificationsRepository.findById(id)).thenReturn(Optional.of(notification));
            when(dashboardNotificationsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            UserDetails userDetails = Mockito.mock(UserDetails.class);
            when(userDetails.getFullName()).thenReturn("Claimant user");
            when(idamApi.retrieveUserDetails(authToken)).thenReturn(userDetails);

            dashboardNotificationService.recordClick(id, authToken);

            ArgumentCaptor<DashboardNotificationsEntity> captor = ArgumentCaptor.forClass(DashboardNotificationsEntity.class);
            verify(dashboardNotificationsRepository).save(captor.capture());
            NotificationActionEntity action = captor.getValue().getNotificationAction();
            assertThat(action).isNotNull();
            assertThat(action.getActionPerformed()).isEqualTo("Click");
            assertThat(action.getCreatedBy()).isEqualTo("Claimant user");
            assertThat(action.getReference()).isEqualTo(notification.getReference());
            assertThat(action.getId()).isNull();
        }

        @Test
        void shouldReuseExistingClickActionIdWhenRecordingSecondClick() {
            DashboardNotificationsEntity notification = getNotification(id);
            NotificationActionEntity existingAction = NotificationActionEntity.builder()
                .id(99L)
                .actionPerformed("Click")
                .reference(notification.getReference())
                .build();
            notification.setNotificationAction(existingAction);

            when(dashboardNotificationsRepository.findById(id)).thenReturn(Optional.of(notification));
            when(dashboardNotificationsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            String authToken = "Auth-token";
            UserDetails userDetails = Mockito.mock(UserDetails.class);
            when(userDetails.getFullName()).thenReturn("Claimant user");
            when(idamApi.retrieveUserDetails(authToken)).thenReturn(userDetails);

            dashboardNotificationService.recordClick(id, authToken);

            ArgumentCaptor<DashboardNotificationsEntity> captor = ArgumentCaptor.forClass(DashboardNotificationsEntity.class);
            verify(dashboardNotificationsRepository).save(captor.capture());
            NotificationActionEntity action = captor.getValue().getNotificationAction();
            assertThat(action.getId()).isEqualTo(99L);
            assertThat(action.getCreatedBy()).isEqualTo("Claimant user");
            assertThat(action.getActionPerformed()).isEqualTo("Click");
        }
    }

}
