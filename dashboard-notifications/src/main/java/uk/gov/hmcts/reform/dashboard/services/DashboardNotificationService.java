package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.data.Notification;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class DashboardNotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public DashboardNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationEntity> getAll() {
        return (List<NotificationEntity>) notificationRepository.findAll();
    }

    public Optional<NotificationEntity> getNotification(UUID id) {
        return notificationRepository.findById(id);
    }

    public List<Notification> getNotifications(String ccdCaseIdentifier, String roleType) {

        List<NotificationEntity> notificationEntityList = notificationRepository
            .findByReferenceAndCitizenRole(ccdCaseIdentifier, roleType);

        return notificationEntityList.stream()
            .map(Notification::from)
            .collect(Collectors.toList());
    }

    public NotificationEntity saveOrUpdate(NotificationEntity notification) {
        Optional<NotificationEntity> existingNotification = notificationRepository
            .findByReferenceAndCitizenRoleAndDashboardNotificationsTemplatesId(
                notification.getReference(), notification.getCitizenRole(),
                notification.getDashboardNotificationsTemplates().getId()
            );

        NotificationEntity updated = notification;
        if (existingNotification.isPresent()) {
            updated = notification.toBuilder().id(existingNotification.get().getId()).build();
        }
        return notificationRepository.save(updated);

    }

    public void deleteById(UUID id) {
        notificationRepository.deleteById(id);
    }

    public int deleteByNameAndReferenceAndCitizenRole(String name, String reference, String citizenRole) {
        return notificationRepository.deleteByNameAndReferenceAndCitizenRole(name, reference, citizenRole);
    }
}
