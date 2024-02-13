package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
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

    public NotificationEntity create(NotificationEntity notification) {
        return notificationRepository.save(notification);
    }

    public NotificationEntity update(UUID id, NotificationEntity notification) {
        NotificationEntity existingNotification = notificationRepository.findById(id).orElse(null);

        if (existingNotification != null) {
            existingNotification.builder().dashboardNotificationsTemplates(notification.getDashboardNotificationsTemplates())
            .reference(notification.getReference())
            .titleEn(notification.getTitleEn())
            .descriptionEn(notification.getDescriptionEn())
            .titleCy(notification.getTitleCy())
            .descriptionCy(notification.getDescriptionCy())
            .params(notification.getParams())
            .citizenRole(notification.getCitizenRole())
            .createdBy(notification.getCreatedBy())
            .createdAt(notification.getCreatedAt())
            .updatedBy(notification.getUpdatedBy())
            .updatedOn(notification.getUpdatedOn());

            return notificationRepository.save(existingNotification);
        } else {
            return null;
        }
    }

    public void deleteById(UUID id) {
        notificationRepository.deleteById(id);
    }
}
