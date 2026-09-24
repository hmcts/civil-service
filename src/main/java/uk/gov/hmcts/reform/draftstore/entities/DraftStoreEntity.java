package uk.gov.hmcts.reform.draftstore.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.reform.draftstore.DraftType;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "draft_store", schema = "dbs")
public class DraftStoreEntity implements Serializable {

    private static final long serialVersionUID = 8404428249109945073L;

    @Id
    @NotNull
    private UUID id;

    @NotNull
    @Size(max = 200)
    @Column(name = "user_id")
    private String userId;

    @Size(max = 200)
    @Column(name = "case_id")
    private String caseId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "draft_type", length = 200)
    private DraftType draftType;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @NotNull
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @NotNull
    @Column(name = "expires_at", updatable = false)
    private OffsetDateTime expiresAt;
}
