package uk.gov.hmcts.reform.dashboard.data;

import java.time.LocalDateTime;
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

import lombok.EqualsAndHashCode;
import net.bytebuddy.asm.Advice;

@lombok.Getter
@lombok.Setter
@Entity
@EqualsAndHashCode
@Table(name = "dashboard_notifications", schema = "dbs")
public class NotificationEntity {

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
    @Column(name = "en_html", length = 256)
    private String enHtml;

    @Size(max = 256)
    @Column(name = "cy_html", length = 256)
    private String cyHtml;

    @Size(max = 256)
    @Column(name = "message_param", length = 256)
    private String messageParam;

    @Size(max = 256)
    @Column(name = "created_by", length = 256)
    private String createdBy;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Size(max = 256)
    @Column(name = "updated_by", length = 256)
    private String updatedBy;

    @NotNull
    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn;

}
