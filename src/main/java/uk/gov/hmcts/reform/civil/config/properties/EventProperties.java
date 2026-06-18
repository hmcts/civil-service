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
    // async response timeout for the external task in milliseconds.
    protected long responseTimeout;
    // default number of retry attempts for an external task
    protected int retryCount;
    // desired backoff delay in milliseconds between retries.
    protected int backoffDelay;
    // desired dispatch delay in milliseconds between task executions
    protected int dispatchDelay;
}
