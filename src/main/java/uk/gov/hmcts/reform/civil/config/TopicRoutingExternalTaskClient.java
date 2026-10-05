package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Routes each topic subscription onto the real {@link ExternalTaskClient}s that should serve it, so
 * that batch schedulers cannot block case driven work and so the case driven side can be given more
 * than one thread.
 *
 * <p>The Camunda client creates one {@code TopicSubscriptionManager} per client, and that manager
 * is a single thread which fetches at most {@code maxTasks} per request and runs each handler
 * inline before fetching again. With one client every subscription shares that thread, so a slow
 * or deliberately paced handler stalls pickup for all of them. Confirmed against
 * camunda-external-task-client 7.24.0: {@code acquire()} puts every subscription into one
 * {@code fetchAndLock} request and {@code maxTasks} is a top level field of
 * {@code FetchAndLockRequestDto}, not a per topic allowance.
 *
 * <p>Scheduler topics are the batch dispatchers: they call {@code BaseExternalTaskHandler.throttle()},
 * which sleeps on the subscription thread once a batch exceeds 25, or they process very large
 * batches. They get one client of their own, and one is enough because more threads would only
 * queue up more sequential sleeping.
 *
 * <p>Case driven topics are everything else, including {@code START_BUSINESS_PROCESS} and
 * {@code END_BUSINESS_PROCESS}, where pickup latency is what users feel. They are served by a list
 * of clients rather than one, and every client in that list subscribes to every case driven topic.
 * Giving each client a share of the topics would not work: {@code processCaseEvent} alone was 335
 * of the 637 task executions measured through a functional test run, so whichever client owned it
 * would still be the bottleneck. Subscribing all of them to all topics lets {@code fetchAndLock}
 * hand each client a disjoint set of tasks instead.
 *
 * <p>A list of one behaves exactly as a single client did, including returning the client's own
 * subscription builder rather than a wrapper, so the concurrency can be turned off in configuration
 * without taking a different code path.
 *
 * <p>Unknown topics route to the case driven clients. That is deliberate: it is the behaviour a
 * topic has today, so adding a subscription without touching the configuration cannot change how
 * an existing one is served.
 *
 * <p>Note what more than one case driven client changes behaviourally. Today two case driven tasks
 * never run at the same instant, so two concurrent process instances on the same case are
 * serialised by the single thread. With several clients they can run together, which can produce
 * CCD optimistic locking conflicts on that case where previously it could not. The BPMN keeps tasks
 * within one process instance sequential, so this applies only to a case carrying concurrent
 * instances. Conflict rate is the measurement that gates raising the client count, and the
 * production baseline is 8 "Duplicated attempt" lines in 30 days.
 *
 * @see ExternalTaskListenerConfiguration
 */
public class TopicRoutingExternalTaskClient implements ExternalTaskClient {

    private final List<ExternalTaskClient> caseDrivenClients;
    private final ExternalTaskClient schedulerClient;
    private final Set<String> schedulerTopics;

    public TopicRoutingExternalTaskClient(ExternalTaskClient caseDrivenClient,
                                          ExternalTaskClient schedulerClient,
                                          Set<String> schedulerTopics) {
        this(List.of(caseDrivenClient), schedulerClient, schedulerTopics);
    }

    public TopicRoutingExternalTaskClient(List<ExternalTaskClient> caseDrivenClients,
                                          ExternalTaskClient schedulerClient,
                                          Set<String> schedulerTopics) {
        if (caseDrivenClients.isEmpty()) {
            throw new IllegalArgumentException("At least one case driven external task client is required");
        }
        this.caseDrivenClients = List.copyOf(caseDrivenClients);
        this.schedulerClient = schedulerClient;
        this.schedulerTopics = Set.copyOf(schedulerTopics);
    }

    /**
     * Returns every client that will serve this topic. Package private so the routing can be
     * asserted directly rather than inferred from a subscription side effect.
     */
    List<ExternalTaskClient> clientsFor(String topicName) {
        return schedulerTopics.contains(topicName) ? List.of(schedulerClient) : caseDrivenClients;
    }

    @Override
    public TopicSubscriptionBuilder subscribe(String topicName) {
        List<ExternalTaskClient> targets = clientsFor(topicName);
        if (targets.size() == 1) {
            return targets.get(0).subscribe(topicName);
        }
        List<TopicSubscriptionBuilder> builders = new ArrayList<>(targets.size());
        targets.forEach(client -> builders.add(client.subscribe(topicName)));
        return new FanOutTopicSubscriptionBuilder(builders);
    }

    @Override
    public void start() {
        allClients().forEach(ExternalTaskClient::start);
    }

    @Override
    public void stop() {
        allClients().forEach(ExternalTaskClient::stop);
    }

    /**
     * Active only while every underlying client is, so a partly stopped set never reports healthy.
     * One dead case driven client means a share of case work has silently stopped being picked up.
     */
    @Override
    public boolean isActive() {
        return allClients().allMatch(ExternalTaskClient::isActive);
    }

    private Stream<ExternalTaskClient> allClients() {
        return Stream.concat(caseDrivenClients.stream(), Stream.of(schedulerClient));
    }
}
