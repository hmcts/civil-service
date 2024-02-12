package uk.gov.hmcts.reform.dashboard.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dashboard.entities.NotificationTemplateEntity;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationTemplateRepository;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationTemplateService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.List;

@RestController
@RequestMapping("/dashboard_notification_template")
public class NotificationTemplateController {

    private final DashboardNotificationTemplateService dashboardNotificationTemplateService;

    @Autowired
    public NotificationTemplateController(DashboardNotificationTemplateService dashboardNotificationTemplateService) {

        this.dashboardNotificationTemplateService = dashboardNotificationTemplateService;
    }

    @GetMapping("/")
    public List<NotificationTemplateEntity> getAll() {
        return (List<NotificationTemplateEntity>) dashboardNotificationTemplateService.getAll();
    }

    @GetMapping("/{id}")
    public NotificationTemplateEntity getById(@PathVariable Long id) {
        return dashboardNotificationTemplateService.findById(id).orElse(null);
    }

    @PostMapping("/")
    public NotificationTemplateEntity create(@RequestBody NotificationTemplateEntity template) {
        return dashboardNotificationTemplateService.create(template);
    }

    @PutMapping("/{id}")
    public NotificationTemplateEntity update(@PathVariable Long id, @RequestBody NotificationTemplateEntity template) {

        dashboardNotificationTemplateService.update(id, template);

        return template;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        dashboardNotificationTemplateService.deleteById(id);
    }

}
