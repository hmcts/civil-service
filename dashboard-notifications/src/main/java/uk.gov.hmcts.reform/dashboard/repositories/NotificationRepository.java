package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;

import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;

@Transactional
@Repository
public interface NotificationRepository extends CrudRepository<NotificationEntity, UUID> {

    Optional<NotificationEntity> findById(UUID uuid);
}
