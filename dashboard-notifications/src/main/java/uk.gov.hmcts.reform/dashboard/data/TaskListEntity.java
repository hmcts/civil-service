package uk.gov.hmcts.reform.dashboard.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.dashboard.utils.JsonDataConverter;

import javax.persistence.*;
import java.time.Instant;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "task_list", schema = "public")
public class TaskListEntity {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_list_template_id", nullable = false)
    private TaskListTemplateEntity taskListTemplate;

    @Size(max = 256)
    @Column(name = "role", length = 256)
    private String role;

    @Size(max = 256)
    @Column(name = "current_status", length = 256)
    private String currentStatus;

    @Size(max = 256)
    @Column(name = "next_status", length = 256)
    private String nextStatus;

    @Size(max = 256)
    @Column(name = "task_item_en", length = 256)
    private String taskItemEn;

    @Size(max = 256)
    @Column(name = "task_item_cy", length = 256)
    private String taskItemCy;

    @Size(max = 20)
    @Column(name = "case_reference", length = 20)
    private String caseReference;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "modified_at")
    private Instant modifiedAt;

    @Size(max = 256)
    @Column(name = "created_by", length = 256)
    private String createdBy;

    @Size(max = 256)
    @Column(name = "modified_by", length = 256)
    private String modifiedBy;

    @Column(name = "order_by")
    private Long orderBy;

    @Column(name = "messageparams", nullable = false)
    @Convert(converter = JsonDataConverter.class)
    private JsonNode data;
}
