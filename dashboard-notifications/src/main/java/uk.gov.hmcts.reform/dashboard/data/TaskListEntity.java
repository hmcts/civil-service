package uk.gov.hmcts.reform.dashboard.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.dashboard.utils.JsonDataConverter;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
@Getter
@Setter
@Entity
@Table(name = "task_list", schema = "public")
public class TaskListEntity {
    @Id
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_list_template_id", nullable = false)
    private TaskListTemplateEntity taskListTemplate;
    private String role;
    private String currentStatus;
    private String nextStatus;
    private String taskItemEn;
    private String taskItemCy;
    private String caseReference;
    private Instant createdAt;
    private Instant modifiedAt;
    private String createdBy;
    private String modifiedBy;
    private Long orderBy;

    @Column(name = "messageparams", nullable = false)
    @Convert(converter = JsonDataConverter.class)
    private JsonNode data;
}
