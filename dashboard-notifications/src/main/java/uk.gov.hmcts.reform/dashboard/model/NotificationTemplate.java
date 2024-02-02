package uk.gov.hmcts.reform.dashboard.model;

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
@Table(name = "dashboard_notifications_Templates")
public class NotificationTemplate {

    @Id
    private Long id;
    private String role;
    private String name;
    private String enHTML;
    private String cyHTML;
    private Date createdAt;
    @Column(name = "time_to_live")
    private String timeToLive;

    public NotificationTemplate() {

    }

    public NotificationTemplate(String role, String enHTML, String cyHTML, String name, Date createdAt, String timeToLive) {

        this.role = role;
        this.enHTML = enHTML;
        this.cyHTML = cyHTML;
        this.name = name;
        this.createdAt = createdAt;
        this.timeToLive = timeToLive;
    }
}
