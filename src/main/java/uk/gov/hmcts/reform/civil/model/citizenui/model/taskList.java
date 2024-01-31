package uk.gov.hmcts.reform.civil.model.citizenui.model;

import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@EqualsAndHashCode
@Table(name = "task_List")
public class taskList {

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

    public taskList() {}

    public taskList(String taskListTemplateId, String caseReference, String role, String enHTML, String cyHTML, String reference, String createdBy, Date createdAt, String modifiedBy, Date modifiedAt) {
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
    public String getTaskListTemplateId() {
        return taskListTemplateId;
    }

    public void setTaskListTemplateId(String taskListTemplateId) {
        this.taskListTemplateId = taskListTemplateId;
    }

    public String getCaseReference() {
        return caseReference;
    }

    public void setCaseReference(String caseReference) {
        this.caseReference = caseReference;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEnHTML() {
        return enHTML;
    }

    public void setEnHTML(String enHTML) {
        this.enHTML = enHTML;
    }

    public String getCyHTML() {
        return cyHTML;
    }

    public void setCyHTML(String cyHTML) {
        this.cyHTML = cyHTML;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
