package uk.gov.hmcts.reform.dashboard.model.dashboard;

import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@lombok.Getter
@Entity
@EqualsAndHashCode
@Table(name = "task_List")
public class TaskList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String taskListTemplateId;
    private String caseReference;
    private String role;
    private String enHTML;
    private String cyHTML;
    private String reference;
    private String createdBy;
    private Date createdAt;
    private String modifiedBy;
    private Date modifiedAt;

    public TaskList() {}

    public TaskList(String taskListTemplateId, String caseReference, String role, String enHTML, String cyHTML, String reference, String createdBy, Date createdAt, String modifiedBy, Date modifiedAt) {
        this.taskListTemplateId = taskListTemplateId;
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

    public void setTaskListTemplateId(String taskListTemplateId) {
        this.taskListTemplateId = taskListTemplateId;
    }
    public void setCaseReference(String caseReference) {
        this.caseReference = caseReference;
    }

    public void setRole(String role) {
        this.role = role;

        public void setEnHTML(String enHTML) {
        this.enHTML = enHTML;
    }

    public void setCyHTML(String cyHTML) {
        this.cyHTML = cyHTML;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
    public void setId(Long id) {
        this.id = id;
    }

}
