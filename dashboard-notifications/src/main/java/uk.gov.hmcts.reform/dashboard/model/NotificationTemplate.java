package uk.gov.hmcts.reform.dashboard.model.dashboard;

import lombok.EqualsAndHashCode;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import java.util.Date;

@lombok.Getter
@lombok.Setter
@Entity
@EqualsAndHashCode
@Table(name = "notifications_Templates")
public class NotificationTemplate {

    @Id
    private Long id;

    private String role;
    private String enHTML;
    private String cyHTML;
    private String reference;
    private Date createdAt;
    private Date modifiedAt;

    public NotificationTemplate() {

    }

    public NotificationTemplate(String role, String enHTML, String cyHTML, String reference, Date createdAt, Date modifiedAt) {

        this.role = role;
        this.enHTML = enHTML;
        this.cyHTML = cyHTML;
        this.reference = reference;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }
}
