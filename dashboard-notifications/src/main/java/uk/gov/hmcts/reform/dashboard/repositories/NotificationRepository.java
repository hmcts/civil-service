package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Repository
public interface NotificationRepository extends CrudRepository<NotificationEntity, UUID> {

    Optional<NotificationEntity> findById(UUID uuid);

    List<NotificationEntity> findByReferenceAndCitizenRole(String reference, String role);

    Optional<NotificationEntity> findByReferenceAndCitizenRoleAndDashboardNotificationsTemplatesId(
        String reference, String role, Long templateId);

    int deleteByNameAndReferenceAndCitizenRole(String name, String reference, String role);
}
