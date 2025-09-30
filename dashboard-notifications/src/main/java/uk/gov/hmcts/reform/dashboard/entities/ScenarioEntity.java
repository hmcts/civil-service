package uk.gov.hmcts.reform.dashboard.entities;

import com.vladmihalcea.hibernate.type.json.JsonType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

@lombok.Data
@lombok.Builder(toBuilder = true)
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
@TypeDefs(
    {
        @TypeDef(
            name = "jsonb",
            typeClass = JsonType.class
        )
    }
)
@Immutable
@Table(name = "scenario", schema = "dbs")
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
