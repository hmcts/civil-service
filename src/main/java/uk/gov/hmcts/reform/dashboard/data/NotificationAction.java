package uk.gov.hmcts.reform.dashboard.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dashboard.entities.NotificationActionEntity;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAction {

    private Long id;

    private String reference;

    private String actionPerformed;

    private String createdBy;

    private OffsetDateTime createdAt;

    public static NotificationAction from(NotificationActionEntity notificationActionEntity) {
        return new NotificationAction(
            notificationActionEntity.getId(),
            notificationActionEntity.getReference(),
            notificationActionEntity.getActionPerformed(),
            notificationActionEntity.getCreatedBy(),
            notificationActionEntity.getCreatedAt()
        );
    }
}
