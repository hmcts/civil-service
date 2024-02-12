package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.entities.NotificationTemplateEntity;
import javax.transaction.Transactional;

@Transactional
@Repository
public interface NotificationTemplateRepository extends CrudRepository<NotificationTemplateEntity, Long> {
}
