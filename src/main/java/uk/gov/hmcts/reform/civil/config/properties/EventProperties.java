package uk.gov.hmcts.reform.civil.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "async.event")
public class EventProperties {

    // duration for which the task is locked in milliseconds.
    protected long lockDuration;
    // default number of retry attempts for an external task
    protected int retryCount;
    // desired delay in milliseconds between retries.
    protected int retryDelay;
    // desired delay in milliseconds between task executions
    protected int dispatchDelay;
}
