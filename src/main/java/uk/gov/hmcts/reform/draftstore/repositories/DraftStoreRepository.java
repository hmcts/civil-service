package uk.gov.hmcts.reform.draftstore.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DraftStoreRepository extends JpaRepository<DraftStoreEntity, UUID> {

    List<DraftStoreEntity> findByUserIdAndDraftType(String userId, DraftType draftType);

    List<DraftStoreEntity> findByUserIdAndDraftTypeAndExpiresAtAfter(
        String userId,
        DraftType draftType,
        OffsetDateTime now
    );

    Optional<DraftStoreEntity> findByIdAndUserIdAndDraftTypeAndExpiresAtAfter(
        UUID id,
        String userId,
        DraftType draftType,
        OffsetDateTime now
    );

    long deleteByIdAndUserIdAndDraftType(UUID id, String userId, DraftType draftType);

    long deleteByExpiresAtBefore(OffsetDateTime now);
}
