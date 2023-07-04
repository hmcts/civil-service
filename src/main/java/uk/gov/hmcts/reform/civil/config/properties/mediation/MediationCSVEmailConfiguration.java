package uk.gov.hmcts.reform.civil.config.properties.mediation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mediation.data")
public class MediationCSVEmailConfiguration {

    private String sender;
    private String recipient;
}
