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
            var inOrder = Mockito.inOrder(dashboardNotificationsRepository);
            inOrder.verify(dashboardNotificationsRepository).deleteActionsByReferenceAndCitizenRole(reference, claimant);
            inOrder.verify(dashboardNotificationsRepository).deleteByReferenceAndCitizenRole(reference, claimant);
        }

        @Test
        void deleteAllNotificationsToDefendant() {
            String reference = "reference";
            String defendant = "DEFENDANT";
            dashboardNotificationService.deleteByReferenceAndCitizenRole(reference, defendant);
            var inOrder = Mockito.inOrder(dashboardNotificationsRepository);
            inOrder.verify(dashboardNotificationsRepository).deleteActionsByReferenceAndCitizenRole(reference, defendant);
            inOrder.verify(dashboardNotificationsRepository).deleteByReferenceAndCitizenRole(reference, defendant);
        }
    }

    @Test
    void deleteNotificationsByNameAndReference() {
        String templateName = "template.name";
        String reference = "reference";
        dashboardNotificationService.deleteByNameAndReference(templateName, reference);
        var inOrder = Mockito.inOrder(dashboardNotificationsRepository);
        inOrder.verify(dashboardNotificationsRepository).deleteActionsByNameAndReference(templateName, reference);
        inOrder.verify(dashboardNotificationsRepository).deleteByNameAndReference(templateName, reference);
    }

    @Test
    void deleteNotificationsByNameAndReferenceAndCitizenRole() {
        String templateName = "template.name";
        String reference = "reference";
        String citizenRole = "CLAIMANT";
        when(dashboardNotificationsRepository.deleteByNameAndReferenceAndCitizenRole(templateName, reference, citizenRole))
            .thenReturn(1);

        int result = dashboardNotificationService.deleteByNameAndReferenceAndCitizenRole(templateName, reference, citizenRole);

        assertThat(result).isEqualTo(1);
        var inOrder = Mockito.inOrder(dashboardNotificationsRepository);
        inOrder.verify(dashboardNotificationsRepository).deleteActionsByNameAndReferenceAndCitizenRole(templateName, reference, citizenRole);
        inOrder.verify(dashboardNotificationsRepository).deleteByNameAndReferenceAndCitizenRole(templateName, reference, citizenRole);
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
    void saveOrUpdateShouldSaveDirectlyWhenNameExistsButNoMatchingRecords() {
        DashboardNotificationsEntity notification = new DashboardNotificationsEntity();
        notification.setId(UUID.randomUUID());
        notification.setName("template.name");
        notification.setReference("reference");
        notification.setCitizenRole("CLAIMANT");

        when(dashboardNotificationsRepository.findByReferenceAndCitizenRoleAndName(any(), any(), any()))
            .thenReturn(List.of());
        when(dashboardNotificationsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DashboardNotificationsEntity saved = dashboardNotificationService.saveOrUpdate(notification);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(notification.getId());
        verify(dashboardNotificationsRepository).save(any());
        verifyNoInteractions(notificationActionRepository);
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
        void shouldDoNothingWhenNotificationNotFound() {
            when(dashboardNotificationsRepository.findById(id)).thenReturn(Optional.empty());

            dashboardNotificationService.recordClick(id, "Auth-token");

            verify(dashboardNotificationsRepository, never()).save(any());
            verifyNoInteractions(idamApi);
        }

        @Test
        void shouldNotReuseActionIdWhenExistingActionIsNotClick() {
            DashboardNotificationsEntity notification = getNotification(id);
            NotificationActionEntity existingAction = new NotificationActionEntity();
            existingAction.setId(99L);
            existingAction.setActionPerformed("View");
            existingAction.setReference(notification.getReference());
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
            assertThat(action.getId()).isNull();
            assertThat(action.getActionPerformed()).isEqualTo("Click");
        }

        @Test
        void shouldReuseExistingClickActionIdWhenRecordingSecondClick() {
            DashboardNotificationsEntity notification = getNotification(id);
            NotificationActionEntity existingAction = new NotificationActionEntity();
            existingAction.setId(99L);
            existingAction.setActionPerformed("Click");
            existingAction.setReference(notification.getReference());
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

    @Test
    void should_return_dashboard_notification_entities_for_case_and_role() {
        List<DashboardNotificationsEntity> expected = getNotificationEntityList();

        when(dashboardNotificationsRepository
                 .findByReferenceAndCitizenRole("123", "Claimant"))
            .thenReturn(expected);

        List<DashboardNotificationsEntity> actual =
            dashboardNotificationService
                .getDashboardNotifications("123", "Claimant");

        assertThat(actual).isEqualTo(expected);
        verify(dashboardNotificationsRepository)
            .findByReferenceAndCitizenRole("123", "Claimant");
    }

    @Test
    void should_return_only_case_stayed_notifications_when_present() {
        final UUID normalId = UUID.randomUUID();
        final UUID stayedId = UUID.randomUUID();

        DashboardNotificationsEntity normal = new DashboardNotificationsEntity();
        normal.setId(normalId);
        normal.setName("Some.Other.Template");
        normal.setCreatedAt(OffsetDateTime.now().minusHours(1));

        DashboardNotificationsEntity stayed = new DashboardNotificationsEntity();
        stayed.setId(stayedId);
        stayed.setName("Notice.AAA6.CP.Case.Stayed.Claimant");
        stayed.setCreatedAt(OffsetDateTime.now());

        when(dashboardNotificationsRepository.findByReferenceAndCitizenRole("case", "role"))
            .thenReturn(List.of(normal, stayed));

        List<Notification> result =
            dashboardNotificationService.getNotifications("case", "role");

        assertThat(result)
            .extracting(Notification::getId)
            .containsExactly(stayedId);
    }

    @Test
    void should_return_all_notifications_when_no_case_stayed_templates_exist() {
        final UUID id1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();

        DashboardNotificationsEntity n1 = new DashboardNotificationsEntity();
        n1.setId(id1);
        n1.setName("Template.One");
        n1.setCreatedAt(OffsetDateTime.now());

        DashboardNotificationsEntity n2 = new DashboardNotificationsEntity();
        n2.setId(id2);
        n2.setName("Template.Two");
        n2.setCreatedAt(OffsetDateTime.now().minusHours(1));

        when(dashboardNotificationsRepository.findByReferenceAndCitizenRole("case", "role"))
            .thenReturn(List.of(n1, n2));

        List<Notification> result =
            dashboardNotificationService.getNotifications("case", "role");

        assertThat(result)
            .extracting(Notification::getId)
            .containsExactly(id1, id2);
    }
}
