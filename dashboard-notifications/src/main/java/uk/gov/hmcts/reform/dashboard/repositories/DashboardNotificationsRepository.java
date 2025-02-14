package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DashboardNotificationsRepository extends CrudRepository<DashboardNotificationsEntity, UUID> {

    Optional<DashboardNotificationsEntity> findById(UUID uuid);

    List<DashboardNotificationsEntity> findByReferenceAndCitizenRole(String reference, String role);

    List<DashboardNotificationsEntity> findByReferenceAndCitizenRoleAndDashboardNotificationsTemplatesId(
        String reference, String role, Long templateId);

    int deleteByNameAndReferenceAndCitizenRole(String name, String reference, String role);

    int deleteByReferenceAndCitizenRole(String reference, String role);
}
