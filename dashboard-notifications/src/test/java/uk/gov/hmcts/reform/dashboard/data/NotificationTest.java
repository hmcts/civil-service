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
    void shouldReturnNullNotificationAction_whenNoActionsExist() {
        DashboardNotificationsEntity entity = new DashboardNotificationsEntity();
        entity.setId(UUID.randomUUID());
        entity.setNotificationActions(null);

        Notification notification = Notification.from(entity);

        assertThat(notification.getNotificationAction()).isNull();
    }
}
