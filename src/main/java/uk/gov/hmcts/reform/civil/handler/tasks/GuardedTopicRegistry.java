package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records which external task topic is served by which handler, so the short-interval incident
 * retry can tell which topics are safe to re-run (those whose handler is
 * {@link BaseExternalTaskHandler#isRetryGuarded() retry guarded}).
 *
 * <p>The topics are not listed anywhere. Each listener subscribes to its topic through the
 * {@code ExternalTaskClient}, and the client bean is wrapped (see
 * {@code TopicRecordingExternalTaskClient}) so that every {@code subscribe(topic).handler(handler)}
 * call is recorded here. A handler that starts returning true from {@code isRetryGuarded()} is
 * therefore picked up with no other change.</p>
 */
@Slf4j
@Component
public class GuardedTopicRegistry {

    private final Map<String, ExternalTaskHandler> handlersByTopic = new ConcurrentHashMap<>();

    public void register(String topic, ExternalTaskHandler handler) {
        if (topic != null && handler != null) {
            ExternalTaskHandler previous = handlersByTopic.put(topic, handler);
            if (previous != null && previous != handler) {
                log.warn("Topic {} registered again with a different handler, replacing {}",
                         topic, previous.getClass().getSimpleName());
            }
            if (handler instanceof BaseExternalTaskHandler base && base.isRetryGuarded()) {
                log.info("Registered retry guarded topic {} (handler {})",
                         topic, handler.getClass().getSimpleName());
            } else {
                log.debug("Registered topic {} as not retry guarded", topic);
            }
        }
    }

    /**
     * Checks whether the handler subscribed to a topic is retry guarded.
     *
     * @param topic an external task topic name
     * @return true if a handler is subscribed to the topic and that handler is retry guarded
     */
    public boolean isGuarded(String topic) {
        ExternalTaskHandler handler = topic == null ? null : handlersByTopic.get(topic);
        return handler instanceof BaseExternalTaskHandler base && base.isRetryGuarded();
    }

    /**
     * Lists the topics whose handler is retry guarded.
     *
     * @return the guarded topics, sorted, for logging and tests
     */
    public Set<String> getGuardedTopics() {
        Set<String> guarded = new TreeSet<>();
        handlersByTopic.forEach((topic, handler) -> {
            if (isGuarded(topic)) {
                guarded.add(topic);
            }
        });
        return guarded;
    }

    public int registeredTopicCount() {
        return handlersByTopic.size();
    }
}
