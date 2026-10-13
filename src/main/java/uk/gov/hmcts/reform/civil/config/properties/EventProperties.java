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
    // initial wait in milliseconds the external task client backs off for after a failed fetchAndLock.
    protected long clientBackoffInitial;
    // multiplier applied to the client backoff for each consecutive failed fetchAndLock.
    protected float clientBackoffFactor;
    // maximum wait in milliseconds the external task client backs off for between fetchAndLock attempts;
    // only applied while calls are failing (a successful or empty poll resets the backoff to zero).
    protected long clientBackoffMax;
    // period of inactivity after which pooled Camunda HTTP connections are validated before reuse.
    protected long httpValidateAfterInactivityMs = 2000;
}
