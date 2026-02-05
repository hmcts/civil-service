package uk.gov.hmcts.reform.dashboard.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.ToString;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
    private static final String NOTIFICATION_ACTION_ID_SEQ = "dbs.notification_action_id_seq";

    @Id
    @NotNull
    @Schema(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = NOTIFICATION_ACTION_ID_SEQ)
    @SequenceGenerator(name = NOTIFICATION_ACTION_ID_SEQ, sequenceName = NOTIFICATION_ACTION_ID_SEQ, allocationSize = 1)
    @Column(name = "id", updatable = false, nullable = false, insertable = false)
    private Long id;

    @Schema(name = "reference")
    private String reference;

    @Schema(name = "action_performed")
    private String actionPerformed;

    @Schema(name = "created_by")
    private String createdBy;

    @Schema(name = "created_at")
    private OffsetDateTime createdAt;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "dashboard_notifications_id", referencedColumnName = "id")
    @Schema(name = "dashboard_notifications_id")
    private DashboardNotificationsEntity dashboardNotification;
}
