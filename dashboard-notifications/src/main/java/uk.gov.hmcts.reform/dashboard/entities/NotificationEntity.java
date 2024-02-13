package uk.gov.hmcts.reform.dashboard.entities;

import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@lombok.Data
@lombok.Builder(toBuilder = true)
@lombok.NoArgsConstructor
@Entity
@AllArgsConstructor
@Table(name = "dashboard_notifications", schema = "dbs")
public class NotificationEntity implements Serializable {

    private static final long serialVersionUID = -649190928299762655L;

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = javax.persistence.FetchType.LAZY)
    @JoinColumn(name = "dashboard_notifications_templates_id")
    private NotificationTemplateEntity dashboardNotificationsTemplates;

    @Size(max = 256)
    @Column(name = "reference", length = 256)
    private String reference;

    @Size(max = 256)
    @Column(name = "notification_name", length = 256)
    private String notificationName;

    @Size(max = 256)
    @Column(name = "citizen_role", length = 256)
    private String citizenRole;

    private String titleEn;
    private String titleCy;
    private String descriptionEn;
    private String descriptionCy;

    @Column(name = "message_param")
    private String params;
    private String createdBy;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Size(max = 256)
    @Column(name = "updated_by", length = 256)
    private String updatedBy;

    @NotNull
    @Column(name = "updated_on", nullable = false)
    private Date updatedOn;
}
