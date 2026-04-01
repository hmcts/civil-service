package uk.gov.hmcts.reform.dashboard.data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;

import java.util.HashMap;
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

    private HashMap<String, Object> params;

    private OffsetDateTime createdAt;

    private LocalDateTime deadline;

    private String clickedBy;

    private OffsetDateTime clickedAt;

    public static Notification from(DashboardNotificationsEntity dashboardNotificationsEntity) {
        return new Notification(
            dashboardNotificationsEntity.getId(),
            dashboardNotificationsEntity.getTitleEn(),
            dashboardNotificationsEntity.getTitleCy(),
            dashboardNotificationsEntity.getDescriptionEn(),
            dashboardNotificationsEntity.getDescriptionCy(),
            dashboardNotificationsEntity.getTimeToLive(),
            dashboardNotificationsEntity.getParams(),
            dashboardNotificationsEntity.getCreatedAt(),
            dashboardNotificationsEntity.getDeadline(),
            dashboardNotificationsEntity.getClickedBy(),
            dashboardNotificationsEntity.getClickedAt()
        );
    }
}
