package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "http")
public class HttpConfiguration {

    private int timeout;
    private int requestTimeout;
    private int readTimeout;
}
