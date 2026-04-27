package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dashboard.data.Notification;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationActionEntity;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationActionRepository;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@Slf4j
@Transactional
@SuppressWarnings("deprecation")
public class DashboardNotificationService {

    private static final Set<String> CASE_STAY_TEMPLATES = Set.of(
        "Notice.AAA6.CP.Case.Stayed.Claimant",
        "Notice.AAA6.CP.Case.Stayed.Defendant"
    );
    private static final String CLICK_ACTION = "Click";

    private final DashboardNotificationsRepository dashboardNotificationsRepository;
    private final NotificationActionRepository notificationActionRepository;

    private final IdamApi idamApi;

    @Autowired
    public DashboardNotificationService(DashboardNotificationsRepository dashboardNotificationsRepository,
                                        NotificationActionRepository notificationActionRepository, IdamApi idamApi) {
        this.dashboardNotificationsRepository = dashboardNotificationsRepository;
        this.notificationActionRepository = notificationActionRepository;
        this.idamApi = idamApi;
    }

    public List<DashboardNotificationsEntity> getAll() {
        return (List<DashboardNotificationsEntity>) dashboardNotificationsRepository.findAll();
    }

    public Optional<DashboardNotificationsEntity> getNotification(UUID id) {
        return dashboardNotificationsRepository.findById(id);
    }

    public List<Notification> getNotifications(String ccdCaseIdentifier, String roleType) {

        List<DashboardNotificationsEntity> all =
            dashboardNotificationsRepository.findByReferenceAndCitizenRole(ccdCaseIdentifier, roleType);

        // Sort all notifications
        List<DashboardNotificationsEntity> sortedAllNotifications = all.stream()
            .sorted(Comparator.comparing(DashboardNotificationsEntity::getCreatedAt).reversed())
            .toList();

        // Filter stay lifted
        List<DashboardNotificationsEntity> caseStayedNotification = sortedAllNotifications.stream()
            .filter(n -> n.getName() != null && CASE_STAY_TEMPLATES.contains(n.getName()))
            .toList();

        // If found, return only those, otherwise return all
        List<DashboardNotificationsEntity> result =
            caseStayedNotification.isEmpty() ? sortedAllNotifications : caseStayedNotification;

        List<UUID> notificationIds = result.stream().map(DashboardNotificationsEntity::getId).toList();
        Map<UUID, NotificationActionEntity> latestActions = findLatestNotificationActions(notificationIds);

        return result.stream()
            .map(entity -> Notification.from(entity, latestActions.get(entity.getId())))
            .toList();
    }

    public List<DashboardNotificationsEntity> getDashboardNotifications(String ccdCaseIdentifier, String roleType) {
        return dashboardNotificationsRepository
            .findByReferenceAndCitizenRole(ccdCaseIdentifier, roleType);
    }

    public Map<String, List<Notification>> getAllCasesNotifications(List<String> ccdCaseIdentifiers, String roleType) {
        Map<String, List<Notification>> gaNotifications = new HashMap<>();
        ccdCaseIdentifiers.forEach(gaCaseId -> gaNotifications.put(gaCaseId, getNotifications(gaCaseId, roleType)));
        return gaNotifications;
    }

    public DashboardNotificationsEntity saveOrUpdate(DashboardNotificationsEntity notification) {

        DashboardNotificationsEntity updated = notification;
        if (nonNull(notification.getName())) {
            log.info("Query for dashboard notifications using notification reference= {}, citizenRole = {}, templateName = {}",
                notification.getReference(), notification.getCitizenRole(), notification.getName());
            List<DashboardNotificationsEntity> existingNotifications = dashboardNotificationsRepository
                .findByReferenceAndCitizenRoleAndName(
                    notification.getReference(),
                    notification.getCitizenRole(),
                    notification.getName()
                );

            log.info("Found {} dashboard notifications in database for reference {}",
                     existingNotifications.size(), notification.getReference());

            if (!existingNotifications.isEmpty()) {
                DashboardNotificationsEntity match = findMatchingOrLatestNotification(existingNotifications, notification);
                updated = copyNotification(notification);
                updated.setId(match.getId());
                // Delete click actions so the notification is unread after content changes.
                // This applies to both reconfigured notifications and reapplied scenarios where
                // the content may have been updated.
                for (DashboardNotificationsEntity dashNotification : existingNotifications) {
                    notificationActionRepository.deleteByDashboardNotificationIdAndActionPerformed(
                        dashNotification.getId(),
                        CLICK_ACTION
                    );
                    log.debug("Existing {} notification action deleted reference = {}, id = {}",
                              CLICK_ACTION, notification.getReference(), dashNotification.getId());
                }
            } else {
                log.debug("Existing notification not present reference = {}", notification.getReference());
            }
        } else {
            log.warn("Notification 'name' not provided reference = {}", notification.getReference());
        }

        return dashboardNotificationsRepository.save(updated);
    }

