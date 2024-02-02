package uk.gov.hmcts.reform.dashboard.model;

import java.util.Date;
import javax.persistence.*;

import lombok.EqualsAndHashCode;

@lombok.Getter
@lombok.Setter
@Entity
@EqualsAndHashCode
@Table(name = "dashboard_notifications")
public class Notification {
    @Id
    private Long id;
    @ManyToOne
    @JoinColumn(name = "id")
    private NotificationTemplate notificationTemplate;
    private String caseReference;
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

    public Notification(NotificationTemplate notificationTemplate, String caseReference, String name, String enHTML,String cyHTML, String params, String createdBy, Date createdAt, String updatedBy, Date updatedOn) {
        this.notificationTemplate = notificationTemplate;
        this.caseReference = caseReference;
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
