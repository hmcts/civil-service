package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dashboard.entities.NotificationTemplateEntity;

import java.util.Optional;

@Transactional(readOnly = true)
@Repository
public interface NotificationTemplateRepository extends CrudRepository<NotificationTemplateEntity, Long> {

    Optional<NotificationTemplateEntity> findById(Long id);

    Optional<NotificationTemplateEntity> findByName(String scenarioReference);
}
