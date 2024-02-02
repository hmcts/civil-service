package uk.gov.hmcts.reform.dashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.dashboard.model.NotificationTemplate;
import uk.gov.hmcts.reform.dashboard.repository.NotificationTemplateRepository;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/dashboard_notification_template")
public class NotificationTemplateController {
    @Autowired
    private NotificationTemplateRepository repository;

    @GetMapping("/")
    public List<NotificationTemplate> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public NotificationTemplate getById(@PathVariable UUID id) {
        return repository.findById(id).orElse(null);
    }

    @PostMapping("/")
    public NotificationTemplate create(@RequestBody NotificationTemplate template) {
        return repository.save(template);
    }

    @PutMapping("/{id}")
    public NotificationTemplate update(@PathVariable UUID id, @RequestBody NotificationTemplate template) {
        NotificationTemplate existingTemplate = repository.findById(id).orElse(null);

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
    public void delete(@PathVariable UUID id) {
        repository.deleteById(id);
    }
}
