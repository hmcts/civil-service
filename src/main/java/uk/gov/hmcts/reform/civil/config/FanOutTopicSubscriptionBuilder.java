package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Applies one subscription definition to several {@link org.camunda.bpm.client.ExternalTaskClient}s
 * so a single topic can be served by more than one subscription thread.
 *
 * <p>Needed because the case driven side is thread bound, not batch bound. Measured on the PR 8438
 * preview through a functional test run: in its densest minute the single case driven subscription
 * thread was 95% occupied (57.1s of 60s) running 126 tasks a minute, against a single thread
 * ceiling of 155 (637 tasks, mean 387ms). Raising {@code maxTasks} to 10 on that client did not
 * move peak queue depth, which stayed at 8 unlocked tasks across every configuration tried,
 * because a larger batch removes only the fetch round trip, worth about 6ms against a 387ms task.
 *
 * <p>Load is concentrated in one topic, so the clients cannot simply be given a topic each:
 * {@code processCaseEvent} was 335 of those 637 executions. Whichever client owned it would still
 * be the bottleneck. Instead every case driven client subscribes to every case driven topic and
 * Camunda's {@code fetchAndLock} hands each one a disjoint set of tasks, which is how an external
 * task topic is scaled horizontally.
 *
 * <p>Each delegate keeps its own builder, so the handler instance is shared between the resulting
 * subscriptions and must be thread safe. The handlers here hold no mutable per task state; they
 * resolve collaborators from the Spring context and operate on the {@code ExternalTask} argument.
 *
 * @see TopicRoutingExternalTaskClient
 */
public class FanOutTopicSubscriptionBuilder implements TopicSubscriptionBuilder {

    private final List<TopicSubscriptionBuilder> delegates;

    public FanOutTopicSubscriptionBuilder(List<TopicSubscriptionBuilder> delegates) {
        if (delegates.isEmpty()) {
            throw new IllegalArgumentException("A fan out subscription needs at least one delegate builder");
        }
        this.delegates = List.copyOf(delegates);
    }

    @Override
    public TopicSubscriptionBuilder lockDuration(long lockDuration) {
        delegates.forEach(d -> d.lockDuration(lockDuration));
        return this;
    }

    @Override
    public TopicSubscriptionBuilder handler(ExternalTaskHandler handler) {
        delegates.forEach(d -> d.handler(handler));
        return this;
    }

    @Override
    public TopicSubscriptionBuilder variables(String... variables) {
        delegates.forEach(d -> d.variables(variables));
        return this;
    }

    @Override
    public TopicSubscriptionBuilder localVariables(boolean localVariables) {
        delegates.forEach(d -> d.localVariables(localVariables));
        return this;
    }

    @Override
    public TopicSubscriptionBuilder businessKey(String businessKey) {
        delegates.forEach(d -> d.businessKey(businessKey));
        return this;
    }

    @Override
    public TopicSubscriptionBuilder processDefinitionId(String processDefinitionId) {
        delegates.forEach(d -> d.processDefinitionId(processDefinitionId));
        return this;
    }

    @Override
    public TopicSubscriptionBuilder processDefinitionIdIn(String... processDefinitionIds) {
        delegates.forEach(d -> d.processDefinitionIdIn(processDefinitionIds));
        return this;
    }

    @Override
    public TopicSubscriptionBuilder processDefinitionKey(String processDefinitionKey) {
        delegates.forEach(d -> d.processDefinitionKey(processDefinitionKey));
        return this;
    }

    @Override
    public TopicSubscriptionBuilder processDefinitionKeyIn(String... processDefinitionKeys) {
        delegates.forEach(d -> d.processDefinitionKeyIn(processDefinitionKeys));
        return this;
    }

    @Override
    public TopicSubscriptionBuilder processDefinitionVersionTag(String processDefinitionVersionTag) {
        delegates.forEach(d -> d.processDefinitionVersionTag(processDefinitionVersionTag));
        return this;
    }

    @Override
    public TopicSubscriptionBuilder processVariablesEqualsIn(Map<String, Object> processVariables) {
        delegates.forEach(d -> d.processVariablesEqualsIn(processVariables));
        return this;
    }

    @Override
    public TopicSubscriptionBuilder processVariableEquals(String name, Object value) {
        delegates.forEach(d -> d.processVariableEquals(name, value));
        return this;
    }

    @Override
    public TopicSubscriptionBuilder withoutTenantId() {
        delegates.forEach(TopicSubscriptionBuilder::withoutTenantId);
        return this;
    }

    @Override
    public TopicSubscriptionBuilder tenantIdIn(String... tenantIds) {
        delegates.forEach(d -> d.tenantIdIn(tenantIds));
        return this;
    }

    @Override
    public TopicSubscriptionBuilder includeExtensionProperties(boolean includeExtensionProperties) {
        delegates.forEach(d -> d.includeExtensionProperties(includeExtensionProperties));
        return this;
    }

    /**
     * Opens every delegate subscription. If one throws, the subscriptions already opened are closed
     * before the failure propagates, so a partly subscribed topic cannot be left running: half the
     * intended threads serving a topic is a silent capacity loss rather than a visible failure.
     */
    @Override
    public TopicSubscription open() {
        List<TopicSubscription> opened = new ArrayList<>(delegates.size());
        try {
            delegates.forEach(d -> opened.add(d.open()));
        } catch (RuntimeException e) {
            opened.forEach(FanOutTopicSubscriptionBuilder::closeQuietly);
            throw e;
        }
        return new FanOutTopicSubscription(opened);
    }

    private static void closeQuietly(TopicSubscription subscription) {
        try {
            subscription.close();
        } catch (RuntimeException ignored) {
            // already unwinding a failed open; the original cause is the one worth reporting
        }
    }
}
