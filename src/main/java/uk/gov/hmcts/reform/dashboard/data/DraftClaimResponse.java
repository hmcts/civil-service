package uk.gov.hmcts.reform.dashboard.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dashboard.entities.DraftStoreEntity;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftClaimResponse {

    private UUID draftId;
    private String caseId;
    private Map<String, Object> payload;
    private OffsetDateTime draftClaimCreatedAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static DraftClaimResponse from(DraftStoreEntity draftStoreEntity) {
        return new DraftClaimResponse(
            draftStoreEntity.getId(),
            draftStoreEntity.getCaseId(),
            new HashMap<>(draftStoreEntity.getPayload()),
            draftStoreEntity.getDraftClaimCreatedAt(),
            draftStoreEntity.getExpiresAt(),
            draftStoreEntity.getCreatedAt(),
            draftStoreEntity.getUpdatedAt()
        );
    }
}
