package uk.gov.hmcts.reform.dashboard.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
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
            existingNotification.setNotificationTemplateEntity(notification.getNotificationTemplateEntity());
            existingNotification.setReference(notification.getReference());
            existingNotification.setEnHTML(notification.getEnHTML());
            existingNotification.setCyHTML(notification.getCyHTML());
            existingNotification.setParams(notification.getParams());
            existingNotification.setCreatedBy(notification.getCreatedBy());
            existingNotification.setCreatedAt(notification.getCreatedAt());
            existingNotification.setUpdatedBy(notification.getUpdatedBy());
            existingNotification.setUpdatedOn(notification.getUpdatedOn());

            return notificationRepository.save(existingNotification);
        } else {
            return null;
        }
    }

    public void delete(UUID id) {
        notificationRepository.deleteById(id);
    }
}
