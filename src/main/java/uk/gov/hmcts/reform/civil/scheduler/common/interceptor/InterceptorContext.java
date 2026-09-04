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

    public InterceptorContext(String schedulerName, T item) {
        this.schedulerName = schedulerName;
        this.item = item;
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public <V> Optional<V> getAttribute(String name, Class<V> type) {
        return Optional.ofNullable(attributes.get(name)).map(type::cast);
    }
}
