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
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;
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
    void saveOrUpdateShouldSaveNewNotificationWhenNoneExists() {
        when(dashboardNotificationsRepository.findByReferenceAndCitizenRoleAndName(any(), any(), any()))
            .thenReturn(List.of());

        DashboardNotificationsEntity notification = new DashboardNotificationsEntity();
        notification.setId(UUID.randomUUID());
        notification.setName("template.name");
        notification.setReference("reference");
        notification.setCitizenRole("CLAIMANT");

        dashboardNotificationService.saveOrUpdate(notification);

        verify(dashboardNotificationsRepository, never()).deleteById(any());
        verify(dashboardNotificationsRepository).save(notification);
    }

    @Test
    void saveOrUpdateShouldPickExactIdMatch() {
        UUID specificId = UUID.randomUUID();
        DashboardNotificationsEntity notification = new DashboardNotificationsEntity();
        notification.setId(specificId);
        notification.setName("template.name");

        DashboardNotificationsEntity earlier = createDashboardNotificationsEntity("earlier");
        earlier.setId(specificId);
        earlier.setCreatedAt(OffsetDateTime.now().minusDays(2));

        DashboardNotificationsEntity later = createDashboardNotificationsEntity("later");
        later.setId(UUID.randomUUID());
        later.setCreatedAt(OffsetDateTime.now().minusDays(1));

        when(dashboardNotificationsRepository.findByReferenceAndCitizenRoleAndName(any(), any(), any()))
            .thenReturn(List.of(earlier, later));

        dashboardNotificationService.saveOrUpdate(notification);

        final ArgumentCaptor<DashboardNotificationsEntity> captor = ArgumentCaptor.forClass(DashboardNotificationsEntity.class);
        verify(dashboardNotificationsRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(specificId);
    }

    @Test
    void saveOrUpdateShouldPickLatestByCreatedAt() {
        DashboardNotificationsEntity old = createDashboardNotificationsEntity("old");
        old.setCreatedAt(OffsetDateTime.now().minusDays(2));
        DashboardNotificationsEntity newer = createDashboardNotificationsEntity("newer");
        newer.setCreatedAt(OffsetDateTime.now().minusDays(1));

        when(dashboardNotificationsRepository.findByReferenceAndCitizenRoleAndName(any(), any(), any()))
            .thenReturn(List.of(old, newer));

        DashboardNotificationsEntity notification = new DashboardNotificationsEntity();
        notification.setName("template.name");

        dashboardNotificationService.saveOrUpdate(notification);

        final ArgumentCaptor<DashboardNotificationsEntity> captor = ArgumentCaptor.forClass(DashboardNotificationsEntity.class);
        verify(dashboardNotificationsRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(newer.getId());
    }

    @Test
    void saveOrUpdateShouldPickTimedOverNullCreatedAt() {
        DashboardNotificationsEntity nullCreated = createDashboardNotificationsEntity("null");
        nullCreated.setCreatedAt(null);
        DashboardNotificationsEntity timed = createDashboardNotificationsEntity("timed");
        timed.setCreatedAt(OffsetDateTime.now());

        when(dashboardNotificationsRepository.findByReferenceAndCitizenRoleAndName(any(), any(), any()))
            .thenReturn(List.of(nullCreated, timed));

        DashboardNotificationsEntity notification = new DashboardNotificationsEntity();
        notification.setName("template.name");

        dashboardNotificationService.saveOrUpdate(notification);

        final ArgumentCaptor<DashboardNotificationsEntity> captor = ArgumentCaptor.forClass(DashboardNotificationsEntity.class);
        verify(dashboardNotificationsRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(timed.getId());
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
    }

    private DashboardNotificationsEntity createDashboardNotificationsEntity(String reference) {
        DashboardNotificationsEntity notification = new DashboardNotificationsEntity();
        notification.setId(UUID.randomUUID());
        notification.setReference(reference);
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
            DashboardNotificationsEntity saved = captor.getValue();
            assertThat(saved.getClickedBy()).isEqualTo("Claimant user");
            assertThat(saved.getClickedAt()).isNotNull();
        }

        @Test
        void shouldUpdateClickedAtWhenRecordingSecondClick() {
            DashboardNotificationsEntity notification = getNotification(id);
            OffsetDateTime firstClickDate = OffsetDateTime.now().minusDays(1);
            notification.setClickedBy("First user");
            notification.setClickedAt(firstClickDate);

            when(dashboardNotificationsRepository.findById(id)).thenReturn(Optional.of(notification));
            when(dashboardNotificationsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            String authToken = "Auth-token";
            UserDetails userDetails = Mockito.mock(UserDetails.class);
            when(userDetails.getFullName()).thenReturn("Second user");
            when(idamApi.retrieveUserDetails(authToken)).thenReturn(userDetails);

            dashboardNotificationService.recordClick(id, authToken);

            ArgumentCaptor<DashboardNotificationsEntity> captor = ArgumentCaptor.forClass(DashboardNotificationsEntity.class);
            verify(dashboardNotificationsRepository).save(captor.capture());
            DashboardNotificationsEntity saved = captor.getValue();
            assertThat(saved.getClickedBy()).isEqualTo("Second user");
            assertThat(saved.getClickedAt()).isAfter(firstClickDate);
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
