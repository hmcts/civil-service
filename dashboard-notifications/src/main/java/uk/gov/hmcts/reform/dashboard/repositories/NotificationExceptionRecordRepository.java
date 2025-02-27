package uk.gov.hmcts.reform.dashboard.repositories;

import uk.gov.hmcts.reform.dashboard.entities.NotificationExceptionId;
import uk.gov.hmcts.reform.dashboard.entities.NotificationExceptionRecordEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface NotificationExceptionRecordRepository extends CrudRepository<NotificationExceptionRecordEntity, UUID> {

    Optional<NotificationExceptionRecordEntity> findNotificationExceptionRecordEntitiesByNotificationExceptionId(
        NotificationExceptionId notificationExceptionId);

    void deleteByNotificationExceptionId(NotificationExceptionId notificationExceptionId);
}
