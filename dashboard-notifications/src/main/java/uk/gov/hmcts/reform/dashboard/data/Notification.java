package uk.gov.hmcts.reform.dashboard.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;

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

    public static Notification from(NotificationEntity notificationEntity) {
        return new Notification(notificationEntity.getId(), notificationEntity.getTitleEn(),
                                notificationEntity.getTitleCy(), notificationEntity.getDescriptionEn(),
                                notificationEntity.getDescriptionCy()
        );
    }
}
