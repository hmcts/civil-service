package uk.gov.hmcts.reform.dashboard.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NotificationTemplatesProperties.class)
public class NotificationTemplatesConfiguration {
}
