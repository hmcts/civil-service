package uk.gov.hmcts.reform.dashboard.entities;

import com.vladmihalcea.hibernate.type.array.LocalDateTimeArrayType;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.Type;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@lombok.Data
@lombok.Builder
@AllArgsConstructor
@lombok.NoArgsConstructor
@Entity
@Table(name = "dashboard_notifications", schema = "dbs")
public class NotificationEntity implements Serializable {

    @Serial
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
    private String enHTML;
    private String cyHTML;
    @Type(type = "jsonb")
    @Column(name = "message_param")
    private String params;
    private String createdBy;
    private OffsetDateTime createdAt;
    private String updatedBy;
    private OffsetDateTime updatedOn;
}
