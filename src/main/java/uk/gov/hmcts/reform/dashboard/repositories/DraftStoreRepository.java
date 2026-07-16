package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.entities.DraftStoreEntity;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DraftStoreRepository extends CrudRepository<DraftStoreEntity, UUID> {

    Optional<DraftStoreEntity> findFirstByUserIdAndDraftTypeIdAndExpiresAtAfterOrderByUpdatedAtDesc(
        String userId,
        Integer draftTypeId,
        OffsetDateTime now
    );

    Optional<DraftStoreEntity> findByIdAndUserIdAndDraftTypeIdAndExpiresAtAfter(
        UUID id,
        String userId,
        Integer draftTypeId,
        OffsetDateTime now
    );

    long deleteByIdAndUserIdAndDraftTypeId(UUID id, String userId, Integer draftTypeId);
}
