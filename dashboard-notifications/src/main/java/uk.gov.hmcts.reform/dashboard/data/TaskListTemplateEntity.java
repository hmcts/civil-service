package uk.gov.hmcts.reform.dashboard.data;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "task_list_template", schema = "public")
public class TaskListTemplateEntity {
    @Id
    private Long id;

    private String titleEn;

    private String contentEn;

    private String titleCy;

    private String contentCy;

    private String reference;

    private String taskStatusSequence;

    private String role;

    private Long orderBy;

    private String categoryEn;

    private String categoryCy;

    private Instant createdDate;

}
