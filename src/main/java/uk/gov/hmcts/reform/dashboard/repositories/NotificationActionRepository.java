package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.entities.NotificationActionEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationActionRepository extends CrudRepository<NotificationActionEntity, Long> {

    List<NotificationActionEntity> findByDashboardNotificationIdIn(List<UUID> dashboardNotificationIds);

    void deleteByDashboardNotificationIdAndActionPerformed(UUID dashboardNotificationId, String actionPerformed);

    @Modifying
    @Query("DELETE FROM NotificationActionEntity a WHERE a.dashboardNotificationId IN :ids")
    void deleteByDashboardNotificationIdIn(@Param("ids") List<UUID> ids);
}
