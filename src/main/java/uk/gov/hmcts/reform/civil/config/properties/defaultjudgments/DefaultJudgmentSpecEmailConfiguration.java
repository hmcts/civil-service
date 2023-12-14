package uk.gov.hmcts.reform.civil.config.properties.defaultjudgments;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "notification.caseworker.spec")
public class DefaultJudgmentSpecEmailConfiguration {

    private String receiver;
}
