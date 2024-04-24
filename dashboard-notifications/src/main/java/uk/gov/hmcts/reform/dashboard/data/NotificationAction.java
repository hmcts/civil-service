package uk.gov.hmcts.reform.dashboard.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dashboard.entities.NotificationActionEntity;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAction {

    private Long id;

    private String reference;

    private String actionPerformed;

    private String createdBy;

    private OffsetDateTime createdAt;

    public static NotificationAction from(NotificationActionEntity notificationActionEntity) {
        return NotificationAction.builder()
            .id(notificationActionEntity.getId())
            .reference(notificationActionEntity.getReference())
            .actionPerformed(notificationActionEntity.getActionPerformed())
            .createdAt(notificationActionEntity.getCreatedAt())
            .createdBy(notificationActionEntity.getCreatedBy())
            .build();
    }
}
