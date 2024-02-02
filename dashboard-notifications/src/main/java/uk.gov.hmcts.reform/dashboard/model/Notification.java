package uk.gov.hmcts.reform.dashboard.model;

import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import java.util.Date;
@lombok.Getter
@lombok.Setter
@Entity
@EqualsAndHashCode
@Table(name = "notifications")
public class Notification {
    @Id
    private Long id;
    private String templateId;
    private String caseReference;
    private String role;
    private String enHTML;
    private String cyHTML;
    private String reference;
    private String createdBy;
    private Date createdAt;
    private String modifiedBy;
    private Date modifiedAt;

    public Notification() {

    }

    public Notification(String templateId,String caseReference,String role, String enHTML,String cyHTML, String reference, String createdBy, Date createdAt, String modifiedBy, Date modifiedAt) {
        this.templateId = templateId;
        this.caseReference = caseReference;
        this.role = role;
        this.enHTML = enHTML;
        this.cyHTML = cyHTML;
        this.reference = reference;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.modifiedBy= modifiedBy;
        this.modifiedAt= modifiedAt;
    }
}
