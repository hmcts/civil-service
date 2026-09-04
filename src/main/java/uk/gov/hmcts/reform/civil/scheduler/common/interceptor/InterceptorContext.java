package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class InterceptorContext<T> {

    private final String schedulerName;
    private final T item;
    private final Map<String, Object> attributes = new HashMap<>();
    private final Map<String, Long> metrics = new HashMap<>();

    public InterceptorContext(String schedulerName, T item) {
        this.schedulerName = schedulerName;
        this.item = item;
    }

    /**
     * Records a metric in the context.
     *
     * @param name         the metric name
     * @param durationMillis the duration in milliseconds
     */
    public void recordMetric(String name, long durationMillis) {
        metrics.put(name, durationMillis);
    }

    /**
     * Sets an attribute in the context.
     *
     * @param key   the attribute key
     * @param value the attribute value
     * @param <V>   the type of the value
     */
    public <V> void setAttribute(AttributeKey<V> key, V value) {
        attributes.put(key.getName(), value);
    }

    /**
     * Gets an attribute from the context.
     *
     * @param key the attribute key
     * @param <V> the type of the value
     * @return an optional containing the value if present
     */
    public <V> Optional<V> getAttribute(AttributeKey<V> key) {
        return Optional.ofNullable(attributes.get(key.getName())).map(key.getType()::cast);
    }
}
