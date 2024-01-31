package uk.gov.hmcts.reform.civil.model.citizenui.model;

import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@EqualsAndHashCode
@Table(name = "task_Lists_Templates")
public class TaskListTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String role;
    private String categoyEn;
    private String categoyCy;
    private String enHTML;
    private String cyHTML;
    private String reference;
    private Date createdAt;
    private Date modifiedAt;

    public TaskListTemplate() {}

    public TaskListTemplate(String role,String enHTML, String cyHTML, String categoyEn, String categoyCy, String reference, Date createdAt, Date modifiedAt) {

        this.role = role;
        this.categoyEn = categoyEn;
        this.categoyCy = categoyCy;
        this.enHTML = enHTML;
        this.cyHTML = cyHTML;
        this.reference = reference;
        this.createdAt = createdAt;
        this.modifiedAt= modifiedAt;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoyEn() {
        return categoyEn;
    }

    public void setCategoyEn(String categoyEn) {
        this.categoyEn = categoyEn;
    }

    public String getCategoyCy() {
        return categoyCy;
    }

    public void setCategoyCy(String categoyCy) {
        this.categoyCy = categoyCy;
    }

}
