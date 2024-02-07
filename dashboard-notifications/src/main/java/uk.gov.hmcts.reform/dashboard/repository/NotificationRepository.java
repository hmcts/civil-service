package uk.gov.hmcts.reform.dashboard.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.data.NotificationEntity;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;

@Transactional
@Repository
public interface NotificationRepository extends CrudRepository<NotificationEntity, UUID> {

    @Override
    Optional<NotificationEntity> findById(UUID uuid);
}
