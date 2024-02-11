package uk.gov.hmcts.reform.dashboard.entities;

import org.hibernate.annotations.Type;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
@Table(name = "dashboard_notifications_Templates", schema = "dbs")
public class NotificationTemplateEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -7113029898212912034L;

    @Id
    private Long id;
    @Column(name = "notification_role")
    private String role;
    @Column(name = "template_name")
    private String name;
    @Type(type = "com.vladmihalcea.hibernate.type.array.StringArrayType")
    private String[] notificationsToBeDeleted;
    private String titleEn;
    private String titleCy;
    private String descriptionEn;
    private String descriptionCy;
    private Date createdAt;
    private String timeToLive;
}
