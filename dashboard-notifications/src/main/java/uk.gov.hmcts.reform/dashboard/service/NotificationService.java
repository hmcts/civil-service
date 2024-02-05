package uk.gov.hmcts.reform.dashboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.data.NotificationEntity;
import uk.gov.hmcts.reform.dashboard.repository.NotificationRepository;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository repository;

    public List<NotificationEntity> getAll() {
        return (List<NotificationEntity>) repository.findAll();
    }

    public NotificationEntity getById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public NotificationEntity create(NotificationEntity notification) {
        return repository.save(notification);
    }

    public NotificationEntity update(UUID id, NotificationEntity notification) {
        NotificationEntity existingNotification = repository.findById(id).orElse(null);

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

            return repository.save(existingNotification);
        } else {
            return null;
        }
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
