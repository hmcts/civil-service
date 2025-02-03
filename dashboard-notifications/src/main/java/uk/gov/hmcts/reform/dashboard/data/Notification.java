package uk.gov.hmcts.reform.dashboard.data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder
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

        NotificationBuilder notification = Notification.builder()
            .id(dashboardNotificationsEntity.getId())
            .titleEn(dashboardNotificationsEntity.getTitleEn())
            .titleCy(dashboardNotificationsEntity.getTitleCy())
            .descriptionEn(dashboardNotificationsEntity.getDescriptionEn())
            .descriptionCy(dashboardNotificationsEntity.getDescriptionCy())
            .timeToLive(dashboardNotificationsEntity.getDashboardNotificationsTemplates().getTimeToLive())
            .params(dashboardNotificationsEntity.getParams())
            .createdAt(dashboardNotificationsEntity.getCreatedAt())
            .deadline(dashboardNotificationsEntity.getDeadline());

        Optional.ofNullable(dashboardNotificationsEntity.getNotificationAction())
            .ifPresent(action -> notification.notificationAction(NotificationAction.from(action)));
        return notification.build();
    }
}
