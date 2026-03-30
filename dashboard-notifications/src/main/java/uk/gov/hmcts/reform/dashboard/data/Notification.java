package uk.gov.hmcts.reform.dashboard.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationActionEntity;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private UUID id;

    private String titleEn;

    private String titleCy;

    private String descriptionEn;

    private String descriptionCy;

    private String timeToLive;

    private NotificationAction notificationAction;

    private HashMap<String, Object> params;

    private OffsetDateTime createdAt;

    private LocalDateTime deadline;

    public static Notification from(DashboardNotificationsEntity dashboardNotificationsEntity) {
        return new Notification(
            dashboardNotificationsEntity.getId(),
            dashboardNotificationsEntity.getTitleEn(),
            dashboardNotificationsEntity.getTitleCy(),
            dashboardNotificationsEntity.getDescriptionEn(),
            dashboardNotificationsEntity.getDescriptionCy(),
            dashboardNotificationsEntity.getTimeToLive(),
            getLatestNotificationAction(dashboardNotificationsEntity),
            dashboardNotificationsEntity.getParams(),
            dashboardNotificationsEntity.getCreatedAt(),
            dashboardNotificationsEntity.getDeadline()
        );
    }

    private static NotificationAction getLatestNotificationAction(DashboardNotificationsEntity dashboardNotificationsEntity) {
        List<NotificationActionEntity> notificationActions = dashboardNotificationsEntity.getNotificationActions();
        if (notificationActions == null || notificationActions.isEmpty()) {
            return null;
        }

        return notificationActions.stream()
            .max(Comparator.comparing(
                NotificationActionEntity::getCreatedAt,
                Comparator.nullsFirst(Comparator.naturalOrder())
            ))
            .map(NotificationAction::from)
            .orElse(null);
    }
}
