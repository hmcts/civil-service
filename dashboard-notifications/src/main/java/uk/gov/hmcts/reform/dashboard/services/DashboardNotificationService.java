package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.data.Notification;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationActionEntity;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@Slf4j
@Transactional
public class DashboardNotificationService {

    private final DashboardNotificationsRepository dashboardNotificationsRepository;
  
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

        List<DashboardNotificationsEntity> dashboardNotificationsEntityList = dashboardNotificationsRepository
            .findByReferenceAndCitizenRole(ccdCaseIdentifier, roleType);

        return dashboardNotificationsEntityList.stream()
            .map(Notification::from)
            .collect(Collectors.toList());
    }

    public DashboardNotificationsEntity saveOrUpdate(DashboardNotificationsEntity notification) {
        Optional<DashboardNotificationsEntity> existingNotification = dashboardNotificationsRepository
            .findByReferenceAndCitizenRoleAndDashboardNotificationsTemplatesId(
                notification.getReference(), notification.getCitizenRole(),
                notification.getDashboardNotificationsTemplates().getId()
            );

        DashboardNotificationsEntity updated = notification;
        if (existingNotification.isPresent()) {
            updated = notification.toBuilder().id(existingNotification.get().getId()).build();
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
                .actionPerformed("Click")
                .createdBy(idamApi.retrieveUserDetails(authToken).getFullName())
                .createdAt(OffsetDateTime.now())
                .build();

            if (nonNull(notification.getNotificationAction())
                && notification.getNotificationAction().getActionPerformed().equals("Click")) {
                notificationAction.setId(notification.getNotificationAction().getId());
            }
            notification.setNotificationAction(notificationAction);
            dashboardNotificationsRepository.save(notification);
        });
    }

    public int deleteByNameAndReferenceAndCitizenRole(String name, String reference, String citizenRole) {
        return dashboardNotificationsRepository.deleteByNameAndReferenceAndCitizenRole(name, reference, citizenRole);
    }
}
