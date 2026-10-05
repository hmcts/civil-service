package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;

import java.util.Set;

/**
 * Routes each topic subscription to one of two real {@link ExternalTaskClient}s so that batch
 * schedulers cannot block case driven work.
 *
 * <p>The Camunda client creates one {@code TopicSubscriptionManager} per client, and that manager
 * is a single thread which fetches at most {@code maxTasks} per request and runs each handler
 * inline before fetching again. With one client every subscription shares that thread, so a slow
 * or deliberately paced handler stalls pickup for all of them. Confirmed against
 * camunda-external-task-client 7.24.0: {@code acquire()} puts every subscription into one
 * {@code fetchAndLock} request and {@code maxTasks} is a top level field of
 * {@code FetchAndLockRequestDto}, not a per topic allowance.
 *
 * <p>Splitting the clients gives each group its own thread. Scheduler topics are the batch
 * dispatchers: they call {@code BaseExternalTaskHandler.throttle()}, which sleeps on the
 * subscription thread once a batch exceeds 25, or they process very large batches. Case driven
 * topics are everything else, including {@code START_BUSINESS_PROCESS} and
 * {@code END_BUSINESS_PROCESS}, where pickup latency is what users feel.
 *
 * <p>Unknown topics route to the case driven client. That is deliberate: it is the behaviour a
 * topic has today, so adding a subscription without touching the configuration cannot change how
 * an existing one is served.
 *
 * <p>This does not reduce the total time spent sleeping. It confines it to the scheduler client,
 * so the cost is one client's capacity rather than the whole service's. Moving the pacing off the
 * worker thread is a separate change, and the downstream protection it provides needs preserving.
 *
 * @see ExternalTaskListenerConfiguration
 */
public class TopicRoutingExternalTaskClient implements ExternalTaskClient {

    private final ExternalTaskClient caseDrivenClient;
    private final ExternalTaskClient schedulerClient;
    private final Set<String> schedulerTopics;

    public TopicRoutingExternalTaskClient(ExternalTaskClient caseDrivenClient,
                                          ExternalTaskClient schedulerClient,
                                          Set<String> schedulerTopics) {
        this.caseDrivenClient = caseDrivenClient;
        this.schedulerClient = schedulerClient;
        this.schedulerTopics = Set.copyOf(schedulerTopics);
    }

    /**
     * Returns the client that will serve this topic. Package private so the routing can be
     * asserted directly rather than inferred from a subscription side effect.
     */
    ExternalTaskClient clientFor(String topicName) {
        return schedulerTopics.contains(topicName) ? schedulerClient : caseDrivenClient;
    }

    @Override
    public TopicSubscriptionBuilder subscribe(String topicName) {
        return clientFor(topicName).subscribe(topicName);
    }

    @Override
    public void start() {
        caseDrivenClient.start();
        schedulerClient.start();
    }

    @Override
    public void stop() {
        caseDrivenClient.stop();
        schedulerClient.stop();
    }

    /**
     * Active only while both underlying clients are, so a half stopped pair never reports healthy.
     */
    @Override
    public boolean isActive() {
        return caseDrivenClient.isActive() && schedulerClient.isActive();
    }
}
