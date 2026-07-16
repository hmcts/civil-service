package uk.gov.hmcts.reform.dashboard.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.UUID;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
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
    @Column(name = "draft_type_id")
    private Integer draftTypeId;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private HashMap<String, Object> payload;

    @NotNull
    @Column(name = "draft_claim_created_at", updatable = false)
    private OffsetDateTime draftClaimCreatedAt;

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
