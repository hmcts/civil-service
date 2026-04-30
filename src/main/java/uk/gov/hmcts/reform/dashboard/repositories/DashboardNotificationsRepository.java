package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DashboardNotificationsRepository extends CrudRepository<DashboardNotificationsEntity, UUID> {

    Optional<DashboardNotificationsEntity> findById(UUID uuid);

    List<DashboardNotificationsEntity> findByReferenceAndCitizenRole(String reference, String role);

    List<DashboardNotificationsEntity> findByReferenceAndCitizenRoleAndName(
        String reference, String role, String name);

    List<DashboardNotificationsEntity> findByReferenceAndName(String reference, String name);

    @Modifying
    @Query("DELETE FROM DashboardNotificationsEntity d WHERE d.id IN :ids")
    void deleteByDashboardNotificationIdIn(@Param("ids") List<UUID> ids);
}
