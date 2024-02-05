package uk.gov.hmcts.reform.dashboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.model.NotificationTemplate;
import uk.gov.hmcts.reform.dashboard.repository.NotificationTemplateRepository;


import java.util.List;
import java.util.UUID;

@Service
public class NotificationTemplateService {
    @Autowired
    private NotificationTemplateRepository repository;

    public List<NotificationTemplate> getAll() {
        return repository.findAll();
    }

    public NotificationTemplate getById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public NotificationTemplate create(NotificationTemplate notificationTemplate) {
        return repository.save(notificationTemplate);
    }

    public NotificationTemplate update(UUID id, NotificationTemplate notificationTemplate) {
        NotificationTemplate existingNotification = repository.findById(id).orElse(null);

        if (existingNotification != null) {
            existingNotification.setRole(notificationTemplate.getRole());
            existingNotification.setName(notificationTemplate.getName());
            existingNotification.setEnHTML(notificationTemplate.getEnHTML());
            existingNotification.setCyHTML(notificationTemplate.getCyHTML());
            existingNotification.setCreatedAt(notificationTemplate.getCreatedAt());
            existingNotification.setTimeToLive(notificationTemplate.getTimeToLive());


            return repository.save(existingNotification);
        } else {
            return null;
        }
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
