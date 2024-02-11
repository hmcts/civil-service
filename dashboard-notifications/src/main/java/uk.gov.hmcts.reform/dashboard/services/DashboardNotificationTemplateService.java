package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.entities.NotificationTemplateEntity;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationTemplateRepository;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DashboardNotificationTemplateService {

    private final NotificationTemplateRepository notificationTemplateRepository;

    public DashboardNotificationTemplateService(NotificationTemplateRepository repository) {
        this.notificationTemplateRepository = repository;
    }

    public List<NotificationTemplateEntity> getAll() {
        return (List<NotificationTemplateEntity>) notificationTemplateRepository.findAll();
    }

    public Optional<NotificationTemplateEntity> findById(Long id) {
        return notificationTemplateRepository.findById(id);
    }

    public NotificationTemplateEntity create(NotificationTemplateEntity notificationTemplateEntity) {
        return notificationTemplateRepository.save(notificationTemplateEntity);
    }

    public NotificationTemplateEntity update(Long id, NotificationTemplateEntity notificationTemplateEntity) {
        NotificationTemplateEntity existingNotification = notificationTemplateRepository.findById(id).orElse(null);

        if (existingNotification != null) {
            existingNotification.builder()
                .role(notificationTemplateEntity.getRole())
                .name(notificationTemplateEntity.getName())
                .titleEn(notificationTemplateEntity.getTitleEn())
                .titleCy(notificationTemplateEntity.getTitleCy())
                .descriptionEn(notificationTemplateEntity.getDescriptionEn())
                .descriptionCy(notificationTemplateEntity.getDescriptionCy())
                .createdAt(notificationTemplateEntity.getCreatedAt())
                .timeToLive(notificationTemplateEntity.getTimeToLive());

            return notificationTemplateRepository.save(existingNotification);
        } else {
            return null;
        }
    }

    public void delete(Long id) {
        notificationTemplateRepository.deleteById(id);
    }
}
