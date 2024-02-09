package uk.gov.hmcts.reform.dashboard.data;

import lombok.EqualsAndHashCode;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Date;

@lombok.Getter
@lombok.Setter
@Entity
@EqualsAndHashCode
@Table(name = "dashboard_notifications_Templates", schema = "dbs")
public class NotificationTemplateEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 256)
    @Column(name = "template_name", length = 256)
    private String templateName;

    @Size(max = 256)
    @Column(name = "en_html", length = 256)
    private String enHtml;

    @Size(max = 256)
    @Column(name = "cy_html", length = 256)
    private String cyHtml;

    @Size(max = 256)
    @Column(name = "notification_role", length = 256)
    private String notificationRole;

    @Size(max = 256)
    @Column(name = "time_to_live", length = 256)
    private String timeToLive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
