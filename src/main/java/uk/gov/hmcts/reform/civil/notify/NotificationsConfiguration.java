package uk.gov.hmcts.reform.civil.notify;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import uk.gov.service.notify.NotificationClient;

@Configuration
@EnableRetry
public class NotificationsConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "notifications")
    public NotificationsProperties notificationsProperties() {
        return new NotificationsProperties();
    }

    @Bean
    public NotificationClient notificationClient(NotificationsProperties notificationsProperties) {
        return new NotificationClient(notificationsProperties.getGovNotifyApiKey());
    }

}
