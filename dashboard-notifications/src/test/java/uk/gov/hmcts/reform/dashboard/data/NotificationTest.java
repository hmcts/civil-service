package uk.gov.hmcts.reform.dashboard.data;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationActionEntity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    void shouldReturnLatestNotificationAction_whenMultipleActionsExist() {

        NotificationActionEntity olderAction = new NotificationActionEntity();
        olderAction.setId(1L);
        olderAction.setReference("reference");
        olderAction.setActionPerformed("Click");
        olderAction.setCreatedBy("User 1");
        olderAction.setCreatedAt(OffsetDateTime.now().minusHours(2));

        NotificationActionEntity latestAction = new NotificationActionEntity();
        latestAction.setId(2L);
        latestAction.setReference("reference");
        latestAction.setActionPerformed("Click");
        latestAction.setCreatedBy("User 2");
        latestAction.setCreatedAt(OffsetDateTime.now());

        UUID notificationId = UUID.randomUUID();
        DashboardNotificationsEntity entity = new DashboardNotificationsEntity();
        entity.setId(notificationId);
        entity.setNotificationActions(new ArrayList<>(List.of(olderAction, latestAction)));

        Notification notification = Notification.from(entity);

        assertThat(notification.getNotificationAction()).isNotNull();
        assertThat(notification.getNotificationAction().getId()).isEqualTo(2L);
        assertThat(notification.getNotificationAction().getCreatedBy()).isEqualTo("User 2");
        assertThat(notification.getNotificationAction().getCreatedAt()).isEqualTo(latestAction.getCreatedAt());
    }

    @Test
    void shouldReturnLatestNotificationAction_whenSomeActionsHaveNullCreatedAt() {
        NotificationActionEntity nullAction = new NotificationActionEntity();
        nullAction.setId(1L);
        nullAction.setCreatedAt(null);

        NotificationActionEntity latestAction = new NotificationActionEntity();
        latestAction.setId(2L);
        latestAction.setCreatedAt(OffsetDateTime.now());

        DashboardNotificationsEntity entity = new DashboardNotificationsEntity();
        entity.setId(UUID.randomUUID());
        entity.setNotificationActions(new ArrayList<>(List.of(nullAction, latestAction)));

        Notification notification = Notification.from(entity);

        assertThat(notification.getNotificationAction()).isNotNull();
        assertThat(notification.getNotificationAction().getId()).isEqualTo(2L);
    }

    @Test
    void shouldReturnAction_whenAllActionsHaveNullCreatedAt() {
        NotificationActionEntity action1 = new NotificationActionEntity();
        action1.setId(1L);
        action1.setCreatedAt(null);

        NotificationActionEntity action2 = new NotificationActionEntity();
        action2.setId(2L);
        action2.setCreatedAt(null);

        DashboardNotificationsEntity entity = new DashboardNotificationsEntity();
        entity.setId(UUID.randomUUID());
        entity.setNotificationActions(new ArrayList<>(List.of(action1, action2)));

        Notification notification = Notification.from(entity);

        assertThat(notification.getNotificationAction()).isNotNull();
        // Since both are null, it depends on which one stream max picks if they are equal,
        // but it should at least not throw exception and return one of them.
        assertThat(notification.getNotificationAction().getId()).isIn(1L, 2L);
    }

    @Test
    void shouldReturnNullNotificationAction_whenNoActionsExist() {
        DashboardNotificationsEntity entity = new DashboardNotificationsEntity();
        entity.setId(UUID.randomUUID());
        entity.setNotificationActions(null);

        Notification notification = Notification.from(entity);

        assertThat(notification.getNotificationAction()).isNull();
    }
}
