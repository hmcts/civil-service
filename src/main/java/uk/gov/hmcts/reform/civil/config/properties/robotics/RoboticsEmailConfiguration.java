package uk.gov.hmcts.reform.civil.config.properties.robotics;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "robotics.notification")
public class RoboticsEmailConfiguration {

    private String sender;
    private String recipient;
}
