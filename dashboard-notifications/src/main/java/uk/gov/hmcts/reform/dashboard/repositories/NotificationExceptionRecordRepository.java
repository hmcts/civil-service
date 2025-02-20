package uk.gov.hmcts.reform.dashboard.repositories;

import uk.gov.hmcts.reform.dashboard.entities.NotificationExceptionRecordEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationExceptionRecordRepository extends CrudRepository<NotificationExceptionRecordEntity, UUID> {

    Optional<NotificationExceptionRecordEntity> findByReferenceAndEventId(String reference, String eventId);
}
