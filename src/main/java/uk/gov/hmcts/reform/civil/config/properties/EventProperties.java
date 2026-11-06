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
    // 2 is a deliberate conservative default, not a measured optimum. What the preview did
    // establish is that the mechanism works: max concurrent tasks equals this value (1, 2 and 3
    // each confirmed), and handler work exceeds one thread's worth of wall clock, so the extra
    // threads really do run.
    //
    // What it could not establish is a ranking. Task duration on that environment tracks elapsed
    // time rather than worker count: four runs over 16 hours gave medians of 292, 388, 475 and
    // 508ms in chronological order, and the slowest was a 2 client run, slower than the 3 client
    // one. Two clients measured twice reached 100% and then 70% of their own throughput ceiling.
    // Each functional run seeds more cases into the preview's single CCD data store, which is the
    // likely cause. Ranking counts needs interleaved runs or a fresh environment per run, not
    // sequential ones.
    //
    // So 2 is the smallest step that demonstrably adds a working thread. Whether 3 or more pays
    // off is open, and belongs in a production trial measuring CCD conflict rate and task
    // duration, where CCD is multi replica rather than the single pod a preview gets.
    protected int caseDrivenClients = 2;
    // tasks fetched per fetchAndLock on the case driven client. maxTasks caps tasks per request
    // across all topics on that client, not per topic, so this is the whole request budget.
    protected int caseDrivenMaxTasks = 1;
    // tasks fetched per fetchAndLock on the scheduler client. Left at 1 deliberately: these
    // handlers pace themselves with Thread.sleep, so fetching several would only queue up more
    // sequential sleeping on that one thread.
    protected int schedulerMaxTasks = 1;
}
