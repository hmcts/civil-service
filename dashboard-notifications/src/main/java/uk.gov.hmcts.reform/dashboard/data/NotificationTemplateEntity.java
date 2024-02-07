package uk.gov.hmcts.reform.dashboard.data;

import lombok.EqualsAndHashCode;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import java.util.Date;

@lombok.Getter
@lombok.Setter
@Entity
@EqualsAndHashCode
@Table(name = "dashboard_notifications_Templates", schema = "dbs")
public class NotificationTemplateEntity {

    @Id
    private Long id;
    @Column(name = "notification_role")
    private String role;
    @Column(name = "template_name")
    private String name;
    private String enHTML;
    private String cyHTML;
    private Date createdAt;
    private String timeToLive;

    public NotificationTemplateEntity() {

    }

    public NotificationTemplateEntity(String role, String enHTML, String cyHTML, String name, Date createdAt, String timeToLive) {

        this.role = role;
        this.enHTML = enHTML;
        this.cyHTML = cyHTML;
        this.name = name;
        this.createdAt = createdAt;
        this.timeToLive = timeToLive;
    }
}
