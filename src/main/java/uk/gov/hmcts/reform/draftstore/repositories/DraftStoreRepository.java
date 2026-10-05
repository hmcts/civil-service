package uk.gov.hmcts.reform.draftstore.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DraftStoreRepository extends JpaRepository<DraftStoreEntity, UUID> {

    List<DraftStoreEntity> findByUserIdAndDraftType(String userId, DraftType draftType);

    Optional<DraftStoreEntity> findByUserIdAndDraftTypeAndCaseIdIsNull(String userId, DraftType draftType);

    Optional<DraftStoreEntity> findByUserIdAndDraftTypeAndCaseIdIsNullAndExpiresAtAfter(
        String userId,
        DraftType draftType,
        OffsetDateTime now
    );

    Optional<DraftStoreEntity> findByUserIdAndDraftTypeAndCaseIdAndExpiresAtAfter(
        String userId,
        DraftType draftType,
        String caseId,
        OffsetDateTime now
    );

    Optional<DraftStoreEntity> findByIdAndUserIdAndDraftTypeAndExpiresAtAfter(
        UUID id,
        String userId,
        DraftType draftType,
        OffsetDateTime now
    );

    long deleteByIdAndUserIdAndDraftType(UUID id, String userId, DraftType draftType);

    @Query("SELECT d.id FROM DraftStoreEntity d WHERE d.expiresAt < :now ORDER BY d.id ASC")
    List<UUID> findExpiredIds(@Param("now") OffsetDateTime now, Pageable pageable);

    @Transactional
    @Modifying
    @Query("DELETE FROM DraftStoreEntity d WHERE d.id IN :ids")
    int deleteByIds(@Param("ids") List<UUID> ids);
}
