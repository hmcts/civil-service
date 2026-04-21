package uk.gov.hmcts.reform.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dashboard.notifications.templates")
public class NotificationTemplatesProperties {

    private String location = "classpath:/notification-templates/*.json";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
