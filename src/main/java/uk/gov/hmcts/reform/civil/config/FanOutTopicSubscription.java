package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscription;

import java.util.List;
import java.util.Map;

/**
 * One handle over the several real subscriptions that {@link FanOutTopicSubscriptionBuilder} opened
 * for a single topic, so a caller still holds one {@link TopicSubscription} per {@code subscribe}
 * call as the Camunda API implies.
 *
 * <p>Every delegate was built from the same definition, so the getters answer from the first one.
 * {@link #close()} closes all of them: closing only the first would leave the topic served by
 * fewer threads than intended and still appear to have been unsubscribed.
 */
public class FanOutTopicSubscription implements TopicSubscription {

    private final List<TopicSubscription> delegates;

    public FanOutTopicSubscription(List<TopicSubscription> delegates) {
        if (delegates.isEmpty()) {
            throw new IllegalArgumentException("A fan out subscription needs at least one delegate subscription");
        }
        this.delegates = List.copyOf(delegates);
    }

    /**
     * Closes every delegate. The first failure is rethrown only after the rest have been attempted,
     * so one bad subscription cannot leave the others open.
     */
    @Override
    public void close() {
        RuntimeException first = null;
        for (TopicSubscription delegate : delegates) {
            try {
                delegate.close();
            } catch (RuntimeException e) {
                if (first == null) {
                    first = e;
                }
            }
        }
        if (first != null) {
            throw first;
        }
    }

    private TopicSubscription first() {
        return delegates.get(0);
    }

    @Override
    public String getTopicName() {
        return first().getTopicName();
    }

    @Override
    public Long getLockDuration() {
        return first().getLockDuration();
    }

    @Override
    public ExternalTaskHandler getExternalTaskHandler() {
        return first().getExternalTaskHandler();
    }

    @Override
    public List<String> getVariableNames() {
        return first().getVariableNames();
    }

    @Override
    public boolean isLocalVariables() {
        return first().isLocalVariables();
    }

    @Override
    public String getBusinessKey() {
        return first().getBusinessKey();
    }

    @Override
    public String getProcessDefinitionId() {
        return first().getProcessDefinitionId();
    }

    @Override
    public List<String> getProcessDefinitionIdIn() {
        return first().getProcessDefinitionIdIn();
    }

    @Override
    public String getProcessDefinitionKey() {
        return first().getProcessDefinitionKey();
    }

    @Override
    public List<String> getProcessDefinitionKeyIn() {
        return first().getProcessDefinitionKeyIn();
    }

    @Override
    public String getProcessDefinitionVersionTag() {
        return first().getProcessDefinitionVersionTag();
    }

    @Override
    public Map<String, Object> getProcessVariables() {
        return first().getProcessVariables();
    }

    @Override
    public boolean isWithoutTenantId() {
        return first().isWithoutTenantId();
    }

    @Override
    public List<String> getTenantIdIn() {
        return first().getTenantIdIn();
    }

    @Override
    public boolean isIncludeExtensionProperties() {
        return first().isIncludeExtensionProperties();
    }
}
