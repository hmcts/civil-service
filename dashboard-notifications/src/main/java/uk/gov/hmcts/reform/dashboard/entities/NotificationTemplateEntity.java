package uk.gov.hmcts.reform.dashboard.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@lombok.Data
@lombok.Builder(toBuilder = true)
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
@Immutable
@Table(name = "dashboard_notifications_templates", schema = "dbs")
public class NotificationTemplateEntity implements Serializable {

    private static final long serialVersionUID = -7113029898212912034L;

    @Id
    @Schema(name = "id")
    private Long id;
    @Column(name = "notification_role")
    @Schema(name = "notification_role")
    private String role;
    @Column(name = "template_name")
    @Schema(name = "template_name")
    private String name;
    @Schema(name = "title_en")
    private String titleEn;
    @Schema(name = "title_cy")
    private String titleCy;
    @Schema(name = "description_en")
    private String descriptionEn;
    @Schema(name = "description_cy")
    private String descriptionCy;
    @Schema(name = "created_at")
    private Date createdAt;
    @Schema(name = "time_to_live")
    private String timeToLive;
    @Schema(name = "deadline_param")
    private String deadlineParam;
}
