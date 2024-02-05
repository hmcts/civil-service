package uk.gov.hmcts.reform.dashboard.data;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.dashboard.utils.JsonDataConverter;


import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Table(name = "task_list", schema = "public")
@Entity
public class TaskListEntity {
    @Id
    private UUID id;

    @NotNull
    @ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_item_template_id", nullable = false)
    private TaskItemTemplateEntity taskItemTemplate;

    private Short currentStatus;

    private Short nextStatus;

    @Size(max = 512)
    private String taskItemEn;

    @Size(max = 512)
    private String taskItemCy;

    @NotNull
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Size(max = 256)
    private String updatedBy;

    @Column(name = "message_parm", columnDefinition = "jsonb(0, 0)")
    @Convert(converter = JsonDataConverter.class)
    private Object messageParm;

}
