package uk.gov.hmcts.reform.dashboard.services;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import static org.mockito.Mockito.mock;
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
            DashboardNotificationsEntity earlier = new DashboardNotificationsEntity();
            earlier.setId(earlierId);
            earlier.setCreatedAt(OffsetDateTime.now().minusDays(1));
            DashboardNotificationsEntity later = new DashboardNotificationsEntity();
            later.setId(laterId);
            later.setCreatedAt(OffsetDateTime.now());

            when(dashboardNotificationsRepository.findByReferenceAndCitizenRole("case", "role"))
                .thenReturn(List.of(earlier, later));

            List<Notification> notifications = dashboardNotificationService.getNotifications("case", "role");

            assertThat(notifications).extracting(Notification::getId).containsExactly(laterId, earlierId);
        }

        @Test
        void should_keep_getNotifications_working_when_entity_has_multiple_actions() {
            DashboardNotificationsEntity notification = getNotification(UUID.randomUUID());

            NotificationActionEntity olderAction = new NotificationActionEntity();
            olderAction.setId(1L);
            olderAction.setActionPerformed("Click");
            olderAction.setReference(notification.getReference());
            olderAction.setCreatedBy("Older user");
            olderAction.setCreatedAt(OffsetDateTime.now().minusHours(2));
            olderAction.setDashboardNotification(notification);

            NotificationActionEntity latestAction = new NotificationActionEntity();
            latestAction.setId(2L);
            latestAction.setActionPerformed("Click");
            latestAction.setReference(notification.getReference());
            latestAction.setCreatedBy("Latest user");
            latestAction.setCreatedAt(OffsetDateTime.now());
            latestAction.setDashboardNotification(notification);

            notification.setNotificationActions(List.of(olderAction, latestAction));

            when(dashboardNotificationsRepository.findByReferenceAndCitizenRole("case", "Claimant"))
                .thenReturn(List.of(notification));

            List<Notification> actual = dashboardNotificationService.getNotifications("case", "Claimant");

            assertThat(actual).hasSize(1);
            assertThat(actual.get(0).getNotificationAction()).isNotNull();
            assertThat(actual.get(0).getNotificationAction().getId()).isEqualTo(2L);
            assertThat(actual.get(0).getNotificationAction().getCreatedBy()).isEqualTo("Latest user");
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
            verify(dashboardNotificationsRepository).deleteByReferenceAndCitizenRole(reference, claimant);
        }

        @Test
        void deleteAllNotificationsToDefendant() {
            String reference = "reference";
            String defendant = "DEFENDANT";
            dashboardNotificationService.deleteByReferenceAndCitizenRole(reference, defendant);
            verify(dashboardNotificationsRepository).deleteByReferenceAndCitizenRole(reference, defendant);
        }

        @Test
        void deleteNotificationsByNameAndReference() {
            String templateName = "template.name";
            String reference = "reference";
            dashboardNotificationService.deleteByNameAndReference(templateName, reference);
            verify(dashboardNotificationsRepository).deleteByNameAndReference(templateName, reference);
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
        DashboardNotificationsEntity notification = new DashboardNotificationsEntity();
        notification.setId(UUID.randomUUID());
        notification.setName("template.name");
        notification.setReference("reference");
        notification.setCitizenRole("CLAIMANT");
        dashboardNotificationService.saveOrUpdate(notification);
        verify(notificationActionRepository, times(2)).deleteByDashboardNotificationAndActionPerformed(any(DashboardNotificationsEntity.class), any());
        final ArgumentCaptor<DashboardNotificationsEntity> captor = ArgumentCaptor.forClass(DashboardNotificationsEntity.class);
        verify(dashboardNotificationsRepository).save(captor.capture());
        assertThat(captor.getValue()).isNotNull();
        assertThat(captor.getValue().getId()).isEqualTo(notification1.getId());
    }

    @Test
    void saveOrUpdateShouldPersistWithoutTemplateLookupWhenNameMissing() {
        DashboardNotificationsEntity notification = new DashboardNotificationsEntity();
        notification.setId(UUID.randomUUID());
        notification.setReference("reference");
        notification.setCitizenRole("CLAIMANT");

        when(dashboardNotificationsRepository.save(notification)).thenReturn(notification);

        DashboardNotificationsEntity saved = dashboardNotificationService.saveOrUpdate(notification);

        assertThat(saved).isSameAs(notification);
        verify(dashboardNotificationsRepository).save(notification);
        verify(dashboardNotificationsRepository, never()).findByReferenceAndCitizenRoleAndName(any(), any(), any());
        verifyNoInteractions(notificationActionRepository);
    }

    private DashboardNotificationsEntity createDashboardNotificationsEntity() {
        DashboardNotificationsEntity notification = new DashboardNotificationsEntity();
        notification.setId(UUID.randomUUID());
        return notification;
    }

    @Nested
    class RecordClickOnNotification {

        @Test
        void shouldReturnOkWhenRecordingNotificationClick() {
            String authToken = "Auth-token";

            DashboardNotificationsEntity notification = getNotification(id);
            when(dashboardNotificationsRepository.findById(id)).thenReturn(Optional.of(notification));
            when(dashboardNotificationsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getFullName()).thenReturn("Claimant user");
            when(idamApi.retrieveUserDetails(authToken)).thenReturn(userDetails);

            dashboardNotificationService.recordClick(id, authToken);

            ArgumentCaptor<DashboardNotificationsEntity> captor = ArgumentCaptor.forClass(DashboardNotificationsEntity.class);
            verify(dashboardNotificationsRepository).save(captor.capture());
            DashboardNotificationsEntity saved = captor.getValue();

            assertThat(saved.getNotificationActions()).isNotNull();
            assertThat(saved.getNotificationActions()).hasSize(1);

            NotificationActionEntity action = saved.getNotificationActions().get(0);
            assertThat(action).isNotNull();
            assertThat(action.getActionPerformed()).isEqualTo("Click");
            assertThat(action.getCreatedBy()).isEqualTo("Claimant user");
            assertThat(action.getReference()).isEqualTo(notification.getReference());
            assertThat(action.getId()).isNull();
        }

        @Test
        void shouldAppendNewClickActionWhenNotificationAlreadyHasActions() {
            DashboardNotificationsEntity notification = getNotification(id);

            NotificationActionEntity existingAction = new NotificationActionEntity();
            existingAction.setId(99L);
            existingAction.setActionPerformed("Click");
            existingAction.setReference(notification.getReference());
            existingAction.setDashboardNotification(notification);

            notification.setNotificationActions(new ArrayList<>(List.of(existingAction)));

            when(dashboardNotificationsRepository.findById(id)).thenReturn(Optional.of(notification));
            when(dashboardNotificationsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            String authToken = "Auth-token";
            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getFullName()).thenReturn("Claimant user");
            when(idamApi.retrieveUserDetails(authToken)).thenReturn(userDetails);

            dashboardNotificationService.recordClick(id, authToken);

            ArgumentCaptor<DashboardNotificationsEntity> captor = ArgumentCaptor.forClass(DashboardNotificationsEntity.class);
            verify(dashboardNotificationsRepository).save(captor.capture());

            DashboardNotificationsEntity saved = captor.getValue();
            assertThat(saved.getNotificationActions()).hasSize(2);
            assertThat(saved.getNotificationActions())
                .extracting(NotificationActionEntity::getActionPerformed)
                .containsExactly("Click", "Click");
        }
    }
}
