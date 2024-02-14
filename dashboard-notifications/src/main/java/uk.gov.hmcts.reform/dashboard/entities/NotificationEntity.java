package uk.gov.hmcts.reform.dashboard.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@lombok.Data
@lombok.Builder(toBuilder = true)
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
@Table(name = "dashboard_notifications", schema = "dbs")
public class NotificationEntity implements Serializable {

    private static final long serialVersionUID = -649190928299762655L;

    @Id
    @NotNull
    @Schema(name = "id")
    private UUID id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn
    @Schema(name = "dashboard_notifications_templates_id")
    private NotificationTemplateEntity dashboardNotificationsTemplates;

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
    @Type(type = "jsonb")
    @Column(name = "message_param", columnDefinition = "jsonb")
    @Schema(name = "message_param")
    private Map<String, Object> params;
    @Schema(name = "created_by")
    private String createdBy;
    @Schema(name = "created_at")
    private OffsetDateTime createdAt;
    @Schema(name = "updated_by")
    private String updatedBy;
    @Schema(name = "updated_on")
    private OffsetDateTime updatedOn;
}
