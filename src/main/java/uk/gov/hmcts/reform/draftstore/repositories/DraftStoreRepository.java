package uk.gov.hmcts.reform.draftstore.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DraftStoreRepository extends JpaRepository<DraftStoreEntity, UUID> {

    List<DraftStoreEntity> findByUserIdAndDraftTypeId(String userId, Integer draftTypeId);

    List<DraftStoreEntity> findByUserIdAndDraftTypeIdAndExpiresAtAfter(
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

    long deleteByExpiresAtBefore(OffsetDateTime now);
}
