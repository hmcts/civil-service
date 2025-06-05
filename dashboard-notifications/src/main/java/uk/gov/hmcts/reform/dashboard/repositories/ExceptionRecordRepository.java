package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.entities.ExceptionRecordEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExceptionRecordRepository extends CrudRepository<ExceptionRecordEntity, UUID> {

    Optional<ExceptionRecordEntity> findByIdempotencyKey(String idempotencyKey);

    void deleteByIdempotencyKey(String idempotencyKey);
}
