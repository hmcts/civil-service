package uk.gov.hmcts.reform.dashboard.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;

@lombok.Data
@lombok.Builder(toBuilder = true)
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
@Table(name = "scenario", schema = "dbs")
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ScenarioEntity implements Serializable {

    private static final long serialVersionUID = -3567298611275939014L;

    @Id
    @NotNull
    @Schema(name = "id")
    private Long id;

    @JoinColumn
    @Schema(name = "name")
    private String name;

    @Schema(name = "notifications_to_delete")
    @Type(type = "com.vladmihalcea.hibernate.type.array.StringArrayType")
    private String[] notificationsToDelete;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Schema(name = "notifications_to_create")
    private Map<String, String[]> notificationsToCreate;

    @Schema(name = "created_at")
    private OffsetDateTime createdAt;
}
