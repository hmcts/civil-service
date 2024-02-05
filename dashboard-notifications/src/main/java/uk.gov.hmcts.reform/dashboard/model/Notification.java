package uk.gov.hmcts.reform.dashboard.model;

import java.util.Date;
import java.util.UUID;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;

@lombok.Getter
@lombok.Setter
@Entity
@EqualsAndHashCode
@Table(name = "dashboard_notifications")
public class Notification {
    @Id
    @NotNull
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "notifications_Templates_id", referencedColumnName = "id")
    private NotificationTemplate notificationTemplate;
    private String reference;
    @Column(name = "notification_name")
    private String name;
    @Column(name = "en_HTML")
    private String enHTML;
    @Column(name = "cy_HTML")
    private String cyHTML;
    @Column(name = "message_param")
    private String params;
    @Column(name = "created_By")
    private String createdBy;
    @Column(name = "created_At")
    private Date createdAt;
    @Column(name = "updated_By")
    private String updatedBy;
    @Column(name = "updated_On")
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
