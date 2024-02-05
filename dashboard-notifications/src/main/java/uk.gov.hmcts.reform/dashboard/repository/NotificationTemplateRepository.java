package uk.gov.hmcts.reform.dashboard.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.model.NotificationTemplate;
import javax.transaction.Transactional;

@Transactional
@Repository
public interface NotificationTemplateRepository extends CrudRepository<NotificationTemplate, Long> {
}
