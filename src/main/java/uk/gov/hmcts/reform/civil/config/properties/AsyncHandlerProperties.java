package uk.gov.hmcts.reform.civil.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "async.handler")
public class AsyncHandlerProperties {

    protected int corePoolSize;
    protected int maxPoolSize;
    protected int queueCapacity;
}
