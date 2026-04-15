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

    @Modifying
    @Query(value = "DELETE FROM dbs.notification_action WHERE dashboard_notifications_id IN "
        + "(SELECT id FROM dbs.dashboard_notifications WHERE notification_name = :name "
        + "AND reference = :reference AND citizen_role = :role)", nativeQuery = true)
    void deleteActionsByNameAndReferenceAndCitizenRole(@Param("name") String name,
                                                       @Param("reference") String reference,
                                                       @Param("role") String role);

    @Modifying
    @Query(value = "DELETE FROM dbs.dashboard_notifications WHERE notification_name = :name "
        + "AND reference = :reference AND citizen_role = :role", nativeQuery = true)
    int deleteByNameAndReferenceAndCitizenRole(@Param("name") String name,
                                               @Param("reference") String reference,
                                               @Param("role") String role);

    @Modifying
    @Query(value = "DELETE FROM dbs.notification_action WHERE dashboard_notifications_id IN "
        + "(SELECT id FROM dbs.dashboard_notifications WHERE reference = :reference "
        + "AND citizen_role = :role)", nativeQuery = true)
    void deleteActionsByReferenceAndCitizenRole(@Param("reference") String reference,
                                                @Param("role") String role);

    @Modifying
    @Query(value = "DELETE FROM dbs.dashboard_notifications WHERE reference = :reference "
        + "AND citizen_role = :role", nativeQuery = true)
    int deleteByReferenceAndCitizenRole(@Param("reference") String reference,
                                        @Param("role") String role);

    @Modifying
    @Query(value = "DELETE FROM dbs.notification_action WHERE dashboard_notifications_id IN "
        + "(SELECT id FROM dbs.dashboard_notifications WHERE notification_name = :name "
        + "AND reference = :reference)", nativeQuery = true)
    void deleteActionsByNameAndReference(@Param("name") String name,
                                         @Param("reference") String reference);

    @Modifying
    @Query(value = "DELETE FROM dbs.dashboard_notifications WHERE notification_name = :name "
        + "AND reference = :reference", nativeQuery = true)
    int deleteByNameAndReference(@Param("name") String name,
                                 @Param("reference") String reference);
}
