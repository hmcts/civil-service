package uk.gov.hmcts.reform.dashboard.model.dashboard;

import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;
@lombok.Getter

@Entity
@EqualsAndHashCode
@Table(name = "task_Lists_Templates")
public class TaskListTemplate {

    @Id
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
    public void setRole(String role) {
        this.role = role;
    }
    public void setEnHTML(String enHTML) {
        this.enHTML = enHTML;
    }
    public void setCyHTML(String cyHTML) {
        this.cyHTML = cyHTML;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setCategoyEn(String categoyEn) {
        this.categoyEn = categoyEn;
    }
    public void setCategoyCy(String categoyCy) {
        this.categoyCy = categoyCy;
    }

}
