package uk.gov.hmcts.reform.dashboard.model;

import java.util.Date;
import java.util.UUID;
import javax.persistence.*;
import lombok.EqualsAndHashCode;

@lombok.Getter
@lombok.Setter
@Entity
@EqualsAndHashCode
@Table(name = "dashboard_notifications")
public class Notification {
    @Id
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "notifications_Templates_id", referencedColumnName = "id")
    private NotificationTemplate notificationTemplate;
    private String reference;
    private String name;
    private String enHTML;
    private String cyHTML;
    @Column(name = "message_param")
    private String params;
    private String createdBy;
    private Date createdAt;
    private String updatedBy;
    private Date updatedOn;

    public Notification() {

    }

    public Notification(NotificationTemplate notificationTemplate, String reference, String name, String enHTML,String cyHTML, String params, String createdBy, Date createdAt, String updatedBy, Date updatedOn) {
        this.notificationTemplate = notificationTemplate;
        this.reference = reference;
        this.name = name;
        this.enHTML = enHTML;
        this.cyHTML = cyHTML;
        this.params = params;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
    }
}
