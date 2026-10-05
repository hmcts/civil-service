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
    // number of case driven clients, so the number of subscription threads serving case driven
    // topics. Each one subscribes to every case driven topic and fetchAndLock gives each a disjoint
    // set of tasks. Raise this to add concurrency; 1 restores the previous single threaded
    // behaviour. More than one lets two process instances on the same case run at the same time,
    // so CCD conflict rate is the measurement that gates increasing it.
    //
    // 2 is where the measurements stop paying. On the preview: 1 client 126 tasks a minute at a
    // mean of 387ms, 2 clients 247 at 484ms, 3 clients 235 at 593ms. Two ran at exactly 100% of
    // their own ceiling; three reached 77% of theirs and left a thread idle at 88% occupancy while
    // every task got 23% slower. The slowdown is monotonic on every topic across near identical
    // workloads, so something shared downstream saturates at two, on that environment at least.
    //
    // 3 is still worth trying in production, where CCD is multi replica rather than the single
    // pod a preview gets, but it has to be measured there with conflict rate and task duration.
    // Preview cannot answer it.
    protected int caseDrivenClients = 2;
    // tasks fetched per fetchAndLock on the case driven client. maxTasks caps tasks per request
    // across all topics on that client, not per topic, so this is the whole request budget.
    protected int caseDrivenMaxTasks = 1;
    // tasks fetched per fetchAndLock on the scheduler client. Left at 1 deliberately: these
    // handlers pace themselves with Thread.sleep, so fetching several would only queue up more
    // sequential sleeping on that one thread.
    protected int schedulerMaxTasks = 1;
}
