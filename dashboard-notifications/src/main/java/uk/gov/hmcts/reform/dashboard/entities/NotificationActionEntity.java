package uk.gov.hmcts.reform.dashboard.entities;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;

@lombok.Data
@lombok.Builder(toBuilder = true)
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
@Table(name = "notification_action", schema = "dbs")
public class NotificationActionEntity implements Serializable {

    private static final long serialVersionUID = 4840394520858984702L;

    @Id
    @NotNull
    @Schema(name = "id")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn
    @Schema(name = "dashboard_notifications_id")
    private DashboardNotificationsEntity dashboardNotifications;

    @Schema(name = "reference")
    private String reference;

    @Schema(name = "action_performed")
    private String actionPerformed;

    @Schema(name = "created_by")
    private String createdBy;

    @Schema(name = "created_at")
    private OffsetDateTime createdAt;
}
