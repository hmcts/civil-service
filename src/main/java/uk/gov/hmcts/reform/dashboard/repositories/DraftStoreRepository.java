package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.entities.DraftStoreEntity;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DraftStoreRepository extends JpaRepository<DraftStoreEntity, UUID> {

    Optional<DraftStoreEntity> findByUserIdAndDraftTypeId(String userId, Integer draftTypeId);

    Optional<DraftStoreEntity> findByUserIdAndDraftTypeIdAndExpiresAtAfter(
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
