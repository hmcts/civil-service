package uk.gov.hmcts.reform.dashboard.services;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.data.Notification;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationActionEntity;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationActionRepository;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Service
@Slf4j
@Transactional
@SuppressWarnings("deprecation")
public class DashboardNotificationService {

    private final DashboardNotificationsRepository dashboardNotificationsRepository;
    private final NotificationActionRepository notificationActionRepository;

    private final IdamApi idamApi;

    private final String clickAction = "Click";

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

        List<DashboardNotificationsEntity> dashboardNotificationsEntityList = dashboardNotificationsRepository
            .findByReferenceAndCitizenRole(ccdCaseIdentifier, roleType);

        return dashboardNotificationsEntityList.stream()
            .sorted(Comparator.comparing(DashboardNotificationsEntity::getCreatedAt, Comparator.reverseOrder()))
            .map(Notification::from)
            .toList();
    }

    public Map<String, List<Notification>> getAllCasesNotifications(List<String> ccdCaseIdentifiers, String roleType) {
        Map<String, List<Notification>> gaNotifications = new HashMap<>();
        ccdCaseIdentifiers.forEach(gaCaseId -> gaNotifications.put(gaCaseId, getNotifications(gaCaseId, roleType)));
        return gaNotifications;
    }

    public DashboardNotificationsEntity saveOrUpdate(DashboardNotificationsEntity notification) {

        DashboardNotificationsEntity updated = notification;
        if (nonNull(notification.getDashboardNotificationsTemplates())) {
            log.info("Query for dashboard notifications using notification reference= {}, citizenRole = {}, templateId = {}",
                     notification.getReference(),
                     notification.getCitizenRole(),
                     notification.getDashboardNotificationsTemplates().getId()
            );
            List<DashboardNotificationsEntity> existingNotification = dashboardNotificationsRepository
                .findByReferenceAndCitizenRoleAndDashboardNotificationsTemplatesId(
                    notification.getReference(), notification.getCitizenRole(),
                    notification.getDashboardNotificationsTemplates().getId()
                );

            log.info("Found {} dashboard notifications in database for reference {}",
                     nonNull(existingNotification) ? existingNotification.size() : null,
                     notification.getReference());
            if (nonNull(existingNotification) && !existingNotification.isEmpty()) {
                DashboardNotificationsEntity dashboardNotification = existingNotification.get(0);
                updated = notification.toBuilder().id(dashboardNotification.getId()).build();
                for (DashboardNotificationsEntity dashNotification : existingNotification) {
                    notificationActionRepository.deleteByDashboardNotificationAndActionPerformed(
                        dashNotification,
                        clickAction
                    );
                    log.info("Existing notification deleted reference = {}, id = {}", notification.getReference(), dashNotification.getId());
                }
            }
        } else {
            log.info("Existing notification not present reference = {}", notification.getReference());
        }

        return dashboardNotificationsRepository.save(updated);
    }

    public void deleteById(UUID id) {
        dashboardNotificationsRepository.deleteById(id);
    }

    public void recordClick(UUID id, String authToken) {
        Optional<DashboardNotificationsEntity> dashboardNotification = dashboardNotificationsRepository.findById(id);

        dashboardNotification.ifPresent(notification -> {
            NotificationActionEntity notificationAction = NotificationActionEntity.builder()
                .reference(notification.getReference())
                .dashboardNotification(notification)
                .actionPerformed(clickAction)
                .createdBy(idamApi.retrieveUserDetails(authToken).getFullName())
                .createdAt(OffsetDateTime.now())
                .build();

            if (nonNull(notification.getNotificationAction())
                && notification.getNotificationAction().getActionPerformed().equals(clickAction)) {
                notificationAction.setId(notification.getNotificationAction().getId());
            }
            notification.setNotificationAction(notificationAction);
            dashboardNotificationsRepository.save(notification);
        });
    }

    public int deleteByNameAndReferenceAndCitizenRole(String name, String reference, String citizenRole) {
        return dashboardNotificationsRepository.deleteByNameAndReferenceAndCitizenRole(name, reference, citizenRole);
    }

    public void deleteByReferenceAndCitizenRole(String reference, String citizenRole) {
        int deleted = dashboardNotificationsRepository.deleteByReferenceAndCitizenRole(reference, citizenRole);
        log.info("{} notifications removed for claim = {}", deleted, reference);
    }
}
