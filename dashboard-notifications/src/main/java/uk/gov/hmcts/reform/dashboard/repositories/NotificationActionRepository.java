package uk.gov.hmcts.reform.dashboard.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationActionEntity;

@Repository
public interface NotificationActionRepository extends CrudRepository<NotificationActionEntity, Long> {

    void deleteByDashboardNotificationAndActionPerformed(DashboardNotificationsEntity entity, String action);
}
