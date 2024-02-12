package uk.gov.hmcts.reform.dashboard.entities;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;
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
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "notifications_Templates_id", referencedColumnName = "id")
    private NotificationTemplateEntity notificationTemplateEntity;
    private String reference;
    @Column(name = "notification_name")
    private String name;
    private String citizenRole;
    private String titleEn;
    private String descriptionEn;
    private String titleCy;
    private String descriptionCy;
    @Type(type = "jsonb")
    @Column(name = "message_param")
    private String params;
    private String createdBy;
    private OffsetDateTime createdAt;
    private String updatedBy;
    private OffsetDateTime updatedOn;
}
