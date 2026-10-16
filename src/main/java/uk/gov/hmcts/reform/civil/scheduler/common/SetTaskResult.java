package uk.gov.hmcts.reform.civil.scheduler.common;

import java.util.Set;
import java.util.stream.Stream;

public record SetTaskResult<T>(Set<T> items) implements TaskResult<T> {

    @Override
    public int totalResults() {
        return items.size();
    }

    @Override
    public Stream<T> itemStream() {
        return items.stream();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }
}
