package uk.gov.hmcts.reform.unspec.sendgrid.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("sendgrid")
public class SendGridProperties {

    private String apiKey;
    private Boolean test;
    private String host;
    private String version;
}
