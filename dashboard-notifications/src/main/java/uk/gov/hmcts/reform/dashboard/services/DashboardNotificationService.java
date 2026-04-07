package uk.gov.hmcts.reform.dashboard.services;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dashboard.data.Notification;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Service
@Slf4j
@Transactional
@SuppressWarnings("deprecation")
public class DashboardNotificationService {

    private final DashboardNotificationsRepository dashboardNotificationsRepository;
    private static final Set<String> CASE_STAY_TEMPLATES = Set.of(
        "Notice.AAA6.CP.Case.Stayed.Claimant",
        "Notice.AAA6.CP.Case.Stayed.Defendant"
    );

    private final IdamApi idamApi;

    @Autowired
    public DashboardNotificationService(DashboardNotificationsRepository dashboardNotificationsRepository,
                                        IdamApi idamApi) {
        this.dashboardNotificationsRepository = dashboardNotificationsRepository;
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

        return result.stream()
            .map(Notification::from)
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
            log.debug("Query for dashboard notifications using notification reference= {}, citizenRole = {}, templateName = {}",
                notification.getReference(),
                notification.getCitizenRole(),
                notification.getName()
            );
            List<DashboardNotificationsEntity> existingNotifications = dashboardNotificationsRepository
                .findByReferenceAndCitizenRoleAndName(
                    notification.getReference(),
                    notification.getCitizenRole(),
                    notification.getName()
                );

            log.debug("Found {} dashboard notifications in database for reference {}",
                nonNull(existingNotifications) ? existingNotifications.size() : null,
                notification.getReference());
            if (nonNull(existingNotifications) && !existingNotifications.isEmpty()) {
                DashboardNotificationsEntity latest = getLatestNotification(existingNotifications);
                if (existingNotifications.size() > 1) {
                    log.warn(
                        "Deduplicate notification for reference {} role {} name {}. Found {} extra records.",
                        latest.getReference(), latest.getCitizenRole(), latest.getName(),
                        existingNotifications.size() - 1
                    );
                    removeOrphanedNotifications(existingNotifications, latest.getId());
                }
                updated = copyNotification(notification);
                updated.setId(latest.getId());
                if (nonNull(latest.getCreatedAt())) {
                    updated.setCreatedAt(latest.getCreatedAt());
                    updated.setCreatedBy(latest.getCreatedBy());
                }
                updated.setClickedAt(latest.getClickedAt());
                updated.setClickedBy(latest.getClickedBy());
                log.info("Updating existing notification reference = {}", notification.getReference());
            } else {
                log.info("Saving new notification reference = {}", notification.getReference());
            }
        } else {
            log.warn("Notification 'name' not present reference = {}", notification.getReference());
        }

        return dashboardNotificationsRepository.save(updated);
    }

    public void deleteById(UUID id) {
        dashboardNotificationsRepository.deleteById(id);
    }

    public void recordClick(UUID id, String authToken) {
        dashboardNotificationsRepository.findById(id).ifPresent(notification -> {
            notification.setClickedBy(idamApi.retrieveUserDetails(authToken).getFullName());
            notification.setClickedAt(OffsetDateTime.now());
            dashboardNotificationsRepository.save(notification);
        });
    }

    public int deleteByNameAndReferenceAndCitizenRole(String name, String reference, String citizenRole) {
        return dashboardNotificationsRepository.deleteByNameAndReferenceAndCitizenRole(name, reference, citizenRole);
    }

    public int deleteByNameAndReference(String name, String reference) {
        return dashboardNotificationsRepository.deleteByNameAndReference(name, reference);
    }

    public void deleteByReferenceAndCitizenRole(String reference, String citizenRole) {
        int deleted = dashboardNotificationsRepository.deleteByReferenceAndCitizenRole(reference, citizenRole);
        log.info("{} notifications removed for claim = {}", deleted, reference);
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
            notification.getTimeToLive(),
            notification.getClickedBy(),
            notification.getClickedAt()
        );
    }

    private DashboardNotificationsEntity getLatestNotification(List<DashboardNotificationsEntity> existingNotifications) {
        return existingNotifications.stream()
            .max(Comparator.comparing(
                     DashboardNotificationsEntity::getCreatedAt,
                     Comparator.nullsFirst(Comparator.naturalOrder())
                 )
                 .thenComparing(DashboardNotificationsEntity::getId))
            .orElse(existingNotifications.getFirst());
    }

    private void removeOrphanedNotifications(List<DashboardNotificationsEntity> existingNotifications, @NotNull UUID latestId) {
        existingNotifications.stream()
            .filter(e -> !e.getId().equals(latestId))
            .forEach(duplicate -> {
                try {
                    dashboardNotificationsRepository.deleteById(duplicate.getId());
                } catch (Exception e) {
                    log.warn("Failed to delete duplicate notification reference = {}", duplicate.getReference());
                }
            });
    }
}
