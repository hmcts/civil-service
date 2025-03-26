package uk.gov.hmcts.reform.dashboard.repositories;

import uk.gov.hmcts.reform.dashboard.entities.ExceptionRecordEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ExceptionRecordRepository extends CrudRepository<ExceptionRecordEntity, UUID> {

    Optional<ExceptionRecordEntity> findByIdempotencyKey(String idempotencyKey);

    void deleteByIdempotencyKey(String idempotencyKey);
}
