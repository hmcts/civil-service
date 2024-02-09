package uk.gov.hmcts.reform.dashboard.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.data.NotificationTemplateEntity;
import uk.gov.hmcts.reform.dashboard.repository.NotificationTemplateRepository;
import java.util.List;

@Service
public class DashboardNotificationTemplateService {

    private final NotificationTemplateRepository repository;

    public DashboardNotificationTemplateService(NotificationTemplateRepository repository) {
        this.repository = repository;
    }

    public List<NotificationTemplateEntity> getAll() {
        return (List<NotificationTemplateEntity>) repository.findAll();
    }

    public NotificationTemplateEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public NotificationTemplateEntity create(NotificationTemplateEntity notificationTemplateEntity) {
        return repository.save(notificationTemplateEntity);
    }

    public NotificationTemplateEntity update(Long id, NotificationTemplateEntity notificationTemplateEntity) {
        NotificationTemplateEntity existingNotification = repository.findById(id).orElse(null);

        if (existingNotification != null) {
            existingNotification.setNotificationRole(notificationTemplateEntity.getNotificationRole());
            existingNotification.setTemplateName(notificationTemplateEntity.getTemplateName());
            existingNotification.setEnHtml(notificationTemplateEntity.getEnHtml());
            existingNotification.setCyHtml(notificationTemplateEntity.getCyHtml());
            existingNotification.setCreatedAt(notificationTemplateEntity.getCreatedAt());
            existingNotification.setTimeToLive(notificationTemplateEntity.getTimeToLive());

            return repository.save(existingNotification);
        } else {
            return null;
        }
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