    public void recordClick(UUID id, String authToken) {
        dashboardNotificationsRepository.findById(id).ifPresent(notification -> {
            NotificationActionEntity notificationAction = new NotificationActionEntity(
                null,
                notification.getReference(),
                CLICK_ACTION,
                idamApi.retrieveUserDetails(authToken).getFullName(),
                OffsetDateTime.now(),
                id
            );

            findLatestClickAction(id).ifPresent(e -> notificationAction.setId(e.getId()));

            notificationActionRepository.save(notificationAction);
        });
    }

    public void deleteById(UUID id) {
        performBulkDelete(List.of(id));
    }

    public int deleteByNameAndReferenceAndCitizenRole(String name, String reference, String citizenRole) {
        List<DashboardNotificationsEntity> notifications = dashboardNotificationsRepository
            .findByReferenceAndCitizenRoleAndName(reference, citizenRole, name);

        performBulkDelete(notifications.stream().map(DashboardNotificationsEntity::getId).toList());

        return notifications.size();
    }

    public int deleteByNameAndReference(String name, String reference) {
        List<DashboardNotificationsEntity> notifications = dashboardNotificationsRepository
            .findByReferenceAndName(reference, name);

        performBulkDelete(notifications.stream().map(DashboardNotificationsEntity::getId).toList());

        return notifications.size();
    }

    public void deleteByReferenceAndCitizenRole(String reference, String citizenRole) {
        List<DashboardNotificationsEntity> notifications = dashboardNotificationsRepository
            .findByReferenceAndCitizenRole(reference, citizenRole);

        performBulkDelete(notifications.stream().map(DashboardNotificationsEntity::getId).toList());

        log.info("{} notifications removed for claim = {}", notifications.size(), reference);
    }

    private void performBulkDelete(List<UUID> ids) {
        if (ids.isEmpty()) {
            return;
        }

        notificationActionRepository.deleteByDashboardNotificationIdIn(ids);
        dashboardNotificationsRepository.deleteByDashboardNotificationIdIn(ids);
    }

    private DashboardNotificationsEntity copyNotification(DashboardNotificationsEntity notification) {
        return new DashboardNotificationsEntity(
            notification.getId(),
            notification.getReference(),
            notification.getName(),
            notification.getCitizenRole(),
            notification.getTitleEn(),
            notification.getDescriptionEn(),
            notification.getTitleCy(),
            notification.getDescriptionCy(),
            notification.getParams(),
            notification.getCreatedBy(),
            notification.getCreatedAt(),
            notification.getUpdatedBy(),
            notification.getUpdatedOn(),
            notification.getDeadline(),
            notification.getTimeToLive()
        );
    }

    private Optional<NotificationActionEntity> findLatestClickAction(UUID dashboardNotificationId) {
        return notificationActionRepository
            .findByDashboardNotificationIdIn(List.of(dashboardNotificationId))
            .stream()
            .filter(a -> CLICK_ACTION.equals(a.getActionPerformed()))
            .max(Comparator.comparing(
                NotificationActionEntity::getCreatedAt,
                Comparator.nullsFirst(Comparator.naturalOrder())
            ));
    }

    private Map<UUID, NotificationActionEntity> findLatestNotificationActions(List<UUID> dashboardNotificationIds) {
        if (dashboardNotificationIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return notificationActionRepository
            .findByDashboardNotificationIdIn(dashboardNotificationIds)
            .stream()
            .collect(Collectors.groupingBy(
                NotificationActionEntity::getDashboardNotificationId,
                Collectors.collectingAndThen(
                    Collectors.maxBy(Comparator.comparing(
                        NotificationActionEntity::getCreatedAt,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                    )),
                    opt -> opt.orElse(null)
                )
            ));
    }

    private DashboardNotificationsEntity findMatchingOrLatestNotification(List<DashboardNotificationsEntity> existingNotifications,
                                                                          DashboardNotificationsEntity notification) {
        return existingNotifications.stream()
            .filter(n -> n.getId().equals(notification.getId()))
            .findFirst()
            .orElseGet(() -> existingNotifications.stream()
                .max(Comparator.comparing(
                    DashboardNotificationsEntity::getCreatedAt,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ).thenComparing(DashboardNotificationsEntity::getId))
                .orElse(existingNotifications.getFirst()));
    }
}
