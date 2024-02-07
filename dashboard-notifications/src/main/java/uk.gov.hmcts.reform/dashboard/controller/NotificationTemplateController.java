package uk.gov.hmcts.reform.dashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dashboard.data.NotificationTemplateEntity;
import uk.gov.hmcts.reform.dashboard.repository.NotificationTemplateRepository;

import java.util.List;
@RestController
@RequestMapping("/dashboard_notification_template")
public class NotificationTemplateController {

    @Autowired
    private NotificationTemplateRepository repository;

    @GetMapping("/")
    public List<NotificationTemplateEntity> getAll() {
        return (List<NotificationTemplateEntity>) repository.findAll();
    }

    @GetMapping("/{id}")
    public NotificationTemplateEntity getById(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }

    @PostMapping("/")
    public NotificationTemplateEntity create(@RequestBody NotificationTemplateEntity template) {
        return repository.save(template);
    }

    @PutMapping("/{id}")
    public NotificationTemplateEntity update(@PathVariable Long id, @RequestBody NotificationTemplateEntity template) {
        NotificationTemplateEntity existingTemplate = repository.findById(id).orElse(null);

        if (existingTemplate != null) {
            existingTemplate.setName(template.getName());
            existingTemplate.setEnHTML(template.getEnHTML());
            existingTemplate.setCyHTML(template.getCyHTML());
            existingTemplate.setRole(template.getRole());
            existingTemplate.setTimeToLive(template.getTimeToLive());
            existingTemplate.setCreatedAt(template.getCreatedAt());

            return repository.save(existingTemplate);
        } else {
            return null;
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

}
