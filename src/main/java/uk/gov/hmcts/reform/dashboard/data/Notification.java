package uk.gov.hmcts.reform.dashboard.data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationActionEntity;

import java.util.HashMap;
import java.util.Optional;
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

    public static Notification from(DashboardNotificationsEntity entity, NotificationActionEntity latestAction) {
        Notification notification = new Notification(
            entity.getId(),
            entity.getTitleEn(),
            entity.getTitleCy(),
            entity.getDescriptionEn(),
            entity.getDescriptionCy(),
            entity.getTimeToLive(),
            null,
            entity.getParams(),
            entity.getCreatedAt(),
            entity.getDeadline()
        );

        Optional.ofNullable(latestAction)
            .ifPresent(action -> notification.setNotificationAction(NotificationAction.from(action)));

        return notification;
    }
}
