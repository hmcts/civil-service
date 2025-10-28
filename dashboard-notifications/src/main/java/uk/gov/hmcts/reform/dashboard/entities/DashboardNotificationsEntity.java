package uk.gov.hmcts.reform.dashboard.entities;

import com.vladmihalcea.hibernate.type.json.JsonType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.hibernate.annotations.Type;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.UUID;

@lombok.Data
@lombok.Builder(toBuilder = true)
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
@Table(name = "dashboard_notifications", schema = "dbs")
public class DashboardNotificationsEntity implements Serializable {

    private static final long serialVersionUID = -649190928299762655L;

    @Id
    @NotNull
    @Schema(name = "id")
    private UUID id;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn
    @Schema(name = "dashboard_notifications_templates_id")
    private NotificationTemplateEntity dashboardNotificationsTemplates;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "dashboardNotification")
    @Schema(name = "notification_action_id")
    private NotificationActionEntity notificationAction;

    @Schema(name = "reference")
    private String reference;

    @Column(name = "notification_name")
    @Schema(name = "notification_name")
    private String name;

    @Schema(name = "citizen_role")
    private String citizenRole;

    @Schema(name = "title_en")
    private String titleEn;

    @Schema(name = "description_en")
    private String descriptionEn;

    @Schema(name = "title_cy")
    private String titleCy;

    @Schema(name = "description_cy")
    private String descriptionCy;

    @Type(JsonType.class)
    @Column(name = "message_params", columnDefinition = "jsonb")
    @Schema(name = "message_params")
    private HashMap<String, Object> params;

    @Schema(name = "created_by")
    private String createdBy;

    @Schema(name = "created_at")
    private OffsetDateTime createdAt;

    @Schema(name = "updated_by")
    private String updatedBy;

    @Schema(name = "updated_on")
    private OffsetDateTime updatedOn;

    @Schema(name = "deadline")
    private LocalDateTime deadline;
}
