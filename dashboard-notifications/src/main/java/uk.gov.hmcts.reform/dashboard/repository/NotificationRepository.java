package uk.gov.hmcts.reform.dashboard.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.data.NotificationEntity;

import javax.transaction.Transactional;
import java.util.UUID;
@Transactional
@Repository
public interface NotificationRepository extends CrudRepository<NotificationEntity, UUID> {
}
