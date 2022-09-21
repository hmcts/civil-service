package uk.gov.hmcts.reform.civil.config.properties.hearing;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "hearing-notification")
public class HearingNotificationEmailConfiguration {

    private String receiver;
}
